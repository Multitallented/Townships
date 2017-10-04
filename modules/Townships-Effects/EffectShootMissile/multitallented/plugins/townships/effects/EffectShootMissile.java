package multitallented.plugins.townships.effects;

import java.util.HashMap;
import java.util.Set;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.*;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

        public IntruderListener(EffectShootMissile effect) {
            this.effect = effect;
        }


        @EventHandler
        public void onCreate(ToRegionCreatedEvent event) {
            Region region = event.getRegion();
            RegionType rt = rm.getRegionType(region.getType());
            boolean hasEffect = false;
            int periods = 4;
            long cooldown = 8;
            double accuracy = 1;
            for (String effectName : rt.getEffects()) {
                if (effectName.startsWith("shoot_missile")) {
                    hasEffect = true;
                    String[] effectParts = effectName.split("\\.");
                    if (effectParts.length > 3) {
                        try {
                            periods = Integer.parseInt(effectParts[1]);
                            cooldown = Long.parseLong(effectParts[2]);
                            accuracy = Double.parseDouble(effectParts[3]);
                        } catch (Exception e) {
                            return;
                        }
                    }

                    break;
                }
            }
            if (!hasEffect) {
                return;
            }
            ItemStack controllerWand = new ItemStack(Material.STICK, 1);
            ItemMeta im = controllerWand.getItemMeta();
            im.setDisplayName("Missile Controller " + region.getID());
            controllerWand.setItemMeta(im);

            region.getLocation().getWorld().dropItemNaturally(region.getLocation().getBlock().getRelative(BlockFace.UP,2).getLocation(), controllerWand);
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if ((event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                    event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getItemMeta() == null ||
                    event.getPlayer().getItemInHand().getItemMeta().getDisplayName() == null ||
                    !event.getPlayer().getItemInHand().getItemMeta().getDisplayName().contains("Missile Controller")) {
                return;
            }
            Player player = event.getPlayer();
            int id;
            try {
                String name = player.getItemInHand().getItemMeta().getDisplayName();
                id = Integer.parseInt(name.replace("Missile Controller ", ""));
            } catch (Exception e) {
                return;
            }
            if (id < 0) {
                return;
            }
            RegionManager rm = getPlugin().getRegionManager();
            Region region = rm.getRegionByID(id);
            if (region == null || !region.getOwners().contains(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "[Townships] You must be an owner to use this.");
                return;
            }
            RegionType rt = rm.getRegionType(region.getType());
            boolean hasEffect = false;
            long cooldown = 8;
            for (String effectName : rt.getEffects()) {
                if (effectName.startsWith("shoot_missile")) {
                    hasEffect = true;
                    String[] effectParts = effectName.split("\\.");
                    if (effectParts.length > 1) {
                        try {
                            cooldown = Long.parseLong(effectParts[1]);
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
            Location fireLocation = region.getLocation().getBlock().getRelative(BlockFace.UP, 2).getLocation();
            if (!effect.hasReagents(region.getLocation())) {
                return;
            }
            event.setCancelled(true);

            if (cooldowns.get(id) != null && cooldowns.get(id) > System.currentTimeMillis()) {
                //TODO show how long till reload is done
                player.sendMessage(ChatColor.RED + "[Townships] That " + region.getType() + " is reloading.");
                return;
            }
            Location targetLocation = player.getTargetBlock((Set<Material>) null, 100).getLocation();
            if (!targetLocation.getWorld().equals(fireLocation.getWorld())) {
                return;
            }
            if (targetLocation.distanceSquared(fireLocation) < 1600) {
                player.sendMessage(ChatColor.RED + "[Townships] That target is too close to shoot at.");
                return;
            }
            TNTPrimed tnt = (TNTPrimed) fireLocation.getWorld().spawn(fireLocation, TNTPrimed.class);

            /*Vector vector = new Vector((targetLocation.getX() - fireLocation.getX()) / periods,
                             (targetLocation.getY() - fireLocation.getY()) / periods + (100 / periods * 2),
                             (targetLocation.getZ() - fireLocation.getZ()) / periods);
            tnt.setVelocity(vector);*/

            //vt terminal velocity == 1.96
            //vo muzzle velocity
            //g acceleration (gravity) == 0.04
            //theta = angle of elevation == 60
            //
            double g = 0.04;
            double vt = 1.96;

            double deltaX = Math.sqrt(Math.pow(targetLocation.getX() - fireLocation.getX(), 2) + Math.pow(targetLocation.getZ() + fireLocation.getZ(), 2));
            deltaX = targetLocation.distance(fireLocation);
//            double deltaY = targetLocation.getY() - fireLocation.getY();

            double theta = 64.2556-0.0651852*deltaX;
            theta = Math.PI * theta / 180;

            double current = 0.977778*g*(11.4205+deltaX)/(vt*Math.cos(theta));
//            double prevPrev = 0.041*deltaX;
//            double prev = prevPrev + 0.01;
//            accuracy = accuracy / 10000;

            /*int i = 0;
            while (Math.abs(functionDx(deltaX, deltaY, prev)) > accuracy && i <= 9001) {
                current = (prevPrev*functionDx(deltaX, deltaY, prev)-prev*functionDx(deltaX,deltaY,prevPrev))/(functionDx(deltaX,deltaY,prev)-functionDx(deltaX, deltaY, prevPrev));
                prevPrev = prev;
                prev = current;
                i++;
            }*/
//            player.sendMessage(ChatColor.GREEN + "[Townships] Val: " + Math.abs(functionDx(deltaX, deltaY, prev)));
//            player.sendMessage(ChatColor.GREEN + "[Townships] Iterations: " + i);
            double newX = current*Math.cos(theta)*Math.cos(Math.atan2(targetLocation.getZ() - fireLocation.getZ(), targetLocation.getX() - fireLocation.getX()));
            double newZ = current*Math.cos(theta)*Math.sin(Math.atan2(targetLocation.getZ() - fireLocation.getZ(), targetLocation.getX() - fireLocation.getX()));
            double newY = current*Math.sin(theta);

//            player.sendMessage(ChatColor.GREEN + "[Townships] Current Velocity: " + current);

            Vector vector1 = new Vector(newX, newY, newZ);
            tnt.setVelocity(vector1);
            tnt.setFuseTicks(240);

//            FiredTNT ftnt = new FiredTNT(tnt, periods, fireLocation, targetLocation);
//            firedTNT.put(tnt, ftnt);
            cooldowns.put(id, System.currentTimeMillis() + cooldown * 1000);

            effect.forceUpkeep(region.getLocation());
            /*player.sendMessage(ChatColor.GREEN + "[Townships] Dx: " + deltaX);
            player.sendMessage(ChatColor.GREEN + "[Townships] Velocity: " + newX + ", " + newY + ", " + newZ);
            player.sendMessage(ChatColor.GREEN + "[Townships] Theta: " + theta);
            player.sendMessage(ChatColor.GREEN + "[Townships] Current: " + current);*/
            player.getWorld().playEffect(fireLocation.getBlock().getRelative(BlockFace.NORTH, 1).getLocation(), org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation.getBlock().getRelative(BlockFace.EAST, 1).getLocation(), org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation.getBlock().getRelative(BlockFace.WEST, 1).getLocation(), org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation.getBlock().getRelative(BlockFace.SOUTH, 1).getLocation(), org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation.getBlock().getRelative(BlockFace.UP, 1).getLocation(), org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation, org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation, org.bukkit.Effect.EXPLOSION_LARGE, 1);
            player.getWorld().playEffect(fireLocation, org.bukkit.Effect.EXPLOSION_LARGE, 1);
            for (Player currPlayer : Bukkit.getOnlinePlayers()) {
                if (currPlayer.getLocation().distanceSquared(fireLocation) > 2500) {
                    continue;
                }
//                currPlayer.playSound(fireLocation, Sound.EXPLODE, 2, 1);
                //TODO fix this sound
                try {
                    currPlayer.playSound(fireLocation, "EXPLODE", 2, 1);
                } catch (Exception e) {
                    //meh...
                }
            }

            player.sendMessage(ChatColor.GREEN + "[Townships] Your " + region.getType() + " has fired ordinance at your new target.");
        }

        private double functionDx(double deltaX, double deltaY, double v) {
            try {
                return 0.04 * deltaY - 0.04 * deltaX * (v * 1.732050808 + 1.96 / 0.5) + (3.8416) * Math.log(v * 0.5 / (v * 0.5 - 0.04 * deltaX / 1.96));
            } catch (Exception e) {
                return 10000;
            }
        }

        /*@EventHandler
        public void onTwoSecondEvent(ToTwoSecondEvent event) {
            HashSet<TNTPrimed> removeTNT = new HashSet<TNTPrimed>();
            for (FiredTNT tnt : firedTNT.values()) {
                if (tnt.getStage() < 2) {
                    removeTNT.add(tnt.getTNT());
                }
                TNTPrimed tntPrimed = tnt.getTNT();
                Location tntLocation = tntPrimed.getLocation();
                tntPrimed.remove();
                tntPrimed = tntLocation.getWorld().spawn(tntLocation, TNTPrimed.class);
                Vector vector = new Vector();
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
        }*/

        private class FiredTNT {
            private int stage;
            private Location startLocation;
            private Location targetLocation;
            private TNTPrimed tnt;

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
