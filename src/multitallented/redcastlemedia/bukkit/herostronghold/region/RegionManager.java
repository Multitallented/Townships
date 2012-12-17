package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import multitallented.redcastlemedia.bukkit.herostronghold.ConfigManager;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.PermSet;
import multitallented.redcastlemedia.bukkit.herostronghold.effect.Effect;
import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionDestroyedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.events.SuperRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.events.SuperRegionDestroyedEvent;
import org.bukkit.*;
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
    private Map<Integer, Region> idRegions = new HashMap<Integer, Region>();
    private ArrayList<Region> sortedRegions = new ArrayList<Region>();
    private Map<String, SuperRegion> liveSuperRegions = new HashMap<String, SuperRegion>();
    private ArrayList<SuperRegion> sortedSuperRegions = new ArrayList<SuperRegion>();
    private Map<String, RegionType> regionTypes = new HashMap<String, RegionType>();
    private Map<String, SuperRegionType> superRegionTypes = new HashMap<String, SuperRegionType>();
    private HeroStronghold plugin;
    private final FileConfiguration config;
    private FileConfiguration dataConfig;
    private final ConfigManager configManager;
    private HashMap<SuperRegion, HashSet<SuperRegion>> wars = new HashMap<SuperRegion, HashSet<SuperRegion>>();
    private HashMap<String, PermSet> permSets = new HashMap<String, PermSet>();
    private HashSet<String> possiblePermSets = new HashSet<String>();
    private ArrayList<Region> sortedBuildRegions = new ArrayList<Region>();
    
    
    public RegionManager(HeroStronghold plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        
        configManager = new ConfigManager(config, plugin);
        plugin.setConfigManager(configManager);
        load();
    }
    
    public void reload() {   
        load();
    }
    private void load() {
        permSets = PermSet.loadPermSets(plugin);
        for (String s : permSets.keySet()) {
            possiblePermSets.add(s);
        }
        
        File regionFolder = new File(plugin.getDataFolder(), "RegionConfig");
        if (!regionFolder.exists()) {
            File regionFile = new File(plugin.getDataFolder(), "regions.yml");
            if (!regionFile.exists()) {
                DefaultRegions.createDefaultRegionFiles(plugin);
            } else {
                DefaultRegions.migrateRegions(regionFile, plugin);
            }
        }
        for (File currentRegionFile : regionFolder.listFiles()) {
            try {
                FileConfiguration rConfig = new YamlConfiguration();
                rConfig.load(currentRegionFile);
                String regionName = currentRegionFile.getName().replace(".yml", "");
                regionTypes.put(regionName, new RegionType(regionName,
                        rConfig.getString("group", regionName),
                        (ArrayList<String>) rConfig.getStringList("friendly-classes"),
                        (ArrayList<String>) rConfig.getStringList("enemy-classes"),
                        (ArrayList<String>) rConfig.getStringList("effects"),
                        (int) Math.pow(rConfig.getInt("radius"), 2),
                        (int) Math.pow(rConfig.getInt("build-radius", rConfig.getInt("radius", 2)), 2),
                        processItemStackList(rConfig.getStringList("requirements"), currentRegionFile.getName()),
                        rConfig.getStringList("super-regions"),
                        processItemStackList(rConfig.getStringList("reagents"), currentRegionFile.getName()),
                        processItemStackList(rConfig.getStringList("upkeep"), currentRegionFile.getName()),
                        processItemStackList(rConfig.getStringList("output"), currentRegionFile.getName()),
                        rConfig.getDouble("upkeep-chance"),
                        rConfig.getDouble("money-requirement"),
                        rConfig.getDouble("upkeep-money-output"),
                        rConfig.getDouble("exp"),
                        rConfig.getString("description"),
                        rConfig.getInt("power-drain", 0),
                        rConfig.getInt("housing", 0)));
            } catch (Exception e) {
                plugin.warning("[HeroStronghold] failed to load " + currentRegionFile.getName());
                e.printStackTrace();
            }
        }
        
        File suRegionFolder = new File(plugin.getDataFolder(), "SuperRegionConfig");
        if (!suRegionFolder.exists()) {
            File sRegionFile = new File(plugin.getDataFolder(), "super-regions.yml");
            if (!sRegionFile.exists()) {
                DefaultRegions.createDefaultSuperRegionFiles(plugin);
            } else {
                DefaultRegions.migrateSuperRegions(sRegionFile, plugin);
            }
        }
        for (File currentRegionFile : suRegionFolder.listFiles()) {
            try {
                FileConfiguration rConfig = new YamlConfiguration();
                rConfig.load(currentRegionFile);
                String regionName = currentRegionFile.getName().replace(".yml", "");
                superRegionTypes.put(regionName, new SuperRegionType(regionName,
                        rConfig.getStringList("effects"),
                        (int) Math.pow(rConfig.getInt("radius"), 2),
                        processRegionTypeMap(rConfig.getStringList("requirements")),
                        rConfig.getDouble("money-requirement", 0),
                        rConfig.getDouble("money-output-daily", 0),
                        rConfig.getStringList("children"),
                        rConfig.getInt("max-power", 100),
                        rConfig.getInt("daily-power-increase", 10),
                        rConfig.getInt("charter", 0),
                        rConfig.getDouble("exp", 0),
                        rConfig.getString("central-structure"),
                        rConfig.getString("description"),
                        rConfig.getInt("population", 0)));
            } catch (Exception e) {
                plugin.warning("[HeroStronghold] failed to load " + currentRegionFile.getName());
            }
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
                        try {
                            location.getBlock().getTypeId();
                            getRegionType(type).getRadius();
                            liveRegions.put(location, new Region(Integer.parseInt(regionFile.getName().replace(".yml", "")), location, type, owners, members));

                            sortedRegions.add(liveRegions.get(location));
                            sortedBuildRegions.add(liveRegions.get(location));
                            idRegions.put(liveRegions.get(location).getID(), liveRegions.get(location));
                        } catch (NullPointerException npe) {
                            System.out.println("[HeroStronghold] failed to load data from " + regionFile.getName());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load data from " + regionFile.getName());
                System.out.println(e.getStackTrace());
            }
        }
        if (sortedRegions.size() > 1) {
            Collections.sort(sortedRegions, new Comparator<Region>() {

                @Override
                public int compare(Region o1, Region o2) {
                    return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRawRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRawRadius())));
                }
            });
        }
        if (sortedBuildRegions.size() > 1) {
            Collections.sort(sortedBuildRegions, new Comparator<Region>() {

                @Override
                public int compare(Region o1, Region o2) {
                    return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRawBuildRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRawBuildRadius())));
                }
            });
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
                        return (int) (-1 *(o1.getLocation().getX() + getSuperRegionType(o1.getType()).getRawRadius() - (o2.getLocation().getX() + getSuperRegionType(o2.getType()).getRawRadius())));
                    }
                });
            }
        }
        
        
        FileConfiguration warConfig = new YamlConfiguration();
        try {
            File warFile = new File(plugin.getDataFolder(), "war.yml");
            if (!warFile.exists()) {
                warFile.createNewFile();
            }
            warConfig.load(warFile);
            for (String key : warConfig.getKeys(false)) {
                if (!liveSuperRegions.containsKey(key)) {
                    continue;
                }
                SuperRegion sr = liveSuperRegions.get(key);
                HashSet<SuperRegion> tempSet = new HashSet<SuperRegion>();
                for (String s : warConfig.getStringList(key)) {
                    if (liveSuperRegions.containsKey(s)) {
                        tempSet.add(liveSuperRegions.get(s));
                    }
                }
                wars.put(sr, tempSet);
            }
        } catch (Exception ioe) {
            Logger log = plugin.getLogger();
            log.warning("[HeroStronghold] failed to load war.yml");
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
    
    private ArrayList<ItemStack> processItemStackList(List<String> input, String filename) {
        ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
        for (String current : input) {
            String[] params = current.split("\\.");
            if (Material.getMaterial(params[0]) != null) {
                ItemStack is;
                if (params.length < 3) {
                    is = new ItemStack(Material.getMaterial(params[0]),Integer.parseInt(params[1]));
                } else {
                    is = new ItemStack(Material.getMaterial(params[0]),Integer.parseInt(params[1]), Short.parseShort(params[2]));
                }
                returnList.add(is);
            } else {
                plugin.warning("[HeroStronghold] could not find item " + params[0] + " in " + filename);
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
            idRegions.put(i, liveRegions.get(loc));
            sortedBuildRegions.add(liveRegions.get(loc));
            if (sortedBuildRegions.size() > 1) {
                Collections.sort(sortedBuildRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRawBuildRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRawBuildRadius())));
                    }
                });
            }
            sortedRegions.add(liveRegions.get(loc));
            if (sortedRegions.size() > 1) {
                Collections.sort(sortedRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRawRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRawRadius())));
                    }
                });
            }
            plugin.getServer().getPluginManager().callEvent(new RegionCreatedEvent(liveRegions.get(loc)));
        } catch (Exception ioe) {
            System.out.println("[HeroStronghold] unable to write new region to file " + i + ".yml");
            ioe.printStackTrace();
        }
    }
    
    public void addRegion(Location loc, String type, ArrayList<String> owners, ArrayList<String> members) {
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
            dataConfig.set("members", members);
            dataConfig.save(dataFile);
            liveRegions.put(loc, new Region(i, loc, type, owners, members));
            idRegions.put(i, liveRegions.get(loc));
            sortedRegions.add(liveRegions.get(loc));
            sortedBuildRegions.add(liveRegions.get(loc));
            if (sortedBuildRegions.size() > 1) {
                Collections.sort(sortedBuildRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRawBuildRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRawBuildRadius())));
                    }
                });
            }
            if (sortedRegions.size() > 1) {
                Collections.sort(sortedRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRawRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRawRadius())));
                    }
                });
            }
            plugin.getServer().getPluginManager().callEvent(new RegionCreatedEvent(liveRegions.get(loc)));
        } catch (Exception ioe) {
            System.out.println("[HeroStronghold] unable to write new region to file " + i + ".yml");
            ioe.printStackTrace();
        }
    }
    
    public boolean addSuperRegion(String name, Location loc, String type, List<String> owners, Map<String, List<String>> members, int power, double balance) {
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
            dataConfig.set("balance", balance);
            dataConfig.save(dataFile);
            liveSuperRegions.put(name, new SuperRegion(name, loc, type, owners, members, power, 0.0, 0.0, new LinkedList<Double>()));
            
            sortedSuperRegions.add(liveSuperRegions.get(name));
            
            if (sortedSuperRegions.size() > 1) {
                if (sortedSuperRegions.size() > 1) {
                    Collections.sort(sortedSuperRegions, new Comparator<SuperRegion>() {

                        @Override
                        public int compare(SuperRegion o1, SuperRegion o2) {
                            return (int) (-1 *(o1.getLocation().getX() + getSuperRegionType(o1.getType()).getRawRadius() - (o2.getLocation().getX() + getSuperRegionType(o2.getType()).getRawRadius())));
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
        
        plugin.getServer().getPluginManager().callEvent(new RegionDestroyedEvent(currentRegion));
        if (configManager.getExplode()) {
            l.getBlock().setTypeId(0);
            TNTPrimed tnt = l.getWorld().spawn(l, TNTPrimed.class); 
            tnt.setFuseTicks(1);
            
        }
        l.getBlock().setTypeId(0);
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
            final String regionName = name;
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.RED + regionName + " was destroyed!");
                  }
            }.run();
        }
        removeWars(name);
        liveSuperRegions.remove(name);
        sortedSuperRegions.remove(currentRegion);
        Bukkit.getPluginManager().callEvent(new SuperRegionDestroyedEvent(currentRegion));
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
    
    public ArrayList<Region> getContainedRegions(SuperRegion sr) {
        ArrayList<Region> tempRegions = new ArrayList<Region>();
        Location loc = sr.getLocation();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        int radius = getSuperRegionType(sr.getType()).getRawRadius();
        for (Region r : getSortedBuildRegions()) {	  	
            Location l = r.getLocation();  	
            if (l.getX() + radius < x) { 	
                break;	  	
            }
            
            if (l.getX() - radius < x && l.getY() + radius > y && l.getY() - radius < y && 
                    l.getZ() + radius > z && l.getZ() - radius < z &&
                    l.getWorld().equals(loc.getWorld())) {
                tempRegions.add(r);
            }
        }
        
        return tempRegions;
    }
    
    public boolean hasAllRequiredRegions(SuperRegion sr) {
        Location loc = sr.getLocation();
        SuperRegionType srt = getSuperRegionType(sr.getType());
        Map<String, Integer> reqs = new HashMap<String, Integer>();
        for (String s : srt.getRequirements().keySet()) {
            reqs.put(new String(s), new Integer(srt.getRequirement(s)));
        }
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        int radius = getSuperRegionType(sr.getType()).getRawRadius();
        for (Region r : getSortedRegions()) {	  	
            Location l = r.getLocation();  	
            if (l.getX() + radius < x) { 	
                break;	  	
            }
            
            if (l.getX() - radius < x && l.getY() + radius > y && l.getY() - radius < y && 
                    l.getZ() + radius > z && l.getZ() - radius < z && l.getWorld().equals(loc.getWorld()) && reqs.containsKey(getRegionType(r.getType()).getGroup())) {
                String group = getRegionType(r.getType()).getGroup();
                if (reqs.get(group) < 2) {
                    reqs.remove(group);
                } else {
                    reqs.put(group, reqs.get(group) - 1);
                }
            }
        }
        if (reqs.isEmpty()) {
            return true;
        }
        return false;
    }
    
    public synchronized void reduceRegion(SuperRegion sr) {
        ConfigManager cm = HeroStronghold.getConfigManager();
        if (!cm.getUsePower()) {
            return;
        }
        int powerLoss = cm.getPowerPerKill();
        int currentPower = sr.getPower() - powerLoss;
        currentPower = currentPower > 0 ? currentPower : 0;
        final String st = sr.getName();
        if (currentPower < 26 && sr.getPower() > 25) {
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " reached 25 power! Destruction is near!");
                  }
            }.run();
        } else if (currentPower < 11 && sr.getPower() > 10) {
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " reached 10 power! Destruction is at hand!");
                  }
            }.run();
        } else if (currentPower < 1) {
            new Runnable() {
                  @Override
                  public void run()
                  {
                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " reached 0 power!");
                  }
            }.run();
        }
        setPower(sr, currentPower);
    }
    
    public synchronized void setPower(SuperRegion sr, int newPower) {
        ConfigManager cm = HeroStronghold.getConfigManager();
        if (!cm.getUsePower()) {
            return;
        }
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
    
    public void setPrimaryOwner(Region r, String name) {
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
            owners.add(0, name);
        } else {
            owners.add(0, name);
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
        y = y < 0 ? 0 : y;
        y = y > loc.getWorld().getMaxHeight() ? loc.getWorld().getMaxHeight() : y;
        double z = loc.getZ();
        for (Region r : getSortedRegions()) {
            try {
                int radius = getRegionType(r.getType()).getRawRadius();
                Location l = r.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
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
    
    public ArrayList<Region> getContainingRegions(Location loc, int modifier) {
        ArrayList<Region> tempList = new ArrayList<Region>();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        for (Region r : getSortedRegions()) {
            try {
                int radius = getRegionType(r.getType()).getRawRadius() + modifier;
                Location l = r.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
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
    
    public ArrayList<Region> getContainingBuildRegions(Location loc) {
        ArrayList<Region> tempList = new ArrayList<Region>();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        for (Region r : sortedBuildRegions) {
            try {
                int radius = getRegionType(r.getType()).getRawBuildRadius();
                radius = radius < 1 ? getRegionType(r.getType()).getRawRadius() : radius;
                Location l = r.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
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
    
    public ArrayList<Region> getContainingBuildRegions(Location loc, int modifier) {
        ArrayList<Region> tempList = new ArrayList<Region>();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        for (Region r : sortedBuildRegions) {
            try {
                int radius = getRegionType(r.getType()).getRawBuildRadius() + modifier;
                radius = radius < 1 ? getRegionType(r.getType()).getRawRadius() : radius;
                Location l = r.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
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
    
    public Region getClosestRegionType(Location loc, String type) {
        Region re = null;
        double distance = 999999999;
        for (Region r : getSortedRegions()) {
            try {
                Location l = r.getLocation();
                if (r.getType().equalsIgnoreCase(type) &&
                    l.getWorld().equals(loc.getWorld())) {
                    double tempDistance=r.getLocation().distance(loc);
                    if (tempDistance < distance) {
                        distance=tempDistance;
                        re=r;
                    } else {
                        break;
                    }
                    
                }
            } catch (NullPointerException npe) {
                plugin.warning("Region " + r.getID() + " is corrupted");
            }
        }
        return re;
    }
    
    public ArrayList<SuperRegion> getContainingSuperRegions(Location loc) {
        ArrayList<SuperRegion> tempList = new ArrayList<SuperRegion>();
        
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        for (SuperRegion sr : getSortedSuperRegions()) {
            try {
                int radius = getSuperRegionType(sr.getType()).getRawRadius();
                Location l = sr.getLocation();
                if (l.getX() + radius < x) {
                    break;
                }
                if (l.getX() - radius < x && l.getY() + radius > y && l.getY() - radius < y && 
                        l.getZ() + radius > z && l.getZ() - radius < z && l.getWorld().equals(loc.getWorld())) {
                    tempList.add(sr);
                }
            } catch (NullPointerException npe) {
                plugin.warning("SuperRegion " + sr.getName() + " is corrupted");
            }
        }
        return tempList;
    }
    
    @Deprecated
    public boolean shouldTakeAction(Location loc, Player player, int modifier, String effectName, boolean useReagents) {
        Effect effect = new Effect(plugin);
        for (Region r : this.getContainingRegions(loc, modifier)) {
            boolean nullPlayer = player == null;
            boolean member = false;
            if (!nullPlayer) {
                if ((r.isMember(player.getName()) || r.isOwner(player.getName()))) {
                    member = true;
                } else if (r.isMember("all")) {
                    member = true;
                } else  {
                    for (String s : r.getMembers()) {
                        if (s.contains("sr:")) {
                            SuperRegion sr = getSuperRegion(s.replace("sr:", ""));
                            if (sr != null && (sr.hasMember(player.getName()) || sr.hasOwner(player.getName()))) {
                                member = true;
                            }
                        }
                    }
                }
            }
            if (!useReagents && (nullPlayer || !member) && effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0) {
                return true;
            }
            if (useReagents && (nullPlayer || !member) && effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0
                    && effect.hasReagents(r.getLocation())) {
                return true;
            }
        }
        for (SuperRegion sr : this.getContainingSuperRegions(loc)) {
            boolean nullPlayer = player == null;
            boolean member = false;
            if (!nullPlayer) {
                member = (sr.hasOwner(player.getName()) || sr.hasMember(player.getName()));
            }
            boolean reqs = hasAllRequiredRegions(sr);
            boolean hasEffect = getSuperRegionType(sr.getType()).hasEffect(effectName);
            boolean hasPower = sr.getPower() > 0;
            boolean hasMoney = sr.getBalance() > 0;
            if (useReagents && (nullPlayer || !member) && hasEffect && reqs && hasPower && hasMoney) {
                return true;
            }
            if (!useReagents && (nullPlayer || !member) && hasEffect) {
                return true;
            }
        }
        return false;
    }
    
    public boolean shouldTakeAction(Location loc, Player player, RegionCondition condition) {
        int modifier = condition.MODIFIER;
        boolean useReagents = condition.USE_REAGENTS;
        String effectName = condition.NAME;
        Effect effect = new Effect(plugin);
        for (Region r : this.getContainingRegions(loc, modifier)) {
            boolean nullPlayer = player == null;
            boolean member = false;
            if (!nullPlayer) {
                if ((r.isMember(player.getName()) || r.isOwner(player.getName()))) {
                    member = true;
                } else if (r.isMember("all")) {
                    member = true;
                } else  {
                    for (String s : r.getMembers()) {
                        if (s.contains("sr:")) {
                            SuperRegion sr = getSuperRegion(s.replace("sr:", ""));
                            if (sr != null && (sr.hasMember(player.getName()) || sr.hasOwner(player.getName()))) {
                                member = true;
                            }
                        }
                    }
                }
            }
            if (!useReagents && (nullPlayer || !member) && effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0) {
                return true;
            }
            if (useReagents && (nullPlayer || !member) && effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0
                    && effect.hasReagents(r.getLocation())) {
                return true;
            }
        }
        for (SuperRegion sr : this.getContainingSuperRegions(loc)) {
            boolean nullPlayer = player == null;
            boolean member = false;
            if (!nullPlayer) {
                member = (sr.hasOwner(player.getName()) || sr.hasMember(player.getName()));
            }
            boolean reqs = hasAllRequiredRegions(sr);
            boolean hasEffect = getSuperRegionType(sr.getType()).hasEffect(effectName);
            boolean hasPower = sr.getPower() > 0;
            boolean hasMoney = sr.getBalance() > 0;
            if (useReagents && (nullPlayer || !member) && hasEffect && reqs && hasPower && hasMoney) {
                return true;
            }
            if (!useReagents && (nullPlayer || !member) && hasEffect) {
                return true;
            }
        }
        return false;
    }
    
    public boolean shouldTakeAction(Location loc, Player player, ArrayList<RegionCondition> conditions) {
        Effect effect = new Effect(plugin);
        HashMap<Integer, ArrayList<RegionCondition>> conditionJA = new HashMap<Integer, ArrayList<RegionCondition>>();
        for (RegionCondition rc : conditions) {
            if (conditionJA.containsKey(rc.MODIFIER) && conditionJA.get(rc.MODIFIER) != null && !conditionJA.get(rc.MODIFIER).isEmpty()) {
                conditionJA.put(rc.MODIFIER, new ArrayList<RegionCondition>());
                conditionJA.get(rc.MODIFIER).add(rc);
            } else {
                conditionJA.get(rc.MODIFIER).add(rc);
            }
        }
        for (Integer i : conditionJA.keySet()) {
            for (Region r : this.getContainingRegions(loc, i)) {
                boolean nullPlayer = player == null;
                boolean member = false;
                if (!nullPlayer) {
                    if ((r.isMember(player.getName()) || r.isOwner(player.getName()))) {
                        member = true;
                    } else if (r.isMember("all")) {
                        member = true;
                    } else  {
                        for (String s : r.getMembers()) {
                            if (s.contains("sr:")) {
                                SuperRegion sr = getSuperRegion(s.replace("sr:", ""));
                                if (sr != null && (sr.hasMember(player.getName()) || sr.hasOwner(player.getName()))) {
                                    member = true;
                                }
                            }
                        }
                    }
                }
                for (RegionCondition rc : conditionJA.get(i)) {
                    boolean useReagents = rc.USE_REAGENTS;
                    String effectName = rc.NAME;
                    if (!useReagents && (nullPlayer || !member) && effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0) {
                        return true;
                    }
                    if (useReagents && (nullPlayer || !member) && effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) != 0
                            && effect.hasReagents(r.getLocation())) {
                        return true;
                    }
                }
            }
            for (SuperRegion sr : this.getContainingSuperRegions(loc)) {
                boolean nullPlayer = player == null;
                boolean member = false;
                if (!nullPlayer) {
                    member = (sr.hasOwner(player.getName()) || sr.hasMember(player.getName()));
                }
                boolean reqs = hasAllRequiredRegions(sr);
                boolean hasPower = sr.getPower() > 0;
                boolean hasMoney = sr.getBalance() > 0;
                
                for (RegionCondition rc : conditionJA.get(i)) {
                    String effectName = rc.NAME;
                    boolean useReagents = rc.USE_REAGENTS;
                    boolean hasEffect = getSuperRegionType(sr.getType()).hasEffect(effectName);
                    if (useReagents && (nullPlayer || !member) && hasEffect && reqs && hasPower && hasMoney) {
                        return true;
                    }
                    if (!useReagents && (nullPlayer || !member) && hasEffect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void setWar(SuperRegion sr1, SuperRegion sr2) {
        File warFile = new File(plugin.getDataFolder(), "war.yml");
        try {
            if (!warFile.exists()) {
                System.out.println("[HeroStronghold] failed to load war.yml");
                return;
            }
        } catch (Exception e) {
            System.out.println("[HeroStronghold] failed to load war.yml");
            return;
        }
        FileConfiguration warConfig = new YamlConfiguration();
        try {
            warConfig.load(warFile);
        } catch (Exception e) {
            System.out.println("[HeroStronghold] failed to load war.yml");
            return;
        }
        if (hasWar(sr1, sr2)) {
            try {
                if (wars.containsKey(sr1)) {
                    List<String> tempList = warConfig.getStringList(sr1.getName());
                    tempList.remove(sr2.getName());
                    warConfig.set(sr1.getName(), tempList);
                    warConfig.save(warFile);
                    if (tempList.isEmpty()) {
                        wars.remove(sr1);
                    } else {
                        wars.get(sr1).remove(sr2);
                    }
                } else if (wars.containsKey(sr2)) {
                    List<String> tempList = warConfig.getStringList(sr2.getName());
                    tempList.remove(sr1.getName());
                    warConfig.set(sr2.getName(), tempList);
                    warConfig.save(warFile);
                    if (tempList.isEmpty()) {
                        wars.remove(sr2);
                    } else {
                        wars.get(sr2).remove(sr1);
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to remove war from war.yml");
                return;
            }
        } else {
            try {
                if (wars.containsKey(sr1)) {
                    List<String> tempList = warConfig.getStringList(sr1.getName());
                    tempList.add(sr2.getName());
                    warConfig.set(sr1.getName(), tempList);
                    warConfig.save(warFile);
                    wars.get(sr1).add(sr2);
                } else if (wars.containsKey(sr2)) {
                    List<String> tempList = warConfig.getStringList(sr2.getName());
                    tempList.add(sr1.getName());
                    warConfig.set(sr2.getName(), tempList);
                    warConfig.save(warFile);
                    wars.get(sr2).add(sr1);
                } else {
                    ArrayList<String> tempSet = new ArrayList<String>();
                    HashSet<SuperRegion> tempSet2 = new HashSet<SuperRegion>();
                    tempSet.add(sr2.getName());
                    tempSet2.add(sr2);
                    warConfig.set(sr1.getName(), tempSet);
                    warConfig.save(warFile);
                    wars.put(sr1, tempSet2);
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to save new war to war.yml");
                return;
            }
        }
    }

    
    public void removeRegion(Location l) {
        if (liveRegions.containsKey(l)) {
            idRegions.remove(liveRegions.get(l).getID());
            sortedRegions.remove(liveRegions.get(l));
            sortedBuildRegions.remove(liveRegions.get(l));
            liveRegions.remove(l);
        }
    }
    
    public Region getRegionByID(int id) {
        return idRegions.get(id);
    }
    
    public Set<String> getRegionTypes() {
        return regionTypes.keySet();
    }
    
    public ArrayList<Region> getSortedRegions() {
        return sortedRegions;
    }
    
    public ArrayList<Region> getSortedBuildRegions() {
        return sortedBuildRegions;
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
    
    public boolean hasWar(SuperRegion sr1, SuperRegion sr2) {
        for (SuperRegion sr : wars.keySet()) {
            if (sr1.equals(sr)) {
                for (SuperRegion srt : wars.get(sr)) {
                    if (srt.equals(sr2)) {
                        return true;
                    }
                }
            } else if (sr2.equals(sr)) {
                for (SuperRegion srt : wars.get(sr)) {
                    if (srt.equals(sr1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isAtWar(Player p, Player p1) {
        String playername = p.getName();
        String dPlayername = p1.getName();
        HashSet<SuperRegion> tempSet = new HashSet<SuperRegion>();
        HashSet<SuperRegion> dTempSet = new HashSet<SuperRegion>();
        for (SuperRegion sr : getSortedSuperRegions()) {
            if (sr.hasMember(playername) || sr.hasOwner(playername)) {
                tempSet.add(sr);
            } else if (sr.hasMember(dPlayername) || sr.hasOwner(dPlayername)) {
                dTempSet.add(sr);
            }
        }
        for (SuperRegion sr : tempSet) {
            for (SuperRegion srt : dTempSet) {
                if (hasWar(sr, srt)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public HashSet<SuperRegion> getWars(SuperRegion sr) {
        HashSet<SuperRegion> tempSet = new HashSet<SuperRegion>();
        if (wars.containsKey(sr)) {
            tempSet = wars.get(sr);
        }
        for (SuperRegion srt : wars.keySet()) {
            if (!srt.equals(sr)) {
                for (SuperRegion srr : wars.get(srt)) {
                    if (srr.equals(sr)) {
                        tempSet.add(srt);
                        break;
                    }
                }
            }
        }
        return tempSet;
    }

    public void removeWars(String name) {
        FileConfiguration warConfig = new YamlConfiguration();
        File warFile = new File(plugin.getDataFolder(), "war.yml");
        try {
            warConfig.load(warFile);
        } catch (Exception e) {
            System.out.println("[HeroStronghold] Failed to load war.yml");
        }
        SuperRegion sr = liveSuperRegions.get(name);
        if (wars.containsKey(sr)) {
            wars.remove(sr);
            warConfig.set(name, new ArrayList<String>());
        }
        
        for (SuperRegion srt : wars.keySet()) {
            if (!srt.equals(sr)) {
                for (SuperRegion srr : wars.get(srt)) {
                    if (srr.equals(sr)) {
                        if (wars.get(srt).size() < 2) {
                            wars.remove(srt);
                            warConfig.set(srt.getName(), new ArrayList<String>());
                        } else {
                            wars.get(srt).remove(srr);
                            ArrayList<String> tempList = new ArrayList<String>();
                            for (SuperRegion ts : wars.get(srt)) {
                                tempList.add(ts.getName());
                            }
                            warConfig.set(srt.getName(), tempList);
                        }
                        break;
                    }
                }
            }
        }
        
        try {
            warConfig.save(warFile);
        } catch (Exception e) {
            System.out.println("[HeroStronghold] Failed to save war.yml");
        }
    }
    
    public boolean isAtMaxRegions(Player p, RegionType rt) {
        //Find permSet
        ArrayList<String> sets = getPermSets(p);
        PermSet ps = null;
        int highestPriority = -9999999;
        for (String s : sets) {
            if (permSets.containsKey(s) && permSets.get(s).getPriority() > highestPriority) {
                ps = permSets.get(s);
                highestPriority = ps.getPriority();
            }
        }
        
        //Find total regions of that type
        int max = 9999999;
        try {
            max = ps.getPerms().get(rt.getName());
        } catch (NullPointerException npe) {
            return false;
        }
        if (max < 1) {
            return true;
        }
        
        //Find all regions of that type and count them
        int i = 0;
        for (Region r : sortedRegions) {
            if (!r.getType().equals(rt.getName()) || !r.isOwner(p.getName())) {
                continue;
            }
            i++;
            if (max <= i) {
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<String> getPermSets(Player p) {
        ArrayList<String> sets = new ArrayList<String>();
        for (String s : possiblePermSets) {
            if (HeroStronghold.perms.has(p, "herostronghold.group." + s)) {
                sets.add(s);
            }
        }
        return sets;
    }
    
    public boolean canBuildHere(Player p, Location l) {
        String playername = p.getName();
        Effect effect = new Effect(plugin);
        for (Region r : getContainingRegions(l)) {
            if (r.isMember(playername) || r.isOwner(playername)) {
                continue;
            } else if ((effect.regionHasEffect(r, "denyblockbuild") != 0 && effect.hasReagents(r.getLocation())) ||
                    effect.regionHasEffect(r, "denyblockbuildnoreagent") != 0) {
                return false;
            }
        }
        for (SuperRegion sr : getContainingSuperRegions(l)) {
            SuperRegionType srt = getSuperRegionType(sr.getType());
            if (sr.hasMember(playername) || sr.hasOwner(playername)) {
                continue;
            } else if (srt.hasEffect("denyblockbuild") || srt.hasEffect("denyblockbuildnoreagent")) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean canBreakHere(Location l, Player p) {
        String playername = p.getName();
        Effect effect = new Effect(plugin);
        for (Region r : getContainingRegions(l)) {
            if (r.isMember(playername) || r.isOwner(playername)) {
                continue;
            } else if ((effect.regionHasEffect(r, "denyblockbreak") != 0 && effect.hasReagents(r.getLocation())) ||
                    effect.regionHasEffect(r, "denyblockbreaknoreagent") != 0) {
                return false;
            }
        }
        for (SuperRegion sr : getContainingSuperRegions(l)) {
            SuperRegionType srt = getSuperRegionType(sr.getType());
            if (sr.hasMember(playername) || sr.hasOwner(playername)) {
                continue;
            } else if (srt.hasEffect("denyblockbreak") || srt.hasEffect("denyblockbreaknoreagent")) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasAvailableHousing(SuperRegion sr) {
        int housing = 0;
        for (Region r : getContainedRegions(sr)) {
            housing += getRegionType(r.getType()).getHousing();
        }
        return housing > sr.getPopulation();
    }
}
