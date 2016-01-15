package multitallented.plugins.townships.effects;

import java.util.HashMap;
import java.util.HashSet;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

/**
 *
 * @author Multitallented
 */
public class EffectShootMissile extends Effect {
    private final RegionManager rm;
    public EffectShootMissile(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new IntruderListener(this));
    }

    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }

    public class IntruderListener implements Listener {
        private final EffectShootMissile effect;
        private final HashMap<TNTPrimed, FiredTNT> firedTNT = new HashMap<TNTPrimed, FiredTNT>();
        private final HashMap<Integer, Long> cooldowns = new HashMap<Integer, Long>();

        public IntruderListener(EffectShootArrow effect) {
            this.effect = effect;
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
        	if ((event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
        			event.getPlayer().getHeldItem() == null || event.getPlayer().getHeldItem().getItemMeta() == null ||
        			event.getPlayer().getHeldItem().getItemMeta().getDisplayName() == null ||
        			!event.getPlayer().getHeldItem().getItemMeta().getDisplayName().equals("Missile Controller")) {
        		return;
        	}
        	Player player = (Player) event.getPlayer();
        	int id = -1;
        	try {
        		String name = player.getHeldItem().getItemMeta().getDisplayName();
        		id = Integer.parseInt(name.replace("Missile Controller ", ""));
        	} catch (Exception e) {
        		return;
        	}
        	if (id < 0) {
        		return;
        	}
        	RegionManager rm = plugin.getRegionManager();
        	Region region = rm.getRegionById(id);
        	if (region == null || !effect.isOwnerOfRegion(player, l)) {
        		return;
        	}
        	RegionType rt = rm.getRegionType(region.getType());
        	boolean hasEffect = false;
        	int periods = 4;
        	long cooldown = 8;
        	for (String effectName : rt.getEffects()) {
        		if (effectName.startsWith("shoot_missile")) {
        			hasEffect = true;
        			String[] effectParts = effectName.split("\\.");
        			if (effectParts.length > 2) {
        				try {
        					periods = Integer.parseInt(effectParts[1]);
        					cooldown = Long.parseLong(effectParts[2]);
        				} catch (Exception e) {
        					//Do nothing and just use defaults
        				}
        			}

        			break;
				}
        	}
        	if (!hasEffect) {
        		return;
        	}
        	if (!effect.hasReagents(region.getLocation())) {
				return;
			}

        	if (cooldowns.get(id) > System.currentTimeMillis()) {
        		//TODO show how long till reload is done
        		player.sendMessage(ChatColor.RED + "[Townships] That " + region.getType() + " is reloading.");
        		return;
        	}
        	HashSet<Byte> bytes = new HashSet<Byte>();
        	bytes.add((Byte) 0);
        	Location targetLocation = player.getTargetBlock(bytes, 100).getLocation();
			Location fireLocation = region.getLocation();
			if (!targetLocation.getWorld().equals(fireLocation.getWorld())) {
				return;
			}
			fireLocation.setY(fireLocation.getY() + 2);
			TNTPrimed tnt = (TNTPrimed) fireLocation.getWorld().spawn(fireLocation, TNTPrimed.class);

			Vector vector = ((targetLocation.getX() - fireLocation.getX()) / periods,
							 (targetLocation.getY() - fireLocation.getY()) / periods + (100 / periods * 2),
							 (targetLocation.getZ() - fireLocation.getZ()) / periods);
			tnt.setVelocity(vector);

			FiredTNT ftnt = new FiredTNT(tnt, periods, fireLocation, targetLocation);
			firedTNT.put(tnt, ftnt);
			cooldowns.put(id, System.currentTimeMillis() + cooldown * 1000);

			player.sendMessage(ChatColor.GREEN + "[Townships] Your " + region.getType() + " has fired a missile at your new target.";
        }

        @EventHandler
        public void onTwoSecondEvent(TwoSecondEvent event) {
        	HashSet<TNTPrimed> removeTNT = new HashSet<TNTPrimed>();
        	for (FiredTNT tnt : firedTNT.values()) {
				if (tnt.getStage() < 2) {
					removeTNT.add(tnt.getTNT());
				}
				TNTPrimed tntPrimed = tnt.getTNT();
				Location tntLocation = tntPrimed.getLocation();
				tntPrimed.remove();
				tntPrimed = tntLocation.getWorld().spawn(tntLocation, TNTPrimed.class);
				//TODO set velocity and decrement the stage
        	}
        	for (TNTPrimed tnt : removeTNT) {
        		firedTNT.remove(tnt);
        	}
        	HashSet<Integer> removeMe = new HashSet<Integer>();
        	for (Integer id : cooldowns.keySet()) {
        		if (cooldowns.get(id) < System.currentTimeMillis()) {
        			removeMe.add(id);
        		}
        	}
        	for (Integer id : removeMe) {
				cooldowns.remove(id);
        	}
        }

        private class FiredTNT {
        	public FiredTNT(TNTPrimed tnt, int stage, Location startLocation, Location targetLocation) {
        		this.tnt = tnt;
        		this.stage = stage;
        		this.startLocation = startLocation;
        		this.targetLocation = targetLocation;
        	}

        	public void setTNT(TNTPrimed tnt) {
        		this.tnt = tnt;
        	}
        	public void setStage(int stage) {
        		this.stage = stage;
        	}
        	public void setStartLocation(Location startLocation) {
        		this.startLocation = startLocation;
        	}
        	public void setTargetLocation(Location targetLocation) {
        		this.targetLocation = targetLocation;
        	}
        	public TNTPrimed getTNT() {
        		return tnt;
        	}
        	public int getStage() {
        		return stage;
        	}
        	public Location getStartLocation() {
        		return startLocation;
        	}
        	public Location getTargetLocation() {
        		return targetLocation;
        	}
        }
    }

}
