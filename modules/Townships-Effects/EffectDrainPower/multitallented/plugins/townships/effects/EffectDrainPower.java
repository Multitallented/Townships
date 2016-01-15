package multitallented.plugins.townships.effects;

import java.util.Date;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPreRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToRenameEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEffectEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectDrainPower extends Effect {
    private HashMap<Location, Long> lastUpkeep = new HashMap<Location, Long>();

    public EffectDrainPower(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(plugin, this));
    }

    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }

    public class UpkeepListener implements Listener {
        private final EffectDrainPower effect;
        private final Townships plugin;
        public UpkeepListener(Townships plugin, EffectDrainPower effect) {
            this.effect = effect;
            this.plugin = plugin;
        }


        @EventHandler
        public void onCustomEvent(ToTwoSecondEffectEvent event) {
            if (event.getEffect().length < 1 || !event.getEffect()[0].equals("drain_power")) {
                return;
            }
            Region r = event.getRegion();
            RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType());
            Location l = r.getLocation();

            //Check if the region has the shoot arrow effect and return arrow velocity
            long period = Long.parseLong(event.getEffect()[1]) * 1000;
                if (period < 1) {
                return;
            }
            int damage = 1;
            if (event.getEffect().size() > 2) {
            	damage = Integer.parseInt(event.getEffect()[2]);
            }

            if (lastUpkeep.get(l) != null && period + lastUpkeep.get(l) > new Date().getTime()) {
                return;
            }

            //Check if valid siege machine position
            if (l.getBlock().getRelative(BlockFace.UP).getY() < l.getWorld().getHighestBlockAt(l).getY()) {
                return;
            }

            //Check to see if the Townships has enough reagents
            if (!effect.hasReagents(l)) {
                return;
            }

            Block b = l.getBlock().getRelative(BlockFace.UP);
            if (!(b.getState() instanceof Sign)) {
                return;
            }

            //Find target Super-region
            Sign sign = (Sign) b.getState();
            String srName = sign.getLine(0);
            SuperRegion sr = plugin.getRegionManager().getSuperRegion(srName);
            if (sr == null) {
                for (SuperRegion currentSR : plugin.getRegionManager().getSortedSuperRegions()) {
                    if (currentSR.getName().startsWith(srName)) {
                        sr = currentSR;
                        break;
                    }
                }
                if (sr == null) {
                    sign.setLine(2, "invalid name");
                    sign.update();
                    return;
                }
            }

            //Check if too far away
            double rawRadius = plugin.getRegionManager().getSuperRegionType(sr.getType()).getRawRadius();
            try {
                if (sr.getLocation().distance(l) - rawRadius >  150) {
                    sign.setLine(2, "out of");
                    sign.setLine(3, "range");
                    sign.update();
                    return;
                }
            } catch (IllegalArgumentException iae) {
                sign.setLine(2, "out of");
                sign.setLine(3, "range");
                sign.update();
                return;
            }

            if (sr.getPower() < 1) {
                return;
            }

            //Run upkeep but don't need to know if upkeep occured
            //effect.forceUpkeep(l);
            effect.forceUpkeep(l);
            lastUpkeep.put(l, new Date().getTime());

            Location spawnLoc = l.getBlock().getRelative(BlockFace.UP, 3).getLocation();
            //Location srLoc = sr.getLocation();
            Location loc = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 15, spawnLoc.getZ());
            final Location loc1 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 20, spawnLoc.getZ());
            final Location loc2 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 25, spawnLoc.getZ());
            final Location loc3 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 30, spawnLoc.getZ());
            TNTPrimed tnt = l.getWorld().spawn(loc, TNTPrimed.class);
            tnt.setFuseTicks(1);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    TNTPrimed tnt = loc1.getWorld().spawn(loc1, TNTPrimed.class);
                    tnt.setFuseTicks(1);
                }
            }, 5L);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    TNTPrimed tnt = loc2.getWorld().spawn(loc2, TNTPrimed.class);
                    tnt.setFuseTicks(1);
                }
            }, 10L);

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    TNTPrimed tnt = loc3.getWorld().spawn(loc3, TNTPrimed.class);
                    tnt.setFuseTicks(1);
                }
            }, 15L);

            //double randX = srLoc.getX() + Math.random()*rawRadius*(-1 * (int) (Math.random() + 0.5));
            //double randZ = srLoc.getZ() + Math.random()*rawRadius*(-1 * (int) (Math.random() + 0.5));
            //final Location endLoc = new Location(srLoc.getWorld(), randX, 240, randZ);

			if (damage == 1) {
            	plugin.getRegionManager().reduceRegion(sr);
			} else {
				plugin.getRegionManager().reduceRegion(sr, damage);
			}
            if (sr.getPower() < 1 && Townships.getConfigManager().getDestroyNoPower()) {
                plugin.getRegionManager().destroySuperRegion(sr.getName(), true);
            }
        }

        @EventHandler
        public void onPreRegionCreated(ToPreRegionCreatedEvent event) {
            Location l = event.getLocation();
            RegionType rt = event.getRegionType();
            Player player = event.getPlayer();

            for (String s : rt.getEffects()) {
                if (s.startsWith("drain_power") || s.startsWith("charging_drain_power")) {
                    Block b = l.getBlock().getRelative(BlockFace.UP);
                    if (!(b.getState() instanceof Sign)) {
                        player.sendMessage(ChatColor.RED + "[Townships] You need a sign above the chest with the name of the target super region.");
                        event.setCancelled(true);
                        return;
                    }

                    if (l.getBlock().getRelative(BlockFace.UP).getY() < l.getWorld().getHighestBlockAt(l).getY()) {
                        player.sendMessage(ChatColor.RED + "[Townships] There must not be any blocks above the siegecannon center.");
                        event.setCancelled(true);
                        return;
                    }

                    //Find target Super-region
                    Sign sign = (Sign) b.getState();
                    String srName = sign.getLine(0);
                    SuperRegion sr = plugin.getRegionManager().getSuperRegion(srName);
                    if (sr == null) {
                        sign.setLine(0, "invalid target");
                        sign.update();
                        player.sendMessage(ChatColor.RED + "[Townships] You must put the name of the target super region on the first line of the sign.");
                        event.setCancelled(true);
                        return;
                    }
                    Bukkit.broadcastMessage(ChatColor.GRAY + "[Townships] " + ChatColor.RED +
                            player.getDisplayName() + ChatColor.WHITE + " has created a " +
                            ChatColor.RED + rt.getName() + ChatColor.WHITE + " targeting " + ChatColor.RED + sr.getName());
                    return;
                }
            }
        }

        @EventHandler
        public void onRename(ToRenameEvent event) {
            RegionManager rm = getPlugin().getRegionManager();
            for (Region r : rm.getSortedRegions()) {
                RegionType rt = rm.getRegionType(r.getType());
                if (rt == null) {
                    continue;
                }

                boolean hasEffect = false;
                for (String s : rt.getEffects()) {
                    if (s.startsWith("drain_power")) {
                        hasEffect = true;
                        break;
                    }
                }
                if (!hasEffect) {
                    continue;
                }

                Sign sign;
                Block b = r.getLocation().getBlock().getRelative(BlockFace.UP);
                try {
                    if (!(b instanceof Sign)) {
                        continue;
                    }
                    sign = (Sign) b;
                } catch (Exception e) {
                    continue;
                }
                String srName = sign.getLine(0);
                SuperRegion sr = rm.getSuperRegion(srName);
                if (sr == null) {
                    continue;
                }
                if (sr.getName().equals(event.getOldName())) {
                    sign.setLine(0, event.getNewName());
                }
            }
        }
    }
}
