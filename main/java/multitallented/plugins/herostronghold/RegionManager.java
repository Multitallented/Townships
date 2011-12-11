package main.java.multitallented.plugins.herostronghold;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionManager {
    private Map<Location, Region> liveRegions = new HashMap<Location, Region>();
    private Map<String, RegionType> regionTypes = new HashMap<String, RegionType>();
    private HeroStronghold plugin;
    private final FileConfiguration config;
    private FileConfiguration dataConfig;
    
    public RegionManager(HeroStronghold plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        this.dataConfig = new YamlConfiguration();
        //Parse region config data
        ConfigurationSection regions = config.getConfigurationSection("regions");
        for (String key : regions.getKeys(false)) {
            ConfigurationSection currentRegion = regions.getConfigurationSection(key);
            regionTypes.put(key, new RegionType(key,
                    (ArrayList<String>) currentRegion.getStringList("friendly-classes"),
                    (ArrayList<String>) currentRegion.getStringList("enemy-classes"),
                    (ArrayList<String>) currentRegion.getStringList("effects"),
                    currentRegion.getInt("radius"),
                    processItemStackList(currentRegion.getStringList("requirements")),
                    processItemStackList(currentRegion.getStringList("reagents")),
                    processItemStackList(currentRegion.getStringList("upkeep")),
                    processItemStackList(currentRegion.getStringList("output")),
                    currentRegion.getDouble("upkeep-chance"),
                    currentRegion.getDouble("money-requirement"),
                    currentRegion.getDouble("upkeep-money-output")));
        }
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (dataFile.exists()) {
            try {
                //Load saved region data
                dataConfig.load(dataFile);
                ConfigurationSection saves = dataConfig.getConfigurationSection("saved-regions");
                if (saves != null) {
                    for (String key : saves.getKeys(false)) {
                        ConfigurationSection currentSave = saves.getConfigurationSection(key);
                        String locationString = currentSave.getString("location");
                        if (locationString != null) {
                            int id = Integer.parseInt(key.replace("saved-regions.", ""));
                            Location location = null;
                            if (locationString != null) {
                                String[] params = locationString.split(":");
                                World world  = plugin.getServer().getWorld(params[0]);
                                location = new Location(world, Double.parseDouble(params[1]),Double.parseDouble(params[2]),Double.parseDouble(params[3]));
                            }
                            String type = regionTypes.containsKey(currentSave.getString("type")) ? currentSave.getString("type") : null;
                            ArrayList<String> owners = (ArrayList<String>) currentSave.getStringList("owners");
                            ArrayList<String> members = (ArrayList<String>) currentSave.getStringList("members");
                            if (location != null && type != null && owners != null && members != null) {
                                liveRegions.put(location, new Region(id, location, type, owners, members));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load data.yml");
                System.out.println(e.getStackTrace());
            }
        } else {
            //Create new file for it
            try {
                dataFile.createNewFile();
                dataConfig.load(dataFile);
            } catch (IOException ioe) {
                System.out.println("[HeroStronghold] failed to create a new data file");
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load the data file");
            }
        }
        
    }
    
    private ArrayList<ItemStack> processItemStackList(List<String> input) {
        ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
        for (String current : input) {
            String[] params = current.split("\\.");
            if (Material.getMaterial(params[0]) != null) {
                returnList.add(new ItemStack(Material.getMaterial(params[0]),Integer.parseInt(params[1])));
            } else {
                plugin.warning("[HeroStronghold] could not find item " + params[0]);
            }
        }
        return returnList;
    }
    
    public void addRegion(Location loc, String type, ArrayList<String> owners) {
        String basePath = "saved-regions." + (dataConfig.getConfigurationSection("saved-regions").getKeys(false).size() + 1);
        liveRegions.put(loc, new Region(dataConfig.getConfigurationSection("saved-regions").getKeys(false).size(),
                loc, type, owners, new ArrayList<String>()));
        dataConfig.set(basePath + ".location", loc.getWorld().getName() + ":" + loc.getX()
                + ":" + loc.getBlockY() + ":" + loc.getZ());
        dataConfig.set(basePath + ".type", type);
        dataConfig.set(basePath + ".owners", owners);
        dataConfig.set(basePath + ".members", new ArrayList<String>());
        try {
            File dataFile = new File(plugin.getDataFolder(), "data.yml");
            dataConfig.save(dataFile);
        } catch (IOException ioe) {
            System.out.println("[HeroStronghold] unable to write new region to file data.yml");
            ioe.printStackTrace();
        }
    }
    
    public void destroyRegion(Location l) {
        System.out.println("destroyRegion");
        Region currentRegion = liveRegions.get(l);
        liveRegions.remove(l);
        dataConfig.createSection("saved-regions." + currentRegion.getID(), new HashMap<String, Object>());
        try {
        dataConfig.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (IOException ioe) {
            System.out.println("[HeroStronghold] unable to remove region " + currentRegion.getID() + " from file data.yml");
            ioe.printStackTrace();
        }
        if (config.getBoolean("explode-on-destroy")) {
            l.getBlock().setTypeId(46);
            if (l.getY()- 1 > 0) {
                l.getBlock().getRelative(BlockFace.DOWN).setType(Material.REDSTONE_TORCH_ON);
            } 
        }
    }
    
    public Set<String> getRegionTypes() {
        return regionTypes.keySet();
    }
    
    public RegionType getRegionType(String name) {
        return regionTypes.get(name);
    }
    
    public Set<Location> getRegionLocations() {
        return liveRegions.keySet();
    }
    
    public Region getRegion(Location loc) {
        return liveRegions.get(loc);
    }
    
    public Map<Location, Region> getRegions() {
        return liveRegions;
    }
    
    public boolean reloadConfig() {
        //TODO make the reload functionality
        return false;
    }
}
