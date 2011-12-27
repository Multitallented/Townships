package main.java.multitallented.plugins.herostronghold;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<String, SuperRegion> liveSuperRegions = new HashMap<String, SuperRegion>();
    private Map<String, RegionType> regionTypes = new HashMap<String, RegionType>();
    private Map<String, SuperRegionType> superRegionTypes = new HashMap<String, SuperRegionType>();
    private HeroStronghold plugin;
    private final FileConfiguration config;
    private FileConfiguration dataConfig;
    private boolean explode;
    
    public RegionManager(HeroStronghold plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        //Parse region config data
        explode = config.getBoolean("explode-on-destroy");
        //TODO implement the rest of the global config options
        
        
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(new File(plugin.getDataFolder(), "regions.yml"));
            for (String key : regionConfig.getKeys(false)) {
                ConfigurationSection currentRegion = regionConfig.getConfigurationSection(key);
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
        } catch (Exception ex) {
            plugin.warning("Unable to load regions.yml");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(new File(plugin.getDataFolder(), "super-regions.yml"));
            for (String key : regionConfig.getKeys(false)) {
                ConfigurationSection currentRegion = regionConfig.getConfigurationSection(key);
                superRegionTypes.put(key, new SuperRegionType(key, currentRegion.getStringList("effects"),
                        currentRegion.getInt("radius"),
                        processRegionTypeMap(currentRegion.getStringList("requirements")),
                        currentRegion.getDouble("money-requirement"),
                        currentRegion.getDouble("daily-money-output"),
                        currentRegion.getStringList("children"),
                        currentRegion.getInt("max-power")));
            }
        } catch (Exception e) {
            
        }

        File playerFolder = new File(plugin.getDataFolder(), "data"); // Setup the Data Folder if it doesn't already exist
        playerFolder.mkdirs();
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
                    String type = dataConfig.getString("type");
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
        
        //Load super regions
        File sRegionFolder = new File(plugin.getDataFolder(), "superregions"); // Setup the Data Folder if it doesn't already exist
        sRegionFolder.mkdirs();
        
        for (File sRegionFile : sRegionFolder.listFiles()) {
            try {
                //Load saved region data
                FileConfiguration sRegionDataConfig = new YamlConfiguration();
                sRegionDataConfig.load(sRegionFile);
                String name = sRegionFile.getName().replace(".yml", "");
                String locationString = sRegionDataConfig.getString("location");
                if (locationString != null) {
                    Location location = null;
                    if (locationString != null) {
                        String[] params = locationString.split(":");
                        World world  = plugin.getServer().getWorld(params[0]);
                        location = new Location(world, Double.parseDouble(params[1]),Double.parseDouble(params[2]),Double.parseDouble(params[3]));
                    }
                    String type = sRegionDataConfig.getString("type");
                    ArrayList<String> owners = (ArrayList<String>) sRegionDataConfig.getStringList("owners");
                    ConfigurationSection configMembers = sRegionDataConfig.getConfigurationSection("members");
                    Map<String, List<String>> members = new HashMap<String, List<String>>();
                    for (String s : configMembers.getKeys(false)) {
                        members.put(s, members.get(s));
                    }
                    int power = sRegionDataConfig.getInt("power");
                    if (location != null && type != null && owners != null) {
                        liveSuperRegions.put(name, new SuperRegion(name, location, type, owners, members, power));
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load superregions from " + sRegionFile.getName());
                System.out.println(e.getStackTrace());
            }
        }
        
    }
    
    private Map<String, Integer> processRegionTypeMap(List<String> input) {
        Map<String, Integer> tempMap = new HashMap<String, Integer>();
        for (String s : input) {
            String[] args = s.split("\\.");
            RegionType currentRegionType = getRegionType(args[0]);
            if (currentRegionType != null)
                tempMap.put(currentRegionType.getName(), Integer.parseInt(args[1]));
        }
        return tempMap;
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
    
    public boolean addSuperRegion(String name, Location loc, String type, List<String> owners, Map<String, List<String>> members, int maxpower) {
        File superRegionFolder = new File(plugin.getDataFolder() + "/superregions");
        File dataFile = new File(superRegionFolder, name + ".yml");
        if (dataFile.exists()) {
            return false;
        }
        try {
            dataFile.createNewFile();
            dataConfig = new YamlConfiguration();
            System.out.println("[HeroStronghold] saving new superregion to " + name + ".yml");
            liveSuperRegions.put(name, new SuperRegion(name, loc, type, owners, new HashMap<String, List<String>>(), maxpower));
            dataConfig.set("location", loc.getWorld().getName() + ":" + loc.getX()
                    + ":" + loc.getBlockY() + ":" + loc.getZ());
            dataConfig.set("type", type);
            dataConfig.set("owners", owners);
            dataConfig.createSection("members");
            for (String s : members.keySet()) {
                dataConfig.set("members." + s, members.get(s));
            }
            dataConfig.set("power", maxpower);
            dataConfig.save(dataFile);
            return true;
        } catch (Exception ioe) {
            System.out.println("[HeroStronghold] unable to write new superregion to file " + name + ".yml");
            ioe.printStackTrace();
            return false;
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
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            try {
                if (Math.sqrt(p.getLocation().distanceSquared(l)) < 20) {
                    p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + currentRegion.getType() + " was disabled!");
                    if (explode) {
                        p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.RED + "look out it's going to explode!");
                    }
                }
            } catch (IllegalArgumentException e) {
                
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
    
    public void destroySuperRegion(String name, boolean sendMessage) {
        SuperRegion currentRegion = liveSuperRegions.get(name);
        File dataFile = new File(plugin.getDataFolder() + "/superregions", name + ".yml");
        if (!dataFile.exists()) {
            System.out.println("[Herostronghold] Unable to destroy non-existent superregion " + name + ".yml");
            return;
        }
        if (!dataFile.delete()) {
            System.out.println("[Herostronghold] Unable to destroy non-existent superregion " + name + ".yml");
            return;
        } else {
            System.out.println("[HeroStronghold] Successfully destroyed superregion " + name + ".yml");
        }
        if (sendMessage) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + name + " was destroyed!");
            }
        }
        currentRegion.getLocation().getBlock().setTypeId(0);
        if (sendMessage) {
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.RED + name + "was destroyed!");
        }
        liveSuperRegions.remove(name);
    }
    
    public boolean shouldTakeAction(Location loc, Player player, int modifier, String effectName) {
        Effect effect = new Effect(plugin);
        for (Location l : getRegionLocations()) {
            try {
                Region r = getRegion(l);
                RegionType rt = getRegionType(r.getType());
                if (rt.getRadius() >= Math.sqrt(l.distanceSquared(loc))) {
                    if ((r.isOwner(player.getName()) || r.isMember(player.getName())) || effect.regionHasEffect(rt.getEffects(), effectName) == 0 ||
                            !effect.hasReagents(l))
                        return false;
                    return true;
                }
            } catch (IllegalArgumentException iae) {
            
            }
        }
        return false;
    }

    
    public void removeRegion(Location l) {
        if (liveRegions.containsKey(l)) {
            liveRegions.remove(l);
        }
    }
    
    public boolean hasExplode() {
        return explode;
    }
    
    public Set<String> getRegionTypes() {
        return regionTypes.keySet();
    }
    
    public Set<String> getSuperRegionTypes() {
        return superRegionTypes.keySet();
    }
    
    public RegionType getRegionType(String name) {
        return regionTypes.get(name);
    }
    
    public SuperRegionType getSuperRegionType(String name) {
        return superRegionTypes.get(name);
    }
    
    public Set<Location> getRegionLocations() {
        return liveRegions.keySet();
    }
    
    public Set<String> getSuperRegionNames() {
        return liveSuperRegions.keySet();
    }
    
    public Region getRegion(Location loc) {
        return liveRegions.get(loc);
    }
    
    public SuperRegion getSuperRegion(String name) {
        return liveSuperRegions.get(name);
    }
    
    public Map<Location, Region> getRegions() {
        return liveRegions;
    }
    
    public boolean reloadConfig() {
        //TODO make the reload functionality
        return false;
    }
}
