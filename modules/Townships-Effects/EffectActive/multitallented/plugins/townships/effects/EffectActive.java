package multitallented.plugins.townships.effects;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToUpkeepEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

/**
 *
 * @author Multitallented
 */
public class EffectActive extends Effect {
    private HashMap<Region, Long> lastUpkeep = new HashMap<Region, Long>();
    private HashMap<Region, Long> lastActive = new HashMap<Region, Long>();
    
    public EffectActive(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectActive effect;
        public UpkeepListener(EffectActive effect) {
            this.effect = effect;
        }
        
        
        @EventHandler
        public void onCustomEvent(ToPlayerInRegionEvent event) {
            Region r = getPlugin().getRegionManager().getRegion(event.getLocation());
            if (!effect.isOwnerOfRegion(event.getPlayer(), r)) {
                return;
            }
            RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType()); 

            //Check if the region has the shoot arrow effect and return arrow velocity
            long activeTime = effect.regionHasEffect(rt.getEffects(), "active");
            if (activeTime == 0) {
                return;
            }
            long period = 60000;

            activeTime *= 1000;

            if (lastUpkeep.get(r) == null) {
                lastUpkeep.put(r, new Date().getTime() + period);
                return;
            } else if (lastUpkeep.get(r) > new Date().getTime()) {
                return;
            }

            lastUpkeep.put(r, new Date().getTime() + period);
            lastActive.put(r, new Date().getTime() + activeTime);
        }
        
        @EventHandler
        public void onUpkeep(ToUpkeepEvent event) {
            if (event.isCancelled()) {
                return;
            }
            Region r = getPlugin().getRegionManager().getRegion(event.getLocation());
            if (r == null) {
                return;
            }
            RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType());
            if (rt == null) {
                return;
            }
            long activeTime = effect.regionHasEffect(rt.getEffects(), "active");
            if (activeTime == 0) {
                return;
            }
            
            long lastActiveTime = 0;
            if (!lastActive.containsKey(r)) {
                lastActiveTime = getLastActive(r);
            }
            
            if (lastActiveTime == 0) {
                lastActive.put(r, new Date().getTime() + activeTime);
                saveActiveTime(r);
            }
            
            if (lastActive.containsKey(r) && lastActive.get(r) < new Date().getTime()) {
                event.setCancelled(true);
            }
        }
        
        private long getLastActive(Region r) {
            File regionFolder = new File(getPlugin().getDataFolder(), "data");
            File regionFile = new File(regionFolder, r.getID() + ".yml");
            if (!regionFile.exists()) {
                return 0;
            }
            long successes = 0;
            FileConfiguration rConfig = new YamlConfiguration();
            try {
                rConfig.load(regionFile);
                successes = rConfig.getLong("last-active", 0);
            } catch (Exception e) {
                return 0;
            }
            lastActive.put(r, successes);
            return successes;
        }
        
        private void saveActiveTime(Region r) {
            File regionFolder = new File(getPlugin().getDataFolder(), "data");
            
            File regionFile = new File(regionFolder, r.getID() + ".yml");
            FileConfiguration rConfig = new YamlConfiguration();       
            try {
                rConfig.load(regionFile);
                rConfig.set("last-active", lastActive.get(r));
                rConfig.save(regionFile);
            } catch (Exception e) {
                getPlugin().warning("[Townships] unable to save last-active in " + r.getID() + ".yml");
            }
        }
        
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            if (!event.getPlugin().getDescription().getName().equalsIgnoreCase("Townships")) {
                return;
            }
            System.out.println("[Townships] Saving all region last active times...");
            for (Region r : lastActive.keySet()) {
                saveActiveTime(r);  
            }
            System.out.println("[Townships] All region last active times saved.");           
        }   
    }   
}