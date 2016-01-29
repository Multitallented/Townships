package multitallented.plugins.townships.effects;

import java.util.ArrayList;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEvent;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author Multitallented
 */
public class EffectAntiCamp extends Effect {
    private final Townships plugin;
    public EffectAntiCamp(Townships plugin) {
        super(plugin);
        this.plugin = plugin;
        registerEvent(new IntruderListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class IntruderListener implements Listener {
        private final EffectAntiCamp effect;
//        private final HashMap<String, Integer> deathCounts = new HashMap<String, Integer>();
        private final HashMap<String, String> lastDamager = new HashMap<String, String>();
        private final HashMap<String, String> lastDeathTown = new HashMap<String, String>();
        private final HashMap<String, Long> lastPoison = new HashMap<String, Long>();
        private final HashMap<String, ArrayList<Long>> lastDeath = new HashMap<String, ArrayList<Long>>();

        public IntruderListener(EffectAntiCamp effect) {
            this.effect = effect;
        }

        @EventHandler
        public void onDamage(EntityDamageByEntityEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
                return;
            }

            Entity damager = event.getDamager();
            if (event.getCause() == DamageCause.PROJECTILE) {
                ProjectileSource shooter = ((Projectile)damager).getShooter();
                if (!(shooter instanceof Entity)) {
                    return;
                }
                damager = (Entity) shooter;
            }
            if (!(damager instanceof Player)) {
                return;
            }

            Player damagee = (Player) event.getEntity();
            Player damager2 = (Player) damager;

            //record damage for when they die
            lastDamager.put(damager2.getName(), damagee.getName());
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            String playerName = event.getPlayer().getName();

            if (lastDeathTown.containsKey(playerName)) {
                lastDeathTown.remove(playerName);
            }
            if (lastDamager.containsKey(playerName)) {
                lastDamager.remove(playerName);
            }

//            if (deathCounts.containsKey(playerName)) {
//                deathCounts.remove(playerName);
//            }
            if (lastDeath.containsKey(playerName)) {
                lastDeath.remove(playerName);
            }
        }

        @EventHandler
        public void onDeath(PlayerDeathEvent event) {
            Player player = event.getEntity();
            RegionManager rm = getPlugin().getRegionManager();

            //Remove killer from lastDamager
            if (lastDamager.containsKey(player.getName())) {

                //remove the killer from deathCounts
                if (lastDeath.containsKey(lastDamager.get(player.getName()))) {
                    lastDeath.remove(lastDamager.get(player.getName()));
                }
                lastDamager.remove(player.getName());
            }

            //if the person who's dying has died more than twice, then I don't care
//            if (lastDeath.containsKey(player.getName()) && lastDeath.get(player.getName()).size() > 2) {
//                return;
//            }

            //If they died outside of a super region then I don't care
            ArrayList<SuperRegion> superRegions = rm.getContainingSuperRegions(player.getLocation());
            if (superRegions.isEmpty()) {
                return;
            }

            for (SuperRegion sr : superRegions) {
                SuperRegionType srt = rm.getSuperRegionType(sr.getType());
                if (srt == null) {
                    continue;
                }

                boolean hasEffect = false;
                for (String s : srt.getEffects()) {
                    String[] parts = s.split("\\.");
                    if (parts.length > 1 && parts[0].equalsIgnoreCase("anticamp")) {
                        hasEffect = true;
                    }
                }
                if (!hasEffect) {
                    continue;
                }

                //If the person dying was a member, then increment their deathCount
                if (sr.hasOwner(player.getName()) || sr.hasMember(player.getName())) {

                    //Don't count deaths in a previous town
                    if (lastDeathTown.containsKey(player.getName()) &&
                            lastDeathTown.get(player.getName()) != null &&
                            !lastDeathTown.get(player.getName()).equals(sr.getName())) {
                        lastDeath.remove(player.getName());
                    }

                    //if the person hasn't died yet then add to lastDeath
                    if (!lastDeath.containsKey(player.getName())) {
                        lastDeath.put(player.getName(), new ArrayList<Long>());
                        lastDeath.get(player.getName()).add(System.currentTimeMillis());
                    } else {
                        lastDeath.get(player.getName()).add(System.currentTimeMillis());
                    }
                    if (lastDeath.get(player.getName()).size() > 3) {
                        lastDeath.get(player.getName()).remove(0);
                    }

//                    if (deathCounts.containsKey(player.getName())) {
//                        deathCounts.put(player.getName(), deathCounts.get(player.getName()) + 1);
//                    } else {
//                        deathCounts.put(player.getName(), 1);
//                    }
                    lastDeathTown.put(player.getName(), sr.getName());
                    return;
                }
            }
        }

        @EventHandler
        public void onTwoSeconds(ToTwoSecondEvent event) {
            RegionManager rm = plugin.getRegionManager();

            //Activate poison
            ArrayList<String> removeMeDeathCounts = new ArrayList<String>();

            //Go through everyone who has died in their own town
            for (String name : lastDeath.keySet()) {

                //Cleanup
                {
                    ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
                    int i = 0;
                    for (Long deathTime : lastDeath.get(name)) {
                        if (deathTime + 600000 < System.currentTimeMillis()) {
                            removeIndexes.add(i);
                        }
                        i++;
                    }
                    for (Integer j : removeIndexes) {
                        lastDeath.get(name).remove((int) j);
                    }
                }

                //Skip people who haven't died enough
                if (lastDeath.get(name).size() < 3 || !lastDeathTown.containsKey(name)) {
                    continue;
                }

                removeMeDeathCounts.add(name);
                SuperRegion sr = rm.getSuperRegion(lastDeathTown.get(name));
                lastDeathTown.remove(name);
                if (sr == null) {
                    continue;
                }
                SuperRegionType srt = rm.getSuperRegionType(sr.getType());
                if (srt == null) {
                    return;
                }
                long period = 0;
                boolean hasEffect = false;
                for (String s : srt.getEffects()) {
                    String[] parts = s.split("\\.");
                    if (parts.length > 2 && parts[0].equalsIgnoreCase("anticamp")) {
                        try {
                            period = Integer.parseInt(parts[2]);
                        } catch (Exception e) {
                            break;
                        }
                        hasEffect = true;
                        break;
                    }
                }
                if (!hasEffect || period < 1) {
                    continue;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.WHITE + "[Townships] " + ChatColor.RED + sr.getName() + ChatColor.WHITE + "'s anti-camp has been " + ChatColor.ITALIC + "triggered.");
                }
                lastPoison.put(sr.getName(), System.currentTimeMillis() + (period * 1000));
            }
            for (String s : removeMeDeathCounts) {
                lastDeath.remove(s);
            }

            //Deal Poison Damage
            ArrayList<String> removeMePoison = new ArrayList<String>();
            for (String srName : lastPoison.keySet()) {

                Long lastPoisonTime = lastPoison.get(srName);

                if (lastPoisonTime == null || System.currentTimeMillis() > lastPoisonTime) {
                    removeMePoison.add(srName);
                    continue;
                }

                SuperRegion sr = rm.getSuperRegion(srName);
                if (sr == null) {
                    continue;
                }
                SuperRegionType srt = rm.getSuperRegionType(sr.getType());
                if (srt == null) {
                    continue;
                }

                int damage = 1;

                boolean hasEffect = false;
                for (String s : srt.getEffects()) {
                    String[] parts = s.split("\\.");
                    if (parts.length > 1 && parts[0].equalsIgnoreCase("anticamp")) {
                        try {
                            damage = Integer.parseInt(parts[1]);
                        } catch (Exception e) {
                            break;
                        }
                        hasEffect = true;
                        break;
                    }
                }
                if (!hasEffect) {
                    continue;
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (SuperRegion r : rm.getContainingSuperRegions(p.getLocation())) {
                        if (!lastPoison.containsKey(r.getName())) {
                            break;
                        }

                        if (sr.hasMember(p.getName()) || sr.hasOwner(p.getName())) {
                            break;
                        }

                        p.damage(damage);
                    }
                }
            }
            for (String s : removeMePoison) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.WHITE + "[Townships] " + ChatColor.RED + s + ChatColor.WHITE + "'s anti-camp has expired.");
                }
                lastPoison.remove(s);
            }
        }
    }
}
