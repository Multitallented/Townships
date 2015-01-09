package multitallented.plugins.townships.effects;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToSuperRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEffectEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondSREffectEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 * @author Phoenix_Frenzy
 */
public class EffectTemporary extends Effect {
    private HashMap<Location, Long> lastUpkeep = new HashMap<Location, Long>();
    private HashMap<String, Long> lastSRUpkeep = new HashMap<String, Long>();
    
    public EffectTemporary(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectTemporary effect;
        public UpkeepListener(EffectTemporary effect) {
            this.effect = effect;
        }
        
        @EventHandler
        public void onRegionCreatedEvent(ToRegionCreatedEvent event) {
            Region r = event.getRegion();
            RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType());
            
            if (rt == null) {
                return;
            }
            
            for (String s : rt.getEffects()) {
                if (s.startsWith("temporary")) {
                    long period = 0;
                    String[] effectParts = s.split("\\.");
                    if (effectParts.length < 2) {
                        return;
                    }
                    period = Long.parseLong(effectParts[1]) * 1000;
                    if (period == 0) {
                        return;
                    }
                    long created = new Date().getTime() + period;
                    File regionFolder = new File(getPlugin().getDataFolder(), "data");
                    File regionFile = new File(regionFolder, r.getID() + ".yml");
                    FileConfiguration rConfig = new YamlConfiguration();       
                    try {
                        rConfig.load(regionFile);
                        rConfig.set("created", created);
                        rConfig.save(regionFile);
                    } catch (Exception e) {
                        getPlugin().warning("[Townships] unable to save created in " + r.getID() + ".yml");
                        return;
                    }
                    
                    lastUpkeep.put(r.getLocation(), created);
                }
            }
        }
        
        @EventHandler
        public void onSuperRegionCreatedEvent(ToSuperRegionCreatedEvent event) {
            SuperRegion sr = getPlugin().getRegionManager().getSuperRegion(event.getName());
            if (sr == null) {
                return;
            }
            RegionType srt = getPlugin().getRegionManager().getRegionType(sr.getType());
            if (srt == null) {
                return;
            }
            
            for (String s : srt.getEffects()) {
                if (s.startsWith("temporary")) {
                    long period = 0;
                    String[] effectParts = s.split("\\.");
                    if (effectParts.length < 2) {
                        return;
                    }
                    period = Long.parseLong(effectParts[1]) * 1000;
                    if (period == 0) {
                        return;
                    }
                    
                    long created = new Date().getTime() + period;
                    
                    //TODO save file here
                    File regionFolder = new File(getPlugin().getDataFolder(), "superregions");
                    File regionFile = new File(regionFolder, sr.getName() + ".yml");
                    FileConfiguration rConfig = new YamlConfiguration();       
                    try {
                        rConfig.load(regionFile);
                        rConfig.set("created", created);
                        rConfig.save(regionFile);
                    } catch (Exception e) {
                        getPlugin().warning("[Townships] unable to save created in " + sr.getName() + ".yml");
                        return;
                    }
                    lastSRUpkeep.put(sr.getName(), created);
                }
            }
        }
        
        @EventHandler
        public void onCustomEvent(ToTwoSecondEffectEvent event) {
            if (event.getEffect().length < 2 || !event.getEffect()[0].equals("temporary")) {
                return;
            }
            Region r = event.getRegion();
            Location l = r.getLocation();
            RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType());

            //Check if the region has effect temporary
            long period = 0;
            period = Long.parseLong(event.getEffect()[1]) * 1000;

            if (period == 0) {
                return;
            }

            Long lastUp = lastUpkeep.get(l);

            if (lastUp == null) {
                //TODO read from file
                File regionFolder = new File(getPlugin().getDataFolder(), "data");
                File regionFile = new File(regionFolder, r.getID() + ".yml");
                if (!regionFile.exists()) {
                    return;
                }
                long created = 0;
                FileConfiguration rConfig = new YamlConfiguration();
                try {
                    rConfig.load(regionFile);
                    created = rConfig.getLong("created", 0);
                } catch (Exception e) {
                    return;
                }

                if (created == 0) {
                    created = System.currentTimeMillis() + period;
                }

                lastUpkeep.put(r.getLocation(), created);
                lastUp = created;
            }
            
            if (period + lastUp > System.currentTimeMillis()) {
                return;
            }
            event.getRegionsToDestroy().add(r.getLocation());
        }

        @EventHandler
        public void onSuperRegionEffectEvent(ToTwoSecondSREffectEvent event) {
            if (event.getEffect().length < 2 || !event.getEffect()[0].equals("temporary")) {
                return;
            }
            SuperRegion sr = event.getSuperRegion();
            Location l = sr.getLocation();
            SuperRegionType srt = getPlugin().getRegionManager().getSuperRegionType(sr.getType());

            //Check if the region has effect temporary
            long period = 0;
            period = Long.parseLong(event.getEffect()[1]) * 1000;

            if (period == 0) {
                return;
            }

            Long lastUp = lastSRUpkeep.get(sr.getName());

            if (lastUp == null) {
                File regionFolder = new File(getPlugin().getDataFolder(), "superregions");
                File regionFile = new File(regionFolder, sr.getName() + ".yml");
                if (!regionFile.exists()) {
                    return;
                }
                long created = 0;
                FileConfiguration rConfig = new YamlConfiguration();
                try {
                    rConfig.load(regionFile);
                    created = rConfig.getLong("created", 0);
                } catch (Exception e) {
                    return;
                }
                if (lastUp == 0) {
                    return;
                }
                lastSRUpkeep.put(sr.getName(), created);
            }
            
            if (period + lastUp > new Date().getTime()) {
                return;
            }
            getPlugin().getRegionManager().destroySuperRegion(sr.getName(), true);
        }
    }   
}