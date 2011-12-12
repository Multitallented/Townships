package main.java.multitallented.plugins.herostronghold;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
        int i = 0;
        File dataFile = new File(plugin.getDataFolder() + "/data", i + ".yml");
        while (dataFile.exists()) {
            try {
                //Load saved region data
                dataConfig = new YamlConfiguration();
                dataConfig.load(dataFile);
                String locationString = dataConfig.getString("location");
                if (locationString != null) {
                    Location location = null;
                    if (locationString != null) {
                        String[] params = locationString.split(":");
                        World world  = plugin.getServer().getWorld(params[0]);
                        location = new Location(world, Double.parseDouble(params[1]),Double.parseDouble(params[2]),Double.parseDouble(params[3]));
                    }
                    String type = regionTypes.containsKey(dataConfig.getString("type")) ? dataConfig.getString("type") : null;
                    ArrayList<String> owners = (ArrayList<String>) dataConfig.getStringList("owners");
                    ArrayList<String> members = (ArrayList<String>) dataConfig.getStringList("members");
                    if (location != null && type != null && owners != null && members != null) {
                        liveRegions.put(location, new Region(i, location, type, owners, members));
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load data from " + i + ".yml");
                System.out.println(e.getStackTrace());
            }
            i++;
            dataFile = new File(plugin.getDataFolder() + "/data", i + ".yml");
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
        int i = 0;
        File dataFile = new File(plugin.getDataFolder() + "/data", i + ".yml");
        while (dataFile.exists()) {
            i++;
            dataFile = new File(plugin.getDataFolder() + "/data", i + ".yml");
        }
        try {
            dataFile.createNewFile();
            dataConfig = new YamlConfiguration();
            System.out.println("[HeroStronghold] saving new region to " + i + ".yml");
            //dataConfig.load(dataFile);
            liveRegions.put(loc, new Region(i, loc, type, owners, new ArrayList<String>()));
            dataConfig.set("location", loc.getWorld().getName() + ":" + loc.getX()
                    + ":" + loc.getBlockY() + ":" + loc.getZ());
            dataConfig.set("type", type);
            dataConfig.set("owners", owners);
            dataConfig.set("members", new ArrayList<String>());
            dataConfig.save(dataFile);
        } catch (Exception ioe) {
            System.out.println("[HeroStronghold] unable to write new region to file " + i + ".yml");
            ioe.printStackTrace();
        }
    }
    
    public void destroyRegion(Location l) {
        Region currentRegion = liveRegions.get(l);
        File dataFile = new File(plugin.getDataFolder() + "/data", currentRegion.getID() + ".yml");
        if (!dataFile.exists()) {
            System.out.println("[Herostronghold] Unable to destroy non-existent region " + currentRegion.getID() + ".yml");
            return;
        }
        if (!dataFile.delete()) {
            System.out.println("[Herostronghold] Unable to destroy non-existent region " + currentRegion.getID() + ".yml");
            return;
        } else {
            System.out.println("[HeroStronghold] Successfully destroyed region " + currentRegion.getID() + ".yml");
        }
        boolean explode = config.getBoolean("explode-on-destroy");
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (Math.sqrt(p.getLocation().distanceSquared(l)) < 20) {
                p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + currentRegion.getType() + " was disabled!");
                if (explode) {
                    p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.RED + "look out it's going to explode!");
                }
            }
        }
        if (explode) {
            l.getBlock().setTypeId(46);
            if (l.getY()- 1 > 0) {
                l.getBlock().getRelative(BlockFace.DOWN).setType(Material.REDSTONE_TORCH_ON);
            } 
        } else {
            l.getBlock().setTypeId(0);
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
