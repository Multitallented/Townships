package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.ConfigManager;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.effect.Effect;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

/**
 *
 * @author Multitallented
 */
public class RegionEntityListener implements Listener {
    private final RegionManager rm;
    private final HeroStronghold plugin;
    public RegionEntityListener(HeroStronghold plugin) {
        this.plugin = plugin;
        this.rm = plugin.getRegionManager();
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        ConfigManager cm = HeroStronghold.getConfigManager();
        if (!cm.getUsePower()) {
            return;
        }
        EntityDamageEvent ede = event.getEntity().getLastDamageCause();
        if (!(ede instanceof EntityDamageByEntityEvent)) {
            return;
        }
        EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) ede;
        Entity d = edby.getDamager();
        if (edby.getCause() == DamageCause.PROJECTILE) {
            d = ((Projectile) d).getShooter();
        }
        if (!(d instanceof Player)) {
            return;
        }
        Player dPlayer = (Player) d;
        String dPlayername = dPlayer.getName();
        int powerLoss = cm.getPowerPerKill();
        
        Player player = (Player) event.getEntity();
        String playername = player.getName();
        if (cm.getUseWar()) {
            HashSet<SuperRegion> tempSet = new HashSet<SuperRegion>();
            HashSet<SuperRegion> dTempSet = new HashSet<SuperRegion>();
            for (SuperRegion sr : rm.getSortedSuperRegions()) {
                if (sr.hasMember(playername) || sr.hasOwner(playername)) {
                    tempSet.add(sr);
                } else if (sr.hasMember(dPlayername) || sr.hasOwner(dPlayername)) {
                    dTempSet.add(sr);
                }
            }
            for (SuperRegion sr : tempSet) {
                for (SuperRegion srt : dTempSet) {
                    if (rm.hasWar(sr, srt)) {
                        rm.reduceRegion(sr);
                        SendMessageThread smt = new SendMessageThread(plugin, sr.getName(), plugin.getChannels(), null, player, "lost " + powerLoss + " power (" + sr.getPower() + " remaining)");
                        try {
                            smt.run();
                        } catch(Exception e) {

                        }
                        if (sr.getPower() < powerLoss && cm.getDestroyNoPower()) {
                            rm.destroySuperRegion(sr.getName(), true);
                        }
                    }
                }
            }
        } else {
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
                    SendMessageThread smt = new SendMessageThread(plugin, sr.getName(), plugin.getChannels(), null, player, "lost " + powerLoss + " power (" + sr.getPower() + " remaining)");
                    try {
                        smt.run();
                    } catch(Exception e) {

                    }
                    if (sr.getPower() < powerLoss && cm.getDestroyNoPower()) {
                        rm.destroySuperRegion(s, true);
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        if (rm.shouldTakeAction(event.getEntity().getLocation(), (Player) event.getEntity(), 0, "denydamage", true) ||
            rm.shouldTakeAction(event.getEntity().getLocation(), (Player) event.getEntity(), 0, "denydamagenoreagent", false)) {
            ((Player) event.getEntity()).sendMessage(ChatColor.RED + "[HeroStronghold] Damage is disabled here.");
            event.setCancelled(true);
            return;
        }
        
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
            boolean atWar = rm.isAtWar(player, dPlayer);
            boolean hasPower = sr.getPower() > 0;
            boolean hasMoney = sr.getBalance() > 0;
            boolean bothMembers = !notMember && (sr.hasMember(dPlayer.getName()) || sr.hasOwner(dPlayer.getName()));
            if (hasEffect1 || (hasEffect && reqs && hasPower && hasMoney && !atWar)) {
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
            
            if (hasEffect1 || (hasEffect && hasReagents)) {
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
    
    @EventHandler
    public void onPaintingPlace(PaintingPlaceEvent event) {
        if ((event.isCancelled() || !rm.shouldTakeAction(event.getPainting().getLocation(), event.getPlayer(), 0, "denyblockbuild", true))  &&
                (event.isCancelled() || !rm.shouldTakeAction(event.getPainting().getLocation(), event.getPlayer(), 0, "denyblockbuildnoreagent", false))) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
    }

    @EventHandler
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


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Creeper || event.getEntity() instanceof EnderDragon
                || event.getEntity() instanceof TNTPrimed || event.getEntity() instanceof Fireball)) {
            return;
        }
        if (rm.shouldTakeAction(event.getLocation(), null, 4, "denyexplosion", true) ||
                rm.shouldTakeAction(event.getLocation(), null, 4, "denyexplosionnoreagent", false)) {
            event.setCancelled(true);
            return;
        }
        
        Location loc = event.getLocation();
        ArrayList<Location> tempArray = new ArrayList<Location>();
        for (Region r : rm.getContainingRegions(loc, 4)) {
            tempArray.add(r.getLocation());
        }
        for (Location l : tempArray) {
            for (SuperRegion sr : rm.getContainingSuperRegions(l)) {
                if ((rm.getSuperRegionType(sr.getType()).hasEffect("denyexplosion") && sr.getPower() > 0 &&
                        sr.getBalance() > 0 && rm.hasAllRequiredRegions(sr)) ||
                        (rm.getSuperRegionType(sr.getType())).hasEffect("denyexplosionnoreagent")) {
                    event.setCancelled(true);
                    return;
                }
            }
            rm.destroyRegion(l);
            rm.removeRegion(l);
        }
        
    }
}