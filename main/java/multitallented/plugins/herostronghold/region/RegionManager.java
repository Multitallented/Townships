package main.java.multitallented.plugins.herostronghold.region;

import main.java.multitallented.plugins.herostronghold.effect.Effect;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.java.multitallented.plugins.herostronghold.ConfigManager;
import main.java.multitallented.plugins.herostronghold.HeroStronghold;
import main.java.multitallented.plugins.herostronghold.events.RegionDestroyedEvent;
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
        
        
        //TODO add get set methods for global settings
        //TODO implement global settings in listeners
        //TODO write to super-region file when editing power
        
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(new File(plugin.getDataFolder(), "regions.yml"));
            for (String key : regionConfig.getKeys(false)) {
                ConfigurationSection currentRegion = regionConfig.getConfigurationSection(key);
                regionTypes.put(key, new RegionType(key,
                        (ArrayList<String>) currentRegion.getStringList("friendly-classes"),
                        (ArrayList<String>) currentRegion.getStringList("enemy-classes"),
                        (ArrayList<String>) currentRegion.getStringList("effects"),
                        (int) Math.pow(currentRegion.getInt("radius"), 2),
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
                        (int) Math.pow(currentRegion.getInt("radius"), 2),
                        processRegionTypeMap(currentRegion.getStringList("requirements")),
                        currentRegion.getDouble("money-requirement", 0),
                        currentRegion.getDouble("daily-money-output", 0),
                        currentRegion.getStringList("children"),
                        currentRegion.getInt("max-power", 100),
                        currentRegion.getInt("daily-power-increase", 10),
                        currentRegion.getInt("charter", 0)));
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
                        /*int sort = (int) location.getX() + regionTypes.get(type).getRadius();
                        float k = sortedRegions.size() / 2;
                        int j = (int) k;
                        while (k >= 0.5f) {
                            k = k / 2;
                            j = sortedRegions.get(j).getLocation().getX() + regionTypes.get(sortedRegions.get(j).getType()).getRadius() < sort ? Math.round(j + k) : Math.round(j - k);
                        }*/
                        sortedRegions.add(liveRegions.get(location));
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load data from " + i + ".yml");
                System.out.println(e.getStackTrace());
            }
            i++;
            dataFile = new File(plugin.getDataFolder() + "/data", i + ".yml");
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
                    int power = sRegionDataConfig.getInt("power", 10);
                    double taxes = sRegionDataConfig.getDouble("taxes", 0.0);
                    double balance = sRegionDataConfig.getDouble("balance", 0.0);
                    List<Double> taxRevenue1 = sRegionDataConfig.getDoubleList("tax-revenue");
                    LinkedList<Double> taxRevenue = new LinkedList<Double>();
                    for (double d : taxRevenue1) {
                        taxRevenue.add(d);
                    }
                    if (location != null && type != null && owners != null) {
                        liveSuperRegions.put(name, new SuperRegion(name, location, type, owners, members, power, taxes, balance, taxRevenue));
                        /*int sort = (int) location.getX() + superRegionTypes.get(type).getRadius();
                        float k = sortedSuperRegions.size() / 2;
                        int j = (int) k;
                        while (k >= 0.5f) {
                            k = k / 2;
                            j = sortedSuperRegions.get(j).getLocation().getX() + superRegionTypes.get(sortedSuperRegions.get(j).getType()).getRadius() < sort ? Math.round(j + k) : Math.round(j - k);
                        }*/
                        sortedSuperRegions.add(liveSuperRegions.get(name));
                    }
                }
            } catch (Exception e) {
                System.out.println("[HeroStronghold] failed to load superregions from " + sRegionFile.getName());
                System.out.println(e.getStackTrace());
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
            liveRegions.put(loc, new Region(i, loc, type, owners, new ArrayList<String>()));
            /*int sort = (int) loc.getX() + getRegionType(type).getRadius();
            float k = sortedRegions.size() / 2;
            int j = (int) k;
            while (k >= 0.5f) {
                k = k / 2;
                j = sortedRegions.get(j).getLocation().getX() + getRegionType(sortedRegions.get(j).getType()).getRadius() < sort ? Math.round(j + k) : Math.round(j - k);
            }*/
            sortedRegions.add(liveRegions.get(loc));
            if (sortedRegions.size() > 1) {
                Collections.sort(sortedRegions, new Comparator<Region>() {

                    @Override
                    public int compare(Region o1, Region o2) {
                        return (int) (-1 *(o1.getLocation().getX() + getRegionType(o1.getType()).getRadius() - (o2.getLocation().getX() + getRegionType(o2.getType()).getRadius())));
                    }
                });
            }
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
            liveSuperRegions.put(name, new SuperRegion(name, loc, type, owners, new HashMap<String, List<String>>(), maxpower, 0.0, 0.0, new LinkedList<Double>()));
            /*int sort = (int) loc.getX() + superRegionTypes.get(type).getRadius();
            float k = sortedSuperRegions.size() / 2;
            int j = (int) k;
            while (k >= 0.5f) {
                k = k / 2;
                j = sortedSuperRegions.get(j).getLocation().getX() + superRegionTypes.get(sortedSuperRegions.get(j).getType()).getRadius() < sort ? Math.round(j + k) : Math.round(j - k);
            }*/
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
            l.getBlock().setTypeId(46);
            if (l.getY()- 1 > 0) {
                l.getBlock().getRelative(BlockFace.DOWN).setType(Material.REDSTONE_TORCH_ON);
            } 
        } else {
            l.getBlock().setTypeId(0);
        }
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
        
        double x1 = loc.getX();
        for (SuperRegion sr : getSortedSuperRegions()) {
            int radius = getRegionType(sr.getType()).getRadius();
            Location l = sr.getLocation();
            if (l.getX() + radius < x1) {
                break;
            }
            try {
                if (l.getX() - radius > x1 && l.distanceSquared(loc) < radius) {
                    SuperRegionType srt = getSuperRegionType(sr.getType());
                    String rt = srt.getName();
                    int required = srt.getRequirement(rt);
                    
                    double x = loc.getX();
                    for (Region r : getSortedRegions()) {
                        int radius1 = getRegionType(r.getType()).getRadius();
                        Location rl = r.getLocation();
                        if (rl.getX() + radius1 < x) {
                            return;
                        }
                        try {
                            if (rl.getX() - radius1 > x && rl.distanceSquared(loc) < radius1 && getRegion(rl).getType().equals(rt)) {
                                required--;
                                if (required <= 0)
                                    break;
                            }
                        } catch (IllegalArgumentException iae) {

                        }
                    }
                    if (required > 0) {
                        regionsToDestroy.add(sr.getName());
                    }
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
        
        /*for (String s : getSuperRegionNames()) {
            SuperRegion sr = getSuperRegion(s);
            Location loc = sr.getLocation();
            SuperRegionType srt = getSuperRegionType(sr.getType());
            String rt = getRegionType(getRegion(l).getType()).getName();
            int radius = srt.getRadius();
            try {
                int required = srt.getRequirement(rt);
                if (required != 0 && Math.sqrt(loc.distanceSquared(l)) < radius) {
                    
                    outer: for (Location rl : getRegionLocations()) {
                        try {
                            if (getRegion(rl).getType().equals(rt) && Math.sqrt(rl.distanceSquared(loc)) < radius) {
                                required--;
                                if (required <= 0)
                                    break outer;
                            }
                        } catch (IllegalArgumentException iae) {
                            
                        }
                    }
                    if (required > 0) {
                        regionsToDestroy.add(s);
                    }
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }*/
        for (String s : regionsToDestroy) {
            destroySuperRegion(s, true);
        }
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
        try {
            sRegionConfig.save(superRegionFile);
        } catch (Exception e) {
            plugin.warning("Failed to save " + sr.getName() + ".yml");
            return 0;
        }
        
        return newBalance;
    }
    
    public boolean shouldTakeAction(Location loc, Player player, int modifier, String effectName) {
        Effect effect = new Effect(plugin);
        double x = player.getLocation().getX();
        for (Region r : getSortedRegions()) {
            int radius = getRegionType(r.getType()).getRadius();
            Location l = r.getLocation();
            if (l.getX() + radius < x) {
                return false;
            }
            try {
                if (l.getX() - radius > x && l.distanceSquared(player.getLocation()) < radius) {
                    if ((r.isOwner(player.getName()) || r.isMember(player.getName())) || effect.regionHasEffect(getRegionType(r.getType()).getEffects(), effectName) == 0 ||
                            !effect.hasReagents(l))
                        return false;
                    return true;
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
        
        
        /*for (Location l : getRegionLocations()) {
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
        }*/
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
