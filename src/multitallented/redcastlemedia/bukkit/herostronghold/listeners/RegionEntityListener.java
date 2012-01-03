package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegionType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EndermanPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

/**
 *
 * @author Multitallented
 */
public class RegionEntityListener extends EntityListener {
    private final RegionManager rm;
    public RegionEntityListener(RegionManager rm) {
        this.rm = rm;
    }
    
    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        
        Player player = (Player) event.getEntity();
        String playername = player.getName();
        Set<String> regionsToReduce = new HashSet<String>();
        for (String s : rm.getSuperRegionNames()) {
            SuperRegion sr = rm.getSuperRegion(s);
            if (sr.hasMember(playername) || sr.hasOwner(playername))
                regionsToReduce.add(s);
        }
        if (!regionsToReduce.isEmpty()) {
            for (String s : regionsToReduce) {
                SuperRegion sr = rm.getSuperRegion(s);
                rm.reduceRegion(sr);
                rm.destroySuperRegion(s, true);
            }
        }
    }
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent))
            return;
        
        EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
        Entity damager = edby.getDamager(); 
        if (event.getCause() == DamageCause.PROJECTILE) {
            damager = ((Projectile)damager).getShooter();
        }
        if (!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Player dPlayer = (Player) damager;
        
        Location loc = player.getLocation();
        double x1 = loc.getX();
        for (SuperRegion sr : rm.getSortedSuperRegions()) {
            int radius = rm.getSuperRegionType(sr.getType()).getRadius();
            Location l = sr.getLocation();
            if (l.getX() + radius < x1) {
                break;
            }
            SuperRegionType srt = rm.getSuperRegionType(sr.getType());
            try {
                if (!(l.getX() - radius > x1) && l.distanceSquared(loc) < radius) {
                    if (srt.hasEffect("denypvp")) {
                        dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] " + player.getDisplayName() + " is protected in this region.");
                        event.setCancelled(true);
                        return;
                    } else if (srt.hasEffect("denyfriendlyfire")) {
                        String playername = player.getName();
                        String dPlayername = dPlayer.getName();
                        if ((sr.hasMember(playername) || sr.hasOwner(playername)) && (sr.hasMember(dPlayername) || sr.hasOwner(dPlayername))) {
                            dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] Friendly fire is off in this region.");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
        
        /*for (String s : rm.getSuperRegionNames()) {
            SuperRegion sr = rm.getSuperRegion(s);
            Location l = sr.getLocation();
            
            SuperRegionType srt = rm.getSuperRegionType(sr.getType());
            try {
                if (Math.sqrt(l.distanceSquared(player.getLocation())) < srt.getRadius()) {
                    if (srt.hasEffect("denypvp")) {
                        dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] " + player.getDisplayName() + " is protected in this region.");
                        event.setCancelled(true);
                        return;
                    } else if (srt.hasEffect("denyfriendlyfire")) {
                        String playername = player.getName();
                        String dPlayername = dPlayer.getName();
                        if ((sr.hasMember(playername) || sr.hasOwner(playername)) && (sr.hasMember(dPlayername) || sr.hasOwner(dPlayername))) {
                            dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] Friendly fire is off in this region.");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }*/
        
        double x = player.getLocation().getX();
        for (Region r : rm.getSortedRegions()) {
            int radius = rm.getRegionType(r.getType()).getRadius();
            Location l = r.getLocation();
            if (l.getX() + radius < x) {
                return;
            }
            try {
                if (!(l.getX() - radius > x) && l.distanceSquared(player.getLocation()) < radius) {
                    ArrayList<String> effects = rm.getRegionType(r.getType()).getEffects();
                    if (effects != null && !effects.isEmpty()) {
                        for (String effect : effects) {
                            String[] params = effect.split("\\.");
                            if (params.length > 1 && params[0].equalsIgnoreCase("denypvp")) {
                                dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] " + player.getDisplayName() + " is protected in this region.");
                                event.setCancelled(true);
                                return;
                            } else if (params.length > 1 && params[0].equalsIgnoreCase("denyfriendlyfire")) {
                                String playername = player.getName();
                                String dPlayername = dPlayer.getName();
                                if ((r.isMember(playername) || r.isOwner(playername)) && (r.isMember(dPlayername) || r.isOwner(dPlayername))) {
                                    dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] Friendly fire is off in this region.");
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
        
        /*for (Location l : rm.getRegionLocations()) {
            Region r = rm.getRegion(l);
            RegionType rt = rm.getRegionType(r.getType());
            try {
                if (Math.sqrt(l.distanceSquared(player.getLocation())) < rt.getRadius()) {
                    ArrayList<String> effects = rt.getEffects();
                    if (effects != null && !effects.isEmpty()) {
                        for (String effect : effects) {
                            String[] params = effect.split("\\.");
                            if (params.length > 1 && params[0].equalsIgnoreCase("denypvp")) {
                                dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] " + player.getDisplayName() + " is protected in this region.");
                                event.setCancelled(true);
                                return;
                            } else if (params.length > 1 && params[0].equalsIgnoreCase("denyfriendlyfire")) {
                                String playername = player.getName();
                                String dPlayername = dPlayer.getName();
                                if ((r.isMember(playername) || r.isOwner(playername)) && (r.isMember(dPlayername) || r.isOwner(dPlayername))) {
                                    dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] Friendly fire is off in this region.");
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }*/
    }
    
    @Override
    public void onPaintingPlace(PaintingPlaceEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getPainting().getLocation(), event.getPlayer(), 0, "denyblockbuild"))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
    }

    @Override
    public void onEndermanPlace(EndermanPlaceEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getLocation(), null, 0, "denyblockbuild"))
            return;
        event.setCancelled(true);
    }

    @Override
    public void onPaintingBreak(PaintingBreakEvent event) {
        if (event.isCancelled() || !(event instanceof PaintingBreakByEntityEvent))
            return;
        PaintingBreakByEntityEvent pEvent = (PaintingBreakByEntityEvent) event;
        if (!(pEvent.getRemover() instanceof Player))
            return;
        Player player = (Player) pEvent.getRemover();
        if (!rm.shouldTakeAction(event.getPainting().getLocation(), player, 0, "denyblockbreak"))
            return;

        event.setCancelled(true);
        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
    }


    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Location loc = event.getLocation();
        Entity ent = event.getEntity();
        if ((!(ent instanceof Creeper) && !(ent instanceof EnderDragon) && !(ent instanceof TNTPrimed) && !(ent instanceof Fireball))
                && !rm.shouldTakeAction(loc, null, 4, "denyexplosion")) {
            event.setCancelled(true);
            return;
        }
        Region destroyMe = null;
        
        double x = loc.getX();
        for (Region r : rm.getSortedRegions()) {
            int radius = rm.getRegionType(r.getType()).getRadius();
            Location l = r.getLocation();
            if (l.getX() + radius < x) {
                return;
            }
            try {
                if (!(l.getX() - radius > x) && l.distanceSquared(loc) < radius) {
                    destroyMe = r;
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
        
        if (destroyMe != null) {
            rm.destroyRegion(destroyMe.getLocation());
            rm.removeRegion(destroyMe.getLocation());
        }
    }

    @Override
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getEntity().getLocation(), null, 0, "denyblockbreak"))
            return;
        event.setCancelled(true);
    }

    @Override
    public void onEndermanPickup(EndermanPickupEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak"))
            return;
        event.setCancelled(true);
    }
}
