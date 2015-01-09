package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author Multitallented
 */
public class EffectShootPotion extends Effect {
    private final RegionManager rm;
    public EffectShootPotion(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new IntruderListener(this));
    }

    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }

    public class IntruderListener implements Listener {
        private final EffectShootPotion effect;
        //private HashMap<Arrow, Integer> arrowDamages = new HashMap<Arrow, Integer>();
        //private HashMap<Arrow, String> arrowOwners = new HashMap<Arrow, String>();

        public IntruderListener(EffectShootPotion effect) {
            this.effect = effect;
        }

        @EventHandler
        public void onCustomEvent(ToPlayerInRegionEvent event) {

            Location l = event.getLocation();
            Region r = rm.getRegion(l);
            RegionType rt = rm.getRegionType(r.getType());
            //Check if the region has the shoot arrow effect and return arrow velocity
            boolean hasEffect = false;
            int damage = 1;
            double speed = 1;
            int spread = 12;
            for (String s : rt.getEffects()) {
                if (s.startsWith("shoot_potion")) {
                    hasEffect = true;
                    String[] parts = s.split("\\.");
                    if (parts.length > 1) {
                        try {
                            damage = Integer.parseInt(parts[1]);
                        } catch (Exception e) {
                            break;
                        }
                    }
                    if (parts.length > 2) {
                        try {
                            speed = Double.parseDouble(parts[2]) / 10;
                        } catch (Exception e) {
                            break;
                        }
                    }
                    if (parts.length > 3) {
                        try {
                            spread = Integer.parseInt(parts[3]);
                        } catch (Exception e) {
                            break;
                        }
                    }
                    break;
                }
            }
            if (!hasEffect) {
                return;
            }

            if (l.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
                return;
            }

            Player player = event.getPlayer();

            //Check if the player owns or is a member of the region
            if (effect.isOwnerOfRegion(player, l) || effect.isMemberOfRegion(player, l)) {
                return;
            }

            //Check to see if the Townships has enough reagents
            if (!effect.hasReagents(l)) {
                return;
            }

            /*HashSet<Arrow> arrows = new HashSet<Arrow>();
            for (Arrow arrow : arrowDamages.keySet()) {
                if (arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
                    arrows.add(arrow);
                }
            }
            for (Arrow arrow : arrows) {
                arrowDamages.remove(arrow);
            }*/

            //Run upkeep but don't need to know if upkeep occured
            effect.forceUpkeep(event);

            //Calculate trajectory of the arrow
            Location loc = l.getBlock().getRelative(BlockFace.UP, 2).getLocation();
            Location playerLoc = player.getEyeLocation();
            //Location playerLoc = player.getLocation().getBlock().getRelative(BlockFace.UP).getLocation();
            //playerLoc.setX(Math.floor(playerLoc.getX()) + 0.5);
            //playerLoc.setY(Math.floor(playerLoc.getY()) + 0.5);
            //playerLoc.setZ(Math.floor(playerLoc.getZ()) + 0.5);

            Vector vel = new Vector(playerLoc.getX() - loc.getX(), playerLoc.getY() - loc.getY(), playerLoc.getZ() - loc.getZ());
            vel.multiply(speed);
            //Spawn and set velocity of the arrow
            ThrownPotion potion = (ThrownPotion) l.getWorld().spawnEntity(loc, EntityType.SPLASH_POTION);
            potion.setItem(new ItemStack(Material.POTION, 1, (short) damage));
            potion.setVelocity(vel);
            //Arrow arrow = l.getWorld().spawnArrow(loc, vel, (float) (speed), spread);
            //arrowDamages.put(arrow, damage);
            //arrowOwners.put(arrow, r.getOwners().get(0));
        }
    }

}
