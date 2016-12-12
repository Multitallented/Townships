package multitallented.redcastlemedia.bukkit.townships.checkregiontask;

import static java.lang.Thread.sleep;

import java.io.File;
import java.util.*;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Multitallented
 */
public class CheckRegionTask implements Runnable {
    private final transient Server server;
    private final Townships hs;
    private final Set<Location> regionsToDestroy = new HashSet<Location>();
    private final HashSet<Region> regionsToCreate = new HashSet<Region>();
    private int i= 0;

    public final HashMap<String, ArrayList<Region>> lastRegion = new HashMap<String, ArrayList<Region>>();
    public final HashMap<String, ArrayList<SuperRegion>> lastSRegion = new HashMap<String, ArrayList<SuperRegion>>();

    public CheckRegionTask(Server server, Townships hs) {
        this.server = server;
        this.hs = hs;
    }
    
    public synchronized void addOrDestroyRegionToDestroy(Location l) {
        if (!regionsToDestroy.remove(l)) {
            regionsToDestroy.add(l);
        }
    }
    
    public synchronized void addRegionToCreate(Region r) {
        regionsToCreate.add(r);
    }
    
    public boolean containsRegionToDestory(Location l) {
        return regionsToDestroy.contains(l);
    }
    
    public HashSet<Region> getRegionsToCreate(Region r) {
        return regionsToCreate;
    }

    @Override
    public void run() {
        PluginManager pm = server.getPluginManager();
        if (Townships.getConfigManager().getPlayerInRegionChecks()) {
            Collection<? extends Player> players = server.getOnlinePlayers();
            int chunk = players.size() / 8;
            for (int j=chunk * i; j<(i==7 ? players.size() : chunk * (i+1)); j++) {
                try {
                    CheckPlayerInRegionThread thread = new CheckPlayerInRegionThread(this, pm, hs.getRegionManager(), (Player) players.toArray()[j]);
                    thread.go();
                    CheckPlayerInSRegionThread srThread = new CheckPlayerInSRegionThread(this, hs.getRegionManager(), (Player) players.toArray()[j]);
                    srThread.go();
                } catch (Exception e) {

                }
                Thread.yield();
            }
        }
        if (i == 7) {
            i=-1;
            pm.callEvent(new ToTwoSecondEvent());
            TriggerEffectTask triggerEffectTask = new TriggerEffectTask(hs.getRegionManager(), server.getPluginManager(), this);
            triggerEffectTask.go();
        } else {
            i++;
        }

        for (Location l : regionsToDestroy) {
            hs.getRegionManager().destroyRegion(l);
            hs.getRegionManager().removeRegion(l);
        }
        regionsToDestroy.clear();

        for (Region r : regionsToCreate) {
            hs.getRegionManager().addRegion(r.getLocation(), r.getType(), r.getOwners(), r.getMembers());
        }
        regionsToCreate.clear();


        //War Ticks
        long warTickPeriod = ((long) Townships.getConfigManager().getPowerReduceCycle()) * 60000;
        if (warTickPeriod > 0) {
            long currentTime = System.currentTimeMillis();
            HashMap<SuperRegion, Integer> newPowerMap = new HashMap<SuperRegion, Integer>();
            for (SuperRegion sr : hs.getRegionManager().getSortedSuperRegions()) {
                int currentCount = 0;
                HashSet<String> setMeMap = new HashSet<String>();
                Map<String, Long> lastTickMap = sr.getLastWarTick();
                for (String key : lastTickMap.keySet()) {
                    if (sr.getName().equals(key) || sr.getLastWarTick().get(key) < 0 || currentTime - warTickPeriod < sr.getLastWarTick().get(key)) {
                        continue;
                    }
                    setMeMap.add(key);

                    File superRegionFolder = new File(hs.getDataFolder(), "superregions");
                    File superRegionFile = new File(superRegionFolder, sr.getName() + ".yml");
                    FileConfiguration superRegionConfig = new YamlConfiguration();

                    try {
                        superRegionConfig.load(superRegionFile);
                    } catch (Exception e) {
                        System.out.println("[Townships] failed to load " + sr.getName() + ".yml war tick");
                    }

                    ConfigurationSection warSection = superRegionConfig.getConfigurationSection("wars");
                    if (warSection != null) {
                        currentCount = warSection.getInt(key + ".count") + 1;
                        warSection.set(key + ".last-tick", currentTime);
                        warSection.set(key + ".count", currentCount);
                        superRegionConfig.set("wars", warSection);
                        try {
                            superRegionConfig.save(superRegionFile);
                        } catch (Exception e) {
                            System.out.println("[Townships] failed to save " + sr.getName() + ".yml war tick");
                        }
                    }
                    int currentPower = sr.getPower();
                    if (newPowerMap.containsKey(sr)) {
                        currentPower = newPowerMap.get(sr);
                    }
                    int newPower = currentPower - currentCount * Townships.getConfigManager().getPowerReduceAdd() - Townships.getConfigManager().getPowerReduceBase();
                    newPower = newPower < 0 ? 0 : newPower;
                    newPowerMap.put(sr, newPower);
                }
                for (String key : setMeMap) {
                    lastTickMap.put(key, currentTime);
                }
                sr.setLastWarTick(lastTickMap);
            }
            for (SuperRegion sr : newPowerMap.keySet()) {
                System.out.println("[Townships] War attrition for " + sr.getName() + " set to " + newPowerMap.get(sr) + " power");
                hs.getRegionManager().setPower(sr, newPowerMap.get(sr));
            }
        }
    }
}
