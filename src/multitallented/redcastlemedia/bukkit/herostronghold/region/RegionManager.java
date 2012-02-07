package multitallented.redcastlemedia.bukkit.herostronghold.region;

import multitallented.redcastlemedia.bukkit.herostronghold.effect.Effect;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.ConfigManager;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionDestroyedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.events.SuperRegionCreatedEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionManager {
    private Map<Location, Region> liveRegions = new HashMap<Location, Region>();
    private ArrayList<Region> sortedRegions = new ArrayList<Region>();
    private Map<String, SuperRegion> liveSuperRegions = new HashMap<String, SuperRegion>();
    private ArrayList<SuperRegion> sortedSuperRegions = new ArrayList<SuperRegion>();
    private Map<String, RegionType> regionTypes = new HashMap<String, RegionType>();
    private Map<String, SuperRegionType> superRegionTypes = new HashMap<String, SuperRegionType>();
    private HeroStronghold plugin;
    private final FileConfiguration config;
    private FileConfiguration dataConfig;
    private final ConfigManager configManager;
    
    
    public RegionManager(HeroStronghold plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        
        configManager = new ConfigManager(config, plugin);
        plugin.setConfigManager(configManager);
        
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            File regionFile = new File(plugin.getDataFolder(), "regions.yml");
            if (!regionFile.exists()) {
                InputStream defRegionConfigStream = plugin.getResource("regions.yml");
                if (defRegionConfigStream != null) {
                    FileConfiguration defRegionConfig = YamlConfiguration.loadConfiguration(defRegionConfigStream);
                    regionConfig.setDefaults(defRegionConfig);
                }
                regionConfig.options().copyDefaults(true);
                regionFile.createNewFile();
                defRegionConfigStream.close();
            }
            regionConfig.load(regionFile);
            for (String key : regionConfig.getKeys(false)) {
                ConfigurationSection currentRegion = regionConfig.getConfigurationSection(key);
                regionTypes.put(key, new RegionType(key,
                        (ArrayList<String>) currentRegion.getStringList("friendly-classes"),
                        (ArrayList<String>) currentRegion.getStringList("enemy-classes"),
                        (ArrayList<String>) currentRegion.getStringList("effects"),
                        (int) Math.pow(currentRegion.getInt("radius"), 2),
                        (int) Math.pow(currentRegion.getInt("build-radius", currentRegion.getInt("radius")), 2),
                        processItemStackList(currentRegion.getStringList("requirements")),
                        currentRegion.getStringList("super-regions"),
                        processItemStackList(currentRegion.getStringList("reagents")),
                        processItemStackList(currentRegion.getStringList("upkeep")),
                        processItemStackList(currentRegion.getStringList("output")),
                        currentRegion.getDouble("upkeep-chance"),
                        currentRegion.getDouble("money-requirement"),
                        currentRegion.getDouble("upkeep-money-output"),
                        currentRegion.getDouble("exp")));
            }
            regionConfig.save(regionFile);
        } catch (Exception ex) {
            plugin.warning("Unable to load regions.yml");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            File sRegionFile = new File(plugin.getDataFolder(), "super-regions.yml");
            if (!sRegionFile.exists()) {
                InputStream defSRegionConfigStream = plugin.getResource("super-regions.yml");
                if (defSRegionConfigStream != null) {
                    FileConfiguration defSRegionConfig = YamlConfiguration.loadConfiguration(defSRegionConfigStream);
                    sRegionConfig.setDefaults(defSRegionConfig);
                }
                sRegionConfig.options().copyDefaults(true);
                sRegionFile.createNewFile();
                defSRegionConfigStream.close();
            }
            sRegionConfig.load(sRegionFile);
            for (String key : sRegionConfig.getKeys(false)) {
                ConfigurationSection currentRegion = sRegionConfig.getConfigurationSection(key);
                superRegionTypes.put(key, new SuperRegionType(key, currentRegion.getStringList("effects"),
                        (int) Math.pow(currentRegion.getInt("radius"), 2),
                        processRegionTypeMap(currentRegion.getStringList("requirements")),
                        currentRegion.getDouble("money-requirement", 0),
                        currentRegion.getDouble("money-output-daily", 0),
                        currentRegion.getStringList("children"),
                        currentRegion.getInt("max-power", 100),
                        currentRegion.getInt("daily-power-increase", 10),
                        currentRegion.getInt("charter", 0),
                        currentRegion.getDouble("exp", 0),
                        currentRegion.getString("central-structure")));
            }
            sRegionConfig.save(sRegionFile);
        } catch (Exception e) {
            plugin.warning("Unable to load super-regions.yml");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        File playerFolder = new File(plugin.getDataFolder(), "data"); // Setup the Data Folder if it doesn't already exist
        playerFolder.mkdirs();
        for (File regionFile : playerFolder.listFiles()) {
            try {
                //Load saved region data
                dataConfig = new YamlConfiguration();
                dataConfig.load(regionFile);
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
                    if (owners == null) {
                        owners = new ArrayList<String>();
                    }
                    if (members == null) {
                        members = new ArrayList<String>();
                    }
                    if (location != null && type != null) {
                        liveRegions.put(location, new Region(Integer.parseInt(regionFile.getName().replace(".yml", "")), location, type, owners, members));
                        
                        sortedRegions.add(liveRegions.get(location));
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load data from " + regionFile.getName());
                System.out.println(e.getStackTrace());
            }
        }
        if (sortedRegions.size() > 1) {
            if (sortedRegions.size() > 1) {
                Collections.sort(sortedRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRadius())));
                    }
                });
            }
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
                String locationString = sRegionDataConfig.getString("location", "0:64:0");
                if (locationString != null) {
                    Location location = null;
                    if (locationString != null) {
                        String[] params = locationString.split(":");
                        World world  = plugin.getServer().getWorld(params[0]);
                        location = new Location(world, Double.parseDouble(params[1]),Double.parseDouble(params[2]),Double.parseDouble(params[3]));
                    }
                    String type = sRegionDataConfig.getString("type", "shack");
                    ArrayList<String> owners = (ArrayList<String>) sRegionDataConfig.getStringList("owners");
                    ConfigurationSection configMembers = sRegionDataConfig.getConfigurationSection("members");
                    Map<String, List<String>> members = new HashMap<String, List<String>>();
                    for (String s : configMembers.getKeys(false)) {
                        List<String> perm = configMembers.getStringList(s);
                        if (perm.contains("member")) {
                            members.put(s, configMembers.getStringList(s));
                        }
                    }
                    int power = sRegionDataConfig.getInt("power", 10);
                    double taxes = sRegionDataConfig.getDouble("taxes", 0.0);
                    double balance = sRegionDataConfig.getDouble("balance", 0.0);
                    List<Double> taxRevenue1 = sRegionDataConfig.getDoubleList("tax-revenue");
                    LinkedList<Double> taxRevenue = new LinkedList<Double>();
                    if (taxRevenue1 != null) {
                        for (double d : taxRevenue1) {
                            taxRevenue.add(d);
                        }
                    }
                    if (location != null && type != null) {
                        liveSuperRegions.put(name, new SuperRegion(name, location, type, owners, members, power, taxes, balance, taxRevenue));
                        
                        sortedSuperRegions.add(liveSuperRegions.get(name));
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load superregions from " + sRegionFile.getName());
                e.printStackTrace();
            }
        }
        if (sortedSuperRegions.size() > 1) {
            if (sortedSuperRegions.size() > 1) {
                Collections.sort(sortedSuperRegions, new Comparator<SuperRegion>() {

                    @Override
                    public int compare(SuperRegion o1, SuperRegion o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getSuperRegionType(o1.getType()).getRadius() - (o2.getLocation().getX() + getSuperRegionType(o2.getType()).getRadius())));
                    }
                });
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
            
            
            dataConfig.set("location", loc.getWorld().getName() + ":" + loc.getX()
                    + ":" + loc.getBlockY() + ":" + loc.getZ());
            dataConfig.set("type", type);
            dataConfig.set("owners", owners);
            dataConfig.set("members", new ArrayList<String>());
            dataConfig.save(dataFile);
            liveRegions.put(loc, new Region(i, loc, type, owners, new ArrayList<String>()));
            sortedRegions.add(liveRegions.get(loc));
            if (sortedRegions.size() > 1) {
                Collections.sort(sortedRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRadius())));
                    }
                });
            }
            plugin.getServer().getPluginManager().callEvent(new RegionCreatedEvent(loc));
        } catch (Exception ioe) {
            System.out.println("[HeroStronghold] unable to write new region to file " + i + ".yml");
            ioe.printStackTrace();
        }
    }
    
    public boolean addSuperRegion(String name, Location loc, String type, List<String> owners, Map<String, List<String>> members, int power) {
        File superRegionFolder = new File(plugin.getDataFolder() + "/superregions");
        File dataFile = new File(superRegionFolder, name + ".yml");
        if (dataFile.exists()) {
            return false;
        }
        try {
            dataFile.createNewFile();
            dataConfig = new YamlConfiguration();
            System.out.println("[HeroStronghold] saving new superregion to " + name + ".yml");
            
            dataConfig.set("location", loc.getWorld().getName() + ":" + loc.getX()
                    + ":" + loc.getBlockY() + ":" + loc.getZ());
            dataConfig.set("type", type);
            dataConfig.set("owners", owners);
            dataConfig.createSection("members");
            for (String s : members.keySet()) {
                dataConfig.set("members." + s, members.get(s));
            }
            dataConfig.set("power", power);
            dataConfig.save(dataFile);
            liveSuperRegions.put(name, new SuperRegion(name, loc, type, owners, members, power, 0.0, 0.0, new LinkedList<Double>()));
            
            sortedSuperRegions.add(liveSuperRegions.get(name));
            
            if (sortedSuperRegions.size() > 1) {
                if (sortedSuperRegions.size() > 1) {
                    Collections.sort(sortedSuperRegions, new Comparator<SuperRegion>() {

                        @Override
                        public int compare(SuperRegion o1, SuperRegion o2) {
                            return (int) (-1 *(o1.getLocation().getX() + getSuperRegionType(o1.getType()).getRadius() - (o2.getLocation().getX() + getSuperRegionType(o2.getType()).getRadius())));
                        }
                    });
                }
            }
            plugin.getServer().getPluginManager().callEvent(new SuperRegionCreatedEvent(name));
            return true;
        } catch (Exception ioe) {
            System.out.println("[HeroStronghold] unable to write new superregion to file " + name + ".yml");
            ioe.printStackTrace();
            return false;
        }
    }
    
    public void destroyRegion(Location l) {
        ////////////////////////////////////////////
        //Note: this method does not remove the region due to Concurrent Modification Exception
        //You have to do that separately with removeRegion(Location l);
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
        final String regionTypeName = currentRegion.getType();
        final Location threadL = l;
        new Runnable()
        {
          @Override
          public void run()
          {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                try {
                    if (p.getLocation().distanceSquared(threadL) < 400) {
                        p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + regionTypeName + " was disabled!");
                        if (configManager.getExplode()) {
                            p.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.RED + "look out it's going to explode!");
                        }
                    }
                } catch (IllegalArgumentException e) {

                }
            }
          }
        }.run();
        
        plugin.getServer().getPluginManager().callEvent(new RegionDestroyedEvent(l));
        if (configManager.getExplode()) {
            l.getBlock().setTypeId(0);
            TNTPrimed tnt = l.getWorld().spawn(l, TNTPrimed.class); 
            tnt.setFuseTicks(1);
            
        }
        l.getBlock().setTypeId(0);
    }
    
    public void destroySuperRegion(String name, boolean sendMessage) {
        //This method does remove the SuperRegion from liveSuperRegions
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
        
        if (sendMessage) {
            final String regionName = name;
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.RED + regionName + "was destroyed!");
                  }
            }.run();
        }
        liveSuperRegions.remove(name);
        sortedSuperRegions.remove(currentRegion);
    }
    
    public void checkIfDestroyedSuperRegion(Location loc) {
        Set<String> regionsToDestroy = new HashSet<String>();
        Region or = getRegion(loc);
        for (SuperRegion sr : this.getContainingSuperRegions(loc)) {
            if (or.getType().equals(getSuperRegionType(sr.getType()).getCentralStructure())) {
                regionsToDestroy.add(sr.getName());
            }
        }
        
        for (String s : regionsToDestroy) {
            destroySuperRegion(s, true);
        }
    }
    
    public boolean hasAllRequiredRegions(SuperRegion sr) {
        Location loc = sr.getLocation();
        SuperRegionType srt = getSuperRegionType(sr.getType());
        Map<String, Integer> reqs = new HashMap<String, Integer>();
        for (String s : srt.getRequirements().keySet()) {
            reqs.put(new String(s), new Integer(srt.getRequirement(s)));
        }
        for (Region r : this.getContainingRegions(loc)) {
            if (reqs.containsKey(r.getType())) {
                if (reqs.get(r.getType()) < 2) {
                    reqs.remove(r.getType());
                } else {
                    reqs.put(r.getType(), reqs.get(r.getType()) - 1);
                }
            }
        }
        if (reqs.isEmpty()) {
            return true;
        }
        return false;
    }
    
    public synchronized void reduceRegion(SuperRegion sr) {
        int currentPower = sr.getPower() - 1;
        setPower(sr, currentPower);
        final String st = sr.getName();
        if (currentPower == 25) {
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " reached 25 power! Destruction is near!");
                  }
            }.run();
        } else if (currentPower == 10) {
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " reached 10 power! Destruction is at hand!");
                  }
            }.run();
        } else if (currentPower <= 0) {
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " reached 0 power!");
                  }
            }.run();
        }
    }
    
    public synchronized void setPower(SuperRegion sr, int newPower) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to find file " + sr.getName() + ".yml");
            return;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to save new Power");
            return;
        }
        sRegionConfig.set("power", newPower);
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return;
        }
        sr.setPower(newPower);
    }
    
    /**
     * Adds a double to the taxRevenue record for the SuperRegion
     */
    public synchronized void addTaxRevenue(SuperRegion sr, double input) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to find file " + sr.getName() + ".yml");
            return;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to save new taxRevenue");
            return;
        }
        sr.addTaxRevenue(input);
        LinkedList<Double> taxRevenue = sr.getTaxRevenue();
        sRegionConfig.set("tax-revenue", taxRevenue);
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return;
        }
    }
    
    public void setTaxes(SuperRegion sr, double taxes) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to find file " + sr.getName() + ".yml");
            return;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to save new taxes");
            return;
        }
        sRegionConfig.set("taxes", taxes);
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return;
        }
        sr.setTaxes(taxes);
    }
    
    public void setMember(SuperRegion sr, String name, List<String> input) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to find file " + sr.getName() + ".yml");
            return;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to save member");
            return;
        }
        if (sr.hasMember(name)) {
            sr.remove(name);
        }
        sr.addMember(name, input);
        sRegionConfig.set("members." + name, input);
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return;
        }
    }
    
    public void removeMember(SuperRegion sr, String name) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to find file " + sr.getName() + ".yml");
            return;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to remove member " + name);
            return;
        }
        sr.remove(name);
        sRegionConfig.set("members." + name, new ArrayList<String>());
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return;
        }
    }
    
    public void setOwner(SuperRegion sr, String name) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to find file " + sr.getName() + ".yml");
            return;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to save owner");
            return;
        }
        List<String> owners = sr.getOwners();
        if (owners.contains(name)) {
            owners.remove(name);
        } else {
            owners.add(name);
        }
        sRegionConfig.set("owners", owners);
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return;
        }
    }
    
    public void setMember(Region r, String name) {
        Player p = plugin.getServer().getPlayer(name);
        if (p != null) {
            name = p.getName();
        }
        File regionFile = new File(plugin.getDataFolder() + "/data", r.getID() + ".yml");
        if (!regionFile.exists()) {
            plugin.warning("Failed to find file " + r.getID() + ".yml");
            return;
        }
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(regionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + r.getID() + ".yml to save member");
            return;
        }
        ArrayList<String> members = r.getMembers();
        if (members.contains(name)) {
            members.remove(name);
        } else {
            members.add(name);
        }
        regionConfig.set("members", members);
        try {
            regionConfig.save(regionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + r.getID() + ".yml");
            return;
        }
    }
    
    public void setOwner(Region r, String name) {
        File regionFile = new File(plugin.getDataFolder() + "/data", r.getID() + ".yml");
        if (!regionFile.exists()) {
            plugin.warning("Failed to find file " + r.getID() + ".yml");
            return;
        }
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(regionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + r.getID() + ".yml to save owner");
            return;
        }
        List<String> owners = r.getOwners();
        if (owners.contains(name)) {
            owners.remove(name);
        } else {
            owners.add(name);
        }
        regionConfig.set("owners", owners);
        try {
            regionConfig.save(regionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + r.getID() + ".yml");
            return;
        }
    }
    
    /**
     * Adds (or subtracts if negative) the balance from the super-region.
     * It saves that data to sr.getName() + ".yml".
     * If the new balance would be less than 0, it takes the remainder and
     * subtracts that from the owner's balance.
     */
    public synchronized double addBalance(SuperRegion sr, double balance) {
        File superRegionFile = new File(plugin.getDataFolder() + "/superregions", sr.getName() + ".yml");
        if (!superRegionFile.exists()) {
            plugin.warning("Failed to save " + sr.getName() + " new bank balance: " + balance);
            return 0;
        }
        FileConfiguration sRegionConfig = new YamlConfiguration();
        try {
            sRegionConfig.load(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to load " + sr.getName() + ".yml to save new bank balance");
            return 0;
        }
        double newBalance = balance + sr.getBalance();
        if (balance < 0) {
            if (newBalance < 0 && HeroStronghold.econ != null) {
                String ownerName = sr.getOwners().get(0);
                double ownerBalance = HeroStronghold.econ.bankBalance(ownerName).balance;
                if (newBalance + ownerBalance <= 0 && ownerBalance != 0) {
                    HeroStronghold.econ.withdrawPlayer(ownerName, ownerBalance);
                    Player p = plugin.getServer().getPlayer(ownerName);
                    if (p != null && p.isOnline()) {
                        p.sendMessage(ChatColor.RED + "[HeroStronghold] " + sr.getName() + " and you are out of money. Do something fast!");
                    }
                } else {
                    HeroStronghold.econ.withdrawPlayer(ownerName, -newBalance);
                }
                
            }
        }
        sRegionConfig.set("balance", newBalance);
        sr.setBalance(newBalance);
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return 0;
        }
        
        return newBalance;
    }
    
    public ArrayList<Region> getContainingRegions(Location loc) {
        ArrayList<Region> tempList = new ArrayList<Region>();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        for (Region r : getSortedRegions()) {
            try {
                int radius = getRegionType(r.getType()).getRawRadius();
                Location l = r.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
                //System.out.println("x: " + (l.getX() - radius) + " - " + (l.getX() + radius) + " : " + x);
                //.out.println("y: " + (l.getY() - radius) + " - " + (l.getY() + radius) + " : " + y);
                //System.out.println("z: " + (l.getZ() - radius) + " - " + (l.getZ() + radius) + " : " + z);
                if (l.getX() - radius < x && l.getY() + radius > y && l.getY() - radius < y && 
                        l.getZ() + radius > z && l.getZ() - radius < z && l.getWorld().equals(loc.getWorld())) {
                    tempList.add(r);
                }
            } catch (NullPointerException npe) {
                plugin.warning("Region " + r.getID() + " is corrupted");
            }
        }
        return tempList;
    }
    
    public ArrayList<SuperRegion> getContainingSuperRegions(Location loc) {
        ArrayList<SuperRegion> tempList = new ArrayList<SuperRegion>();
        
        double x = loc.getX();
        for (SuperRegion sr : getSortedSuperRegions()) {
            try {
                int radius = getSuperRegionType(sr.getType()).getRadius();
                Location l = sr.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
                try {
                    if (!(l.getX() - radius > x) && l.distanceSquared(loc) < radius) {
                        tempList.add(sr);
                    }
                } catch (IllegalArgumentException iae) {

                }
            } catch (NullPointerException npe) {
                plugin.warning("SuperRegion " + sr.getName() + " is corrupted");
            }
        }
        return tempList;
    }
    
    public boolean shouldTakeAction(Location loc, Player player, int modifier, String effectName, boolean useReagents) {
        Effect effect = new Effect(plugin);
        for (Region r : this.getContainingRegions(loc)) {
            if (!useReagents && (player == null || (!r.isOwner(player.getName()) && !r.isMember(player.getName()))) && 
                    effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0) {
                return true;
            }
            if (useReagents && (player == null || (!r.isOwner(player.getName()) && !r.isMember(player.getName()))) && 
                    effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0 && effect.hasReagents(r.getLocation())) {
                return true;
            }
        }
        for (SuperRegion sr : this.getContainingSuperRegions(loc)) {
            boolean nullPlayer = player == null;
            boolean notMember = nullPlayer;
            if (!notMember) {
                notMember = !(sr.hasOwner(player.getName()) || sr.hasMember(player.getName()));
            }
            boolean reqs = hasAllRequiredRegions(sr);
            boolean hasEffect = getSuperRegionType(sr.getType()).hasEffect(effectName);
            boolean hasPower = sr.getPower() > 0;
            boolean hasMoney = sr.getBalance() > 0;
            if (useReagents && notMember && hasEffect && reqs && hasPower && hasMoney) {
            /*if ((player == null || (!sr.hasOwner(player.getName()) && !sr.hasMember(player.getName())))
                    && getSuperRegionType(sr.getType()).hasEffect(effectName) && hasAllRequiredRegions(sr)) {*/
                return true;
            }
            if (!useReagents && notMember && hasEffect) {
                return true;
            }
        }
        return false;
    }

    
    public void removeRegion(Location l) {
        if (liveRegions.containsKey(l)) {
            sortedRegions.remove(liveRegions.get(l));
            liveRegions.remove(l);
        }
    }
    
    public Set<String> getRegionTypes() {
        return regionTypes.keySet();
    }
    
    public ArrayList<Region> getSortedRegions() {
        return sortedRegions;
    }
    
    public ArrayList<SuperRegion> getSortedSuperRegions() {
        return sortedSuperRegions;
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
}
