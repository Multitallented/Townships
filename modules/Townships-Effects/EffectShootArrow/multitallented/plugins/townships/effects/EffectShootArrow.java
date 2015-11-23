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
public class EffectShootArrow extends Effect {
    private final RegionManager rm;
    public EffectShootArrow(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new IntruderListener(this));
    }

    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }

    public class IntruderListener implements Listener {
        private final EffectShootArrow effect;
        private HashMap<Arrow, Integer> arrowDamages = new HashMap<Arrow, Integer>();
        //private HashMap<Arrow, String> arrowOwners = new HashMap<Arrow, String>();

        public IntruderListener(EffectShootArrow effect) {
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
            double speed = 0.5;
            int spread = 12;
            for (String s : rt.getEffects()) {
                if (s.startsWith("shoot_arrow")) {
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


            //Check if the player is invincible
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                return;
            }

//            EntityDamageEvent damageEvent = new EntityDamageEvent(null, DamageCause.CUSTOM, 0);
//            Bukkit.getPluginManager().callEvent(damageEvent);
//            if (damageEvent.isCancelled()) {
//                System.out.println("damage cancelled");
//                return;
//            }

            //Check if the player owns or is a member of the region
            if (effect.isOwnerOfRegion(player, l) || effect.isMemberOfRegion(player, l)) {
                return;
            }


            //Check to see if the Townships has enough reagents
            if (!effect.hasReagents(l)) {
                return;
            }

            //Damage check before firing
//            EntityDamageEvent testEvent = new EntityDamageEvent(player, DamageCause.CUSTOM, 0);
//            Bukkit.getPluginManager().callEvent(testEvent);
//            if (testEvent.isCancelled()) {
//                System.out.println("damage test failed");
//                return;
//            }

            HashSet<Arrow> arrows = new HashSet<Arrow>();
            for (Arrow arrow : arrowDamages.keySet()) {
                if (arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
                    arrows.add(arrow);
                }
            }
            for (Arrow arrow : arrows) {
                arrowDamages.remove(arrow);
            }


            //Calculate trajectory of the arrow
            Location loc = l.getBlock().getRelative(BlockFace.UP, 2).getLocation();
            Location playerLoc = player.getEyeLocation();

            Vector vel = new Vector(playerLoc.getX() - loc.getX(), playerLoc.getY() - loc.getY(), playerLoc.getZ() - loc.getZ());

            //Make sure the target is not hiding behind something
//            if (!hasCleanShot(loc, playerLoc)) {
//                System.out.println("line of sight failed");
//                return;
//            }

            //Run upkeep but don't need to know if upkeep occured
            effect.forceUpkeep(event);


            //Location playerLoc = player.getLocation().getBlock().getRelative(BlockFace.UP).getLocation();
            //playerLoc.setX(Math.floor(playerLoc.getX()) + 0.5);
            //playerLoc.setY(Math.floor(playerLoc.getY()) + 0.5);
            //playerLoc.setZ(Math.floor(playerLoc.getZ()) + 0.5);


            //Spawn and set velocity of the arrow
            Arrow arrow = l.getWorld().spawnArrow(loc, vel, (float) (speed), spread);
            arrowDamages.put(arrow, damage);
            //arrowOwners.put(arrow, r.getOwners().get(0));
        }

        @EventHandler
        public void onEntityDamageByEntityEvent(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() < 1 || !(event instanceof EntityDamageByEntityEvent)) {
                return;
            }
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            Entity projectile = edby.getDamager();
            if (!(projectile instanceof Arrow) || !(edby.getEntity() instanceof Player)) {
                return;
            }
            Arrow arrow = (Arrow) projectile;
            Player damagee = (Player) edby.getEntity();
            double maxHP = damagee.getMaxHealth();
            if (arrowDamages.get(arrow) == null) {
                return;
            }

            //String ownerName = arrowOwners.get(arrow);
            //Player player = null;
            //if (ownerName != null) {
            //    player = Bukkit.getPlayer(ownerName);
            //}

            int damage = (int) ((double) arrowDamages.get(arrow) / 100.0 * (double) maxHP);
            arrowDamages.remove(arrow);
            //arrowOwners.remove(arrow);

            //if (player != null) {
            //    damagee.damage(damage, player);
            //} else {
//                damagee.damage(damage);
                //damagee.damage(damage);
            //}
//            event.setCancelled(true);
            event.setDamage(damage);

        }

        private boolean hasCleanShot(Location shootHere, Location targetHere) {
            double x = shootHere.getX();
            double y = shootHere.getY();
            double z = shootHere.getZ();

            double x1 = targetHere.getX();
            double y1 = targetHere.getY();
            double z1 = targetHere.getZ();

            Vector start = new Vector(x, y, z);
            Vector end = new Vector (x1, y1, z1);

            BlockIterator bi = new BlockIterator(shootHere.getWorld(), start, end, 0, (int) shootHere.distance(targetHere));
            while (bi.hasNext()) {
                Block block = bi.next();
                System.out.println("[Townships] " + ((int) block.getLocation().getX()) +
                        ":" + ((int) block.getLocation().getY()) + ":" +
                        ((int) block.getLocation().getZ()) + " " + !Util.isSolidBlock(block.getType()));
                if (!Util.isSolidBlock(block.getType())) {
                    return false;
                }
            }

            return true;
        }
    }

}
