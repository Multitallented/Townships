package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.effect.Effect;
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
    private final HeroStronghold plugin;
    public RegionEntityListener(HeroStronghold plugin) {
        this.plugin = plugin;
        this.rm = plugin.getRegionManager();
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
                SendMessageThread smt = new SendMessageThread(plugin, sr.getName(), plugin.getChannels(), null, player, "lost 1 power (" + sr.getPower() + " remaining)");
                try {
                    smt.run();
                } catch(Exception e) {

                }
                if (sr.getPower() < 1) {
                    rm.destroySuperRegion(s, true);
                }
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
        for (SuperRegion sr : rm.getContainingSuperRegions(loc)) {
            boolean notMember = player == null;
            if (!notMember) {
                notMember = !(sr.hasOwner(player.getName()) || sr.hasMember(player.getName()));
            }
            boolean reqs = rm.hasAllRequiredRegions(sr);
            boolean hasEffect = rm.getSuperRegionType(sr.getType()).hasEffect("denypvp");
            boolean hasEffect1 = rm.getSuperRegionType(sr.getType()).hasEffect("denypvpnoreagent");
            boolean hasEffect2 = rm.getSuperRegionType(sr.getType()).hasEffect("denyfriendlyfire");
            boolean hasEffect3 = rm.getSuperRegionType(sr.getType()).hasEffect("denyfriendlyfirenoreagent");
            boolean hasPower = sr.getPower() > 0;
            boolean hasMoney = sr.getBalance() > 0;
            boolean bothMembers = !notMember && (sr.hasMember(dPlayer.getName()) || sr.hasOwner(dPlayer.getName()));
            if ((!notMember && hasEffect1) || (!notMember && hasEffect && reqs && hasPower && hasMoney)) {
                dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] " + player.getDisplayName() + " is protected in this region.");
                event.setCancelled(true);
                return;
            } else if ((bothMembers && hasEffect3) || (bothMembers && hasEffect2 && reqs && hasPower && hasMoney)) {
                dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] Friendly fire is off in this region.");
                event.setCancelled(true);
                return;
            }
        }
        for (Region r : rm.getContainingRegions(loc)) {
            Effect effect = new Effect(plugin);
            boolean member = r.isMember(player.getName()) || r.isOwner(player.getName());
            boolean bothMembers = member && (r.isMember(dPlayer.getName()) || r.isOwner(dPlayer.getName()));
            boolean hasEffect = effect.regionHasEffect(rm.getRegionType(r.getType()).getEffects(), "denypvp") > 0;
            boolean hasEffect1 = effect.regionHasEffect(rm.getRegionType(r.getType()).getEffects(), "denypvpnoreagent") > 0;
            boolean hasEffect2 = effect.regionHasEffect(rm.getRegionType(r.getType()).getEffects(), "denyfriendlyfire") > 0;
            boolean hasEffect3 = effect.regionHasEffect(rm.getRegionType(r.getType()).getEffects(), "denyfriendlyfirenoreagent") > 0;
            boolean hasReagents = effect.hasReagents(r.getLocation());
            
            if ((member && hasEffect1) || (member && hasEffect && hasReagents)) {
                dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] " + player.getDisplayName() + " is protected in this region.");
                event.setCancelled(true);
                return;
            } else if ((bothMembers && hasEffect3) || (bothMembers && hasEffect2 && hasReagents)) {
                dPlayer.sendMessage(ChatColor.RED + "[HeroStronghold] Friendly fire is off in this region.");
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @Override
    public void onPaintingPlace(PaintingPlaceEvent event) {
        if ((event.isCancelled() || !rm.shouldTakeAction(event.getPainting().getLocation(), event.getPlayer(), 0, "denyblockbuild", true))  &&
                (event.isCancelled() || !rm.shouldTakeAction(event.getPainting().getLocation(), event.getPlayer(), 0, "denyblockbuildnoreagent", false))) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
    }

    @Override
    public void onEndermanPlace(EndermanPlaceEvent event) {
        if ((event.isCancelled() || !rm.shouldTakeAction(event.getLocation(), null, 0, "denyblockbuild", true)) &&
                (event.isCancelled() || !rm.shouldTakeAction(event.getLocation(), null, 0, "denyblockbuildnoreagent", false))) {
            return;
        }
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
        if ((!rm.shouldTakeAction(event.getPainting().getLocation(), player, 0, "denyblockbreak", true)) && 
                (!rm.shouldTakeAction(event.getPainting().getLocation(), player, 0, "denyblockbreaknoreagent", false))) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
    }


    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Creeper || event.getEntity() instanceof EnderDragon
                || event.getEntity() instanceof TNTPrimed || event.getEntity() instanceof Fireball)) {
            return;
        }
        
        Location loc = event.getLocation();
        if ((rm.shouldTakeAction(loc, null, 4, "denyexplosion", true)) &&
                (rm.shouldTakeAction(loc, null, 4, "denyexplosionnoreagent", false))) {
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
        if ((event.isCancelled() || !rm.shouldTakeAction(event.getEntity().getLocation(), null, 0, "denyblockbreak", true)) &&
                (event.isCancelled() || !rm.shouldTakeAction(event.getEntity().getLocation(), null, 0, "denyblockbreaknoreagent", false))) {
            return;
        }
        event.setCancelled(true);
    }

    @Override
    public void onEndermanPickup(EndermanPickupEvent event) {
        if ((event.isCancelled() || !rm.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak", true)) &&
                (event.isCancelled() || !rm.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreaknoreagent", false))) {
            return;
        }
        event.setCancelled(true);
    }
}
