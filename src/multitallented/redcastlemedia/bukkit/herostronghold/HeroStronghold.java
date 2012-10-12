package multitallented.redcastlemedia.bukkit.herostronghold;
/**
 *
 * @author Multitallented
 */
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import multitallented.redcastlemedia.bukkit.herostronghold.checkregiontask.CheckRegionTask;
import multitallented.redcastlemedia.bukkit.herostronghold.effect.EffectManager;
import multitallented.redcastlemedia.bukkit.herostronghold.events.CommandEffectEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.listeners.*;
import multitallented.redcastlemedia.bukkit.herostronghold.region.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.hidendra.bukkit.metrics.Metrics;

public class HeroStronghold extends JavaPlugin {
    private PluginServerListener serverListener;
    private Logger log;
    protected FileConfiguration config;
    private RegionManager regionManager;
    private RegionBlockListener blockListener;
    public static Economy econ;
    public static Permission perms;
    private RegionEntityListener regionEntityListener;
    private RegionPlayerInteractListener dpeListener;
    private Map<String, String> pendingInvites = new HashMap<String, String>();
    private static ConfigManager configManager;
    private Map<String, List<String>> pendingCharters = new HashMap<String, List<String>>();
    public static Heroes heroes = null;
    private HashSet<String> effectCommands = new HashSet<String>();
    
    @Override
    public void onDisable() {
        log = Logger.getLogger("Minecraft");
        log.info("[HeroStronghold] is now disabled!");
    }

    @Override
    public void onEnable() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
      
        //setup configs
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        
        //Setup RegionManager
        regionManager = new RegionManager(this, config);
        
        setupPermissions();
        setupEconomy();
        
        //Register Listeners Here
        serverListener = new PluginServerListener(this);
        blockListener = new RegionBlockListener(this);
        dpeListener = new RegionPlayerInteractListener(this);
        regionEntityListener = new RegionEntityListener(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(blockListener, this);
        
        pm.registerEvents(serverListener, this);
        
        pm.registerEvents(regionEntityListener, this);
        
        pm.registerEvents(dpeListener, this);
        
        pm.registerEvents(new CustomListener(this), this);
        log = Logger.getLogger("Minecraft");
        
        //Check for Heroes
        log.info("[HeroStronghold] is looking for Heroes...");
        Plugin currentPlugin = pm.getPlugin("Heroes");
        if (currentPlugin != null) {
            log.info("[HeroStronghold] found Heroes!");
            heroes = ((Heroes) currentPlugin);
        } else {
            log.info("[HeroStronghold] didnt find Heroes, waiting for Heroes to be enabled.");
        }
        
        new EffectManager(this);
        
        //Setup repeating sync task for checking regions
        CheckRegionTask theSender = new CheckRegionTask(getServer(), this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, theSender, 10L, 10L);
        
        System.currentTimeMillis();
        Date date = new Date();
        date.setSeconds(0);
        date.setMinutes(0);
        date.setHours(0);
        long timeUntilDay = (86400000 + date.getTime() - System.currentTimeMillis()) / 50;
        System.out.println("[HeroStronghold] " + timeUntilDay + " ticks until 00:00");
        DailyTimerTask dtt = new DailyTimerTask(this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, dtt, timeUntilDay, 1728000);
        
        log.info("[HeroStronghold] is now enabled!");
    }
    
    public static ConfigManager getConfigManager() {
        return configManager;
    }
    
    public Map<Player, String> getChannels() {
        return dpeListener.getChannels();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        String debug = label;
        for (String s : args) {
            debug += " " + s;
        }
        System.out.println("[HeroStronghold] " + debug);
        Player player = null;
        try {
            player = (Player) sender;
        } catch (Exception e) {

        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (player != null && !(HeroStronghold.perms == null || HeroStronghold.perms.has(player, "herostronghold.admin"))) {
                return true;
            }
            config = getConfig();
            regionManager.reload();
            configManager = new ConfigManager(config, this);
            sender.sendMessage("[HeroStronghold] reloaded");
            return true;
        }
        if (player == null) {
            sender.sendMessage("[HeroStronghold] doesn't recognize non-player commands.");
            return true;
        }
    if (args.length > 2 && args[0].equalsIgnoreCase("war")) {
        //hs war mySR urSR
        
        //Check for valid super-regions
        SuperRegion sr1 = regionManager.getSuperRegion(args[1]);
        SuperRegion sr2 = regionManager.getSuperRegion(args[2]);
        if (sr1 == null || sr2 == null) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That isn't a valid super-region.");
            return true;
        }
        
        //Check if already at war
        if (regionManager.hasWar(sr1, sr2)) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + sr1.getName() + " is already at war!");
            return true;
        }
        
        //Check owner
        if (!sr1.hasOwner(player.getName())) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not an owner of " + sr1.getName());
            return true;
        }
        
        //Calculate Cost
        ConfigManager cm = getConfigManager();
        if (!cm.getUseWar()) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This command is disabled in config.yml");
            return true;
        }
        double cost = cm.getDeclareWarBase() + cm.getDeclareWarPer() * (sr1.getOwners().size() + sr1.getMembers().size() +
                sr2.getOwners().size() + sr2.getMembers().size());
        
        //Check money
        if (HeroStronghold.econ != null) {
            if (sr1.getBalance() < cost) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + sr1.getName() + " doesn't have enough money to war with " + sr2.getName());
                return true;
            } else {
                regionManager.addBalance(sr1, -1 * cost);
            }
        }
        
        regionManager.setWar(sr1, sr2);
        final SuperRegion sr1a = sr1;
        final SuperRegion sr2a = sr2;
        new Runnable() {
              @Override
              public void run()
              {
                getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + sr1a.getName() + " has declared war on " + sr2a.getName() + "!");
              }
        }.run();
        return true;
    } else if (args.length > 2 && args[0].equalsIgnoreCase("peace")) {
        //hs peace mySR urSR
        
        //Check for valid super-regions
        SuperRegion sr1 = regionManager.getSuperRegion(args[1]);
        SuperRegion sr2 = regionManager.getSuperRegion(args[2]);
        if (sr1 == null || sr2 == null) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That isn't a valid super-region.");
            return true;
        }
        
        //Check if already at war
        if (!regionManager.hasWar(sr1, sr2)) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + sr1.getName() + " isn't at war.");
            return true;
        }
        
        //Check owner
        if (!sr1.hasOwner(player.getName())) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not an owner of " + sr1.getName());
            return true;
        }
        
        //Calculate Cost
        ConfigManager cm = getConfigManager();
        if (!cm.getUseWar()) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This command is disabled in config.yml");
            return true;
        }
        double cost = cm.getMakePeaceBase() + cm.getMakePeacePer() * (sr1.getOwners().size() + sr1.getMembers().size() +
                sr2.getOwners().size() + sr2.getMembers().size());
        
        //Check money
        if (HeroStronghold.econ != null) {
            if (sr1.getBalance() < cost) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + sr1.getName() + " doesn't have enough money to make peace with " + sr2.getName());
                return true;
            } else {
                regionManager.addBalance(sr1, -1 * cost);
            }
        }
        
        regionManager.setWar(sr1, sr2);
        final SuperRegion sr1a = sr1;
        final SuperRegion sr2a = sr2;
        new Runnable() {
              @Override
              public void run()
              {
                getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + sr1a.getName() + " has made peace with " + sr2a.getName() + "!");
              }
        }.run();
        return true;
    } else if (args.length > 2 && args[0].equalsIgnoreCase("charter")) {
            
            //Check if valid super region
            SuperRegionType currentRegionType = regionManager.getSuperRegionType(args[1]);
            if (currentRegionType == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " isnt a valid region type");
                int j=0;
                String message = ChatColor.GOLD + "";
                for (String s : regionManager.getSuperRegionTypes()) {
                    if (perms == null || (perms.has(player, "herostronghold.create.all") ||
                            perms.has(player, "herostronghold.create." + s))) {
                        if (message.length() + s.length() + 2 > 55) {
                            player.sendMessage(message + ", ");
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j > 14) {
                            break;
                        } else {
                            message += ", " + s;
                        }
                    }
                }
                if (!message.equals(ChatColor.GOLD + "")) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
                return true;
            }
            
            String regionTypeName = args[1].toLowerCase();
            //Permission Check
            if (perms != null && !perms.has(player, "herostronghold.create.all") &&
                    !perms.has(player, "herostronghold.create." + regionTypeName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionTypeName);
                return true;
            }
            
            //Make sure the super-region requires a Charter
            if (currentRegionType.getCharter() <= 0) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " doesnt require a charter. /hs create " + args[1]);
                return true;
            }
            
            //Make sure the name isn't too long
            if (args[2].length() > 15) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Sorry but that name is too long. (16 max)");
                return true;
            }
            //Check if valid filename
            if (!Util.validateFileName(args[2])) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Sorry but that is an invalid filename.");
                return true;
            }
            
            //Check if valid name
            if (pendingCharters.containsKey(args[2].toLowerCase())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is already a charter or region with that name.");
                return true;
            }
            if (getServer().getPlayerExact(args[2]) != null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Dont name a super-region after a player");
                return true;
            }
            
            //Check if allowed super-region
            if (regionManager.getSuperRegion(args[2]) != null && (!regionManager.getSuperRegion(args[2]).hasOwner(player.getName())
                    || regionManager.getSuperRegion(args[2]).getType().equalsIgnoreCase(args[1]))) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That exact super-region already exists.");
                return true;
            }
            
            //Add the charter
            List<String> tempList = new ArrayList<String>();
            tempList.add(args[1]);
            tempList.add(player.getName());
            pendingCharters.put(args[2].toLowerCase(), tempList);
            configManager.writeToCharter(args[2].toLowerCase(), tempList);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] Youve successfully created a charter for " + args[2]);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] Get other people to type /hs signcharter " + args[2] + " to get started.");
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("charterstats")) {
            //Check if valid charter
            if (!pendingCharters.containsKey(args[1].toLowerCase())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " isn't a valid charter type.");
                return true;
            }
            
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " signatures: ");
            int j=0;
            String message = ChatColor.GOLD + "";
            List<String> charter = pendingCharters.get(args[1]);
            if (charter != null) {
                for (String s : charter) {
                    if (message.length() + s.length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += s + ", ";
                    }
                }
                if (!charter.isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
            } else {
                player.sendMessage(ChatColor.RED + "[HeroStronghold] There was an error loading that charter");
                warning("Failed to load charter " + args[1] + ".yml");
            }
            
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("signcharter")) {
            //Check if valid name
            if (!pendingCharters.containsKey(args[1].toLowerCase())) {
                player.sendMessage(ChatColor.GRAY + "[Herostronghold] There is no charter for " + args[1]);
                return true;
            }
            
            //Check permission
            if (perms != null && !perms.has(player, "herostronghold.join")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have permission to sign a charter.");
                return true;
            }
            
            //Sign Charter
            List<String> charter = pendingCharters.get(args[1].toLowerCase());
            
            //Check if the player has already signed the charter once
            if (charter.contains(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You've already signed this charter.");
                return true;
            }
            
            charter.add(player.getName());
            configManager.writeToCharter(args[1], charter);
            pendingCharters.put(args[1], charter);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] You just signed the charter for " + args[1]);
            int remaining = 0;
            SuperRegionType srt = regionManager.getSuperRegionType(charter.get(0));
            if (srt != null) {
                remaining = srt.getCharter() - charter.size() + 1;
            }
            if (remaining > 0) {
                player.sendMessage(ChatColor.GOLD + "" + remaining + " signatures to go!");
            }
            Player owner = getServer().getPlayer(charter.get(1));
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(ChatColor.GOLD + "[HeroStronghold] " + player.getDisplayName() + " just signed your charter for " + args[1]);
                if (remaining > 0) {
                    owner.sendMessage(ChatColor.GOLD + "" + remaining + " signatures to go!");
                }
            }
            return true;
        } else if (args.length > 1 && args[0].equals("cancelcharter")) {
            if (!pendingCharters.containsKey(args[1].toLowerCase())) {
                player.sendMessage(ChatColor.GRAY + "[Herostronghold] There is no charter for " + args[1]);
                return true;
            }
            
            if (pendingCharters.get(args[1]).size() < 2 || !pendingCharters.get(args[1]).get(1).equals(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are the not owner of this charter.");
                return true;
            }
            
            configManager.removeCharter(args[1]);
            pendingCharters.remove(args[1]);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] You have canceled the charter for " + args[1]);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            String regionName = args[1];
            
            //Permission Check
            boolean nullPerms = perms == null;
            boolean createAll = nullPerms || perms.has(player, "herostronghold.create.all");
            if (!(nullPerms || createAll || perms.has(player, "herostronghold.create." + regionName))) {
                //TODO add limited quantity permissions here
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionName);
                return true;
            }
            
            Location currentLocation = player.getLocation();
            //Check if player is standing someplace where a chest can be placed.
            Block currentBlock = currentLocation.getBlock();
            if (currentBlock.getTypeId() != 0) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] please stand someplace where a chest can be placed.");
                return true;
            }
            RegionType currentRegionType = regionManager.getRegionType(regionName);
            if (currentRegionType == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + regionName + " isnt a valid region type");
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Try /hs create " + regionName + " <insert_name_here>");
                return true;
            }
            
            //Check if player can afford to create this herostronghold
            double costCheck = 0;
            if (econ != null) {
                double cost = currentRegionType.getMoneyRequirement();
                if (econ.getBalance(player.getName()) < cost) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need $" + cost + " to make this type of structure.");
                    return true;
                } else {
                    costCheck = cost;
                }
                
            }
            
            //Check if over max number of regions of that type
            if (regionManager.isAtMaxRegions(player, currentRegionType)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have permission to build more " + currentRegionType.getName());
                return true;
            }
            
            //Check if too close to other HeroStrongholds
            if (!regionManager.getContainingBuildRegions(currentLocation).isEmpty()) {
                player.sendMessage (ChatColor.GRAY + "[HeroStronghold] You are too close to another HeroStronghold");
                return true;
            }
            
            //Check if in a super region and if has permission to make that region
            String playername = player.getName();
            List<String> reqSuperRegion = currentRegionType.getSuperRegions();
            boolean meetsReqs = reqSuperRegion == null || reqSuperRegion.isEmpty();
            for (SuperRegion sr : regionManager.getContainingSuperRegions(currentLocation)) {
                if (!meetsReqs && reqSuperRegion != null && reqSuperRegion.contains(sr.getType())) {
                    meetsReqs = true;
                }
                if (!sr.hasOwner(playername)) {
                    if (!sr.hasMember(playername) || !sr.getMember(playername).contains(regionName)) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have permission from an owner of " + sr.getName()
                                + " to create a " + regionName + " here");
                        return true;
                    }
                }
            }
            if (!meetsReqs) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are required to build this " + regionName + " in a:");
                String message = ChatColor.GOLD + "";
                int j=0;
                for (String s : reqSuperRegion) {
                    if (message.length() + s.length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += s + ", ";
                    }
                }
                if (!reqSuperRegion.isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
                return true;
            }
            
            
            //Prepare a requirements checklist
            ArrayList<ItemStack> requirements = currentRegionType.getRequirements();
            Map<Integer, Integer> reqMap = null;
            if (!requirements.isEmpty()) {
                reqMap = new HashMap<Integer, Integer>();
                for (ItemStack currentIS : requirements) {
                    reqMap.put(new Integer(currentIS.getTypeId()), new Integer(currentIS.getAmount()));
                }
                
                //Check the area for required blocks
                int radius = (int) Math.sqrt(currentRegionType.getBuildRadius());

                int lowerLeftX = (int) currentLocation.getX() - radius;
                int lowerLeftY = (int) currentLocation.getY() - radius;
                lowerLeftY = lowerLeftY < 0 ? 0 : lowerLeftY;
                int lowerLeftZ = (int) currentLocation.getZ() - radius;

                int upperRightX = (int) currentLocation.getX() + radius;
                int upperRightY = (int) currentLocation.getY() + radius;
                upperRightY = upperRightY > 255 ? 255 : upperRightY;
                int upperRightZ = (int) currentLocation.getZ() + radius;
                
                World world = currentLocation.getWorld();
                
                
                outer: for (int x=lowerLeftX; x<upperRightX; x++) {
                    
                    for (int z=lowerLeftZ; z<upperRightZ; z++) {
                        
                        for (int y=lowerLeftY; y<upperRightY; y++) {
                            
                            int type = world.getBlockTypeIdAt(x, y, z);
                            if (type != 0 && reqMap.containsKey(type)) {
                                if (reqMap.get(type) < 2) {
                                    reqMap.remove(type);
                                    if (reqMap.isEmpty()) {
                                        break outer;
                                    }
                                } else {
                                    reqMap.put(type, reqMap.get(type) - 1);
                                }
                            }
                        }
                        
                    }
                    
                }
            }
            
            
            if (reqMap != null && !reqMap.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have all of the required blocks in this structure.");
                String message = ChatColor.GOLD + "";
                int j=0;
                for (int type : reqMap.keySet()) {
                    int reqAmount = reqMap.get(type);
                    String reqType = Material.getMaterial(type).name();
                    if (message.length() + reqAmount + reqType.length() + 3 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += reqAmount + ":" + reqType + ", ";
                    }
                }
                if (!reqMap.isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
                return true;
            }
            
            //Create chest at players feet for tracking reagents and removing upkeep items
            currentBlock.setType(Material.CHEST);
            
            ArrayList<String> owners = new ArrayList<String>();
            owners.add(player.getName());
            if (costCheck > 0) {
                econ.withdrawPlayer(player.getName(), costCheck);
            }
            
            if (heroes != null) {
                heroes.getCharacterManager().getHero(player).gainExp(currentRegionType.getExp(), ExperienceType.EXTERNAL, player.getLocation());
            }
            regionManager.addRegion(currentLocation, regionName, owners);
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "You successfully create a " + ChatColor.RED + regionName);
            
            //Tell the player what reagents are required for it to work
            String message = ChatColor.GOLD + "Reagents: ";
            if (currentRegionType.getReagents() != null) {
                int j=0;
                for (ItemStack is : currentRegionType.getReagents()) {
                    String addLine = is.getAmount() + ":" + is.getType().name() + ", ";
                    if (message.length() + addLine.length() > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j < 14) {
                        message += addLine;
                    } else {
                        break;
                    }
                }
            }
            if (currentRegionType.getReagents() == null || currentRegionType.getReagents().isEmpty()) {
                message += "None";
                player.sendMessage(message);
            } else {
                player.sendMessage(message.substring(0, message.length()-2));
            }
            
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("create")) {
            //Check if valid name (further name checking later)
            if (args[2].length() > 16 || !Util.validateFileName(args[2])) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That name is invalid.");
                return true;
            }
            if (getServer().getPlayerExact(args[2]) != null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Dont name a super-region after a player.");
                return true;
            }
            
            String regionTypeName = args[1];
            //Permission Check
            if (perms != null && !perms.has(player, "herostronghold.create.all") &&
                    !perms.has(player, "herostronghold.create." + regionTypeName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionTypeName);
                return true;
            }
            
            //Check if valid super region
            Location currentLocation = player.getLocation();
            SuperRegionType currentRegionType = regionManager.getSuperRegionType(regionTypeName);
            if (currentRegionType == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + regionTypeName + " isnt a valid region type");
                int j=0;
                String message = ChatColor.GOLD + "";
                for (String s : regionManager.getSuperRegionTypes()) {
                    if (perms == null || (perms.has(player, "herostronghold.create.all") ||
                            perms.has(player, "herostronghold.create." + s))) {
                        if (message.length() + s.length() + 2 > 55) {
                            player.sendMessage(message);
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j > 14) {
                            break;
                        } else {
                            message += s + ", ";
                        }
                    }
                }
                if (!regionManager.getSuperRegionTypes().isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
                return true;
            }
            
            //Check if player can afford to create this herostronghold
            double costCheck = 0;
            if (econ != null) {
                double cost = currentRegionType.getMoneyRequirement();
                if (econ.getBalance(player.getName()) < cost) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need $" + cost + " to make this type of region.");
                    return true;
                } else {
                    costCheck = cost;
                }
                
            }

            Map<String, List<String>> members = new HashMap<String, List<String>>();
            int currentCharter = currentRegionType.getCharter();
            //Make sure the super-region has a valid charter
            if (!HeroStronghold.perms.has(player, "herostronghold.admin")) {
                if (currentCharter > 0) {
                    try {
                        if (!pendingCharters.containsKey(args[2])) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need to start a charter first. /hs charter " + args[1] + " " + args[2]);
                            return true;
                        } else if (pendingCharters.get(args[2]).size() <= currentCharter) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need " + currentCharter + " signature(s). /hs signcharter " + args[2]);
                            return true;
                        } else if (!pendingCharters.get(args[2]).get(0).equalsIgnoreCase(args[1]) ||
                                !pendingCharters.get(args[2]).get(1).equalsIgnoreCase(player.getName())) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] The charter for this name is for a different region type or owner.");
                            player.sendMessage(ChatColor.GRAY + "Owner: " + pendingCharters.get(args[2]).get(1) + ", Type: " + pendingCharters.get(args[2]).get(0));
                            return true;
                        } else {
                            int i =0;
                            for (String s : pendingCharters.get(args[2])) {
                                ArrayList<String> tempArray = new ArrayList<String>();
                                tempArray.add("member");
                                if (i > 2) {
                                    members.put(s, tempArray);
                                } else {
                                    i++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        warning("Possible failure to find correct charter for " + args[2]);
                    }
                }
            } else if (pendingCharters.containsKey(args[2])) {
                if (currentCharter > 0) {
                    try {
                        if (pendingCharters.get(args[2]).size() <= currentCharter) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need " + currentCharter + " signature(s). /hs signcharter " + args[2]);
                            return true;
                        } else if (!pendingCharters.get(args[2]).get(0).equalsIgnoreCase(args[1]) ||
                                !pendingCharters.get(args[2]).get(1).equalsIgnoreCase(player.getName())) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] The charter for this name is for a different region type or owner.");
                            player.sendMessage(ChatColor.GRAY + "Owner: " + pendingCharters.get(args[2]).get(1) + ", Type: " + pendingCharters.get(args[2]).get(0));
                            return true;
                        } else {
                            int i =0;
                            for (String s : pendingCharters.get(args[2])) {
                                ArrayList<String> tempArray = new ArrayList<String>();
                                tempArray.add("member");
                                if (i > 2) {
                                    members.put(s, tempArray);
                                } else {
                                    i++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        warning("Possible failure to find correct charter for " + args[2]);
                    }
                }
            }
            
            Map<String, Integer> requirements = currentRegionType.getRequirements();
            HashMap<String, Integer> req = new HashMap<String, Integer>();
            for (String s : currentRegionType.getRequirements().keySet()) {
                req.put(new String(s), new Integer(requirements.get(s)));
            }
            
            //Check for required regions
            List<String> children = currentRegionType.getChildren();
            if (children != null) {
                for (String s : children) {
                    if (!req.containsKey(s))
                        req.put(new String(s), 1);
                }
            }
            
            //Check if there already is a super-region by that name, but not if it's one of the child regions
            if (regionManager.getSuperRegion(args[2]) != null && (children == null || !children.contains(regionManager.getSuperRegion(args[2]).getType()))) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is already a super-region by that name.");
                return true;
            }
            
            
            
            List<String> quietDestroy = new ArrayList<String>();
            int radius = (int) currentRegionType.getRawRadius();
            
            
            //Check if there is an overlapping super-region of the same type
            for (SuperRegion sr : regionManager.getSortedSuperRegions()) {
                try {
                    if (sr.getLocation().distance(currentLocation) < radius + regionManager.getSuperRegionType(sr.getType()).getRawRadius() &&
                            (sr.getType().equalsIgnoreCase(regionTypeName) || !sr.hasOwner(player.getName()))) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + sr.getName() + " is already here.");
                        return true;
                    }
                } catch (IllegalArgumentException iae) {
                    
                }
            }
            if (!req.isEmpty()) {
                for (SuperRegion sr : regionManager.getContainingSuperRegions(currentLocation)) {
                    if (children.contains(sr.getType()) && sr.hasOwner(player.getName())) {
                        quietDestroy.add(sr.getName());
                    }

                    String rType = sr.getType();
                    if (!sr.hasOwner(player.getName()) && (!sr.hasMember(player.getName()) || !sr.getMember(player.getName()).contains(regionTypeName))) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not permitted to build a " + regionTypeName + " inside " + sr.getName());
                        return true;
                    } 
                    if (req.containsKey(rType)) {
                        int amount = req.get(rType);
                        if (amount < 2) {
                            req.remove(rType);
                            if (req.isEmpty()) {
                                break;
                            }
                        } else {
                            req.put(rType, amount - 1);
                        }
                    }
                }
                
                Location loc = player.getLocation();
                double x = loc.getX();
                double y = loc.getY();
                double z = loc.getZ();
                int radius1 = currentRegionType.getRawRadius();
                for (Region r : regionManager.getSortedRegions()) {	  	
                    Location l = r.getLocation();
                    if (l.getX() + radius1 < x) {
                        break;
                    }

                    if (l.getX() - radius1 < x && l.getY() + radius1 > y && l.getY() - radius1 < y && 
                            l.getZ() + radius1 > z && l.getZ() - radius1 < z && l.getWorld().equals(loc.getWorld()) && req.containsKey(r.getType())) {
                        if (req.get(r.getType()) < 2) {
                            req.remove(r.getType());
                        } else {
                            req.put(r.getType(), req.get(r.getType()) - 1);
                        }
                    }
                }
                if (!req.isEmpty()) {
                    for (Region r : regionManager.getContainingRegions(currentLocation)) {
                        String rType = regionManager.getRegion(r.getLocation()).getType();
                        if (req.containsKey(rType)) {
                            int amount = req.get(rType);
                            if (amount <= 1) {
                                req.remove(rType);
                            } else {
                                req.put(rType, amount - 1);
                            }
                        }
                    }
                    
                }
            }
            if (!req.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This area doesnt have all of the required regions.");
                int j=0;
                String message = ChatColor.GOLD + "";
                for (String s : req.keySet()) {
                    if (message.length() + s.length() + 3 + req.get(s).toString().length() > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j >14) {
                        break;
                    } else {
                        message += req.get(s) + " " + s + ", ";
                    }
                }
                if (!req.isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
                return true;
            }
            
            //Assimulate any child super regions
            List<String> owners = new ArrayList<String>();
            double balance = 0.0;
            for (String s : quietDestroy) {
                SuperRegion sr = regionManager.getSuperRegion(s);
                for (String so : sr.getOwners()) {
                    if (!owners.contains(so))
                        owners.add(so);
                }
                for (String sm : sr.getMembers().keySet()) {
                    if (!members.containsKey(sm) && sr.getMember(sm).contains("member"))
                        members.put(sm, sr.getMember(sm));
                }
                balance += sr.getBalance();
            }
            //Check if more members needed to create the super-region
            if (owners.size() + members.size() < currentRegionType.getPopulation()) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need " + (currentRegionType.getPopulation() - owners.size() - members.size()) + " more members.");
                return true;
            }
            for (String s : quietDestroy) {
                regionManager.destroySuperRegion(s, false);
            }
            if (currentCharter > 0 && pendingCharters.containsKey(args[2])) {
                configManager.removeCharter(args[2]);
                pendingCharters.remove(args[2]);
            }
            String playername = player.getName();
            if (!owners.contains(playername)) {
                owners.add(playername);
            }
            if (costCheck > 0) {
                econ.withdrawPlayer(player.getName(), costCheck);
            }
            
            if (heroes != null) {
                Hero hero = heroes.getCharacterManager().getHero(player);
                if (hero.hasParty()) {
                    hero.getParty().gainExp(currentRegionType.getExp(), ExperienceType.EXTERNAL, player.getLocation());
                } else {
                    heroes.getCharacterManager().getHero(player).gainExp(currentRegionType.getExp(), ExperienceType.EXTERNAL, player.getLocation());
                }
            }
            regionManager.addSuperRegion(args[2], currentLocation, regionTypeName, owners, members, currentRegionType.getDailyPower(), balance);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] You've created a new " + args[1] + " called " + args[2]);
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("listall")) {
            if (args.length > 1) {
                SuperRegionType srt = regionManager.getSuperRegionType(args[1]);
                if (srt == null) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region type named " + args[1]);
                    return true;
                }
                String message = ChatColor.GOLD + "";
                int j =0;
                for (SuperRegion sr : regionManager.getSortedSuperRegions()) {
                    if (message.length() + sr.getName().length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += sr.getName() + ", ";
                    }
                }
                if (!message.equals(ChatColor.GOLD + "")) {
                    player.sendMessage(message);
                }
            } else {
                String message = ChatColor.GOLD + "";
                int j =0;
                for (SuperRegion sr : regionManager.getSortedSuperRegions()) {
                    if (message.length() + sr.getName().length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += sr.getName() + ", ";
                    }
                }
                if (!message.equals(ChatColor.GOLD + "")) {
                    player.sendMessage(message);
                }
            }
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("withdraw")) {
            if (econ == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] No econ plugin recognized");
                return true;
            }
            double amount = 0;
            try {
                amount = Double.parseDouble(args[1]);
                if (amount < 0) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Withdraw a positive amount only.");
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Invalid amount /hs withdraw <amount> <superregionname>");
                return true;
            }
            
            //Check if valid super-region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[2] + " is not a super-region");
                return true;
            }
            
            //Check if owner or permitted member
            if ((!sr.hasMember(player.getName()) || !sr.getMember(player.getName()).contains("withdraw")) && !sr.hasOwner(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not a member or dont have permission to withdraw");
                return true;
            }
            
            //Check if bank has that money
            double output = regionManager.getSuperRegionType(sr.getType()).getOutput();
            if (output < 0 && sr.getBalance() - amount < -output) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You cant withdraw below the the minimum required.");
                return true;
            } else if (output >= 0 && sr.getBalance() - amount < 0) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + sr.getName() + " doesnt have that much money.");
                return true;
            }
            
            //Withdraw the money
            econ.depositPlayer(player.getName(), amount);
            regionManager.addBalance(sr, -amount);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] You withdrew " + amount + " in the bank of " + args[2]);
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("deposit")) {
            if (econ == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] No econ plugin recognized");
                return true;
            }
            double amount = 0;
            try {
                amount = Double.parseDouble(args[1]);
                if (amount < 0) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Nice try. Deposit a positive amount.");
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Invalid amount /hs deposit <amount> <superregionname>");
                return true;
            }
            
            //Check if player has that money
            if (!econ.has(player.getName(), amount)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have that much money.");
                return true;
            }
            
            //Check if valid super-region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[2] + " is not a super-region");
                return true;
            }
            
            //Check if owner or member
            if (!sr.hasMember(player.getName()) && !sr.hasOwner(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not a member of " + args[2]);
                return true;
            }
            
            //Deposit the money
            econ.withdrawPlayer(player.getName(), amount);
            regionManager.addBalance(sr, amount);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] You deposited " + amount + " in the bank of " + args[2]);
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("settaxes")) {
            String playername = player.getName();
            //Check if the player is a owner or member of the super region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no region called " + args[2]);
                return true;
            }
            if (!sr.hasOwner(playername) && !sr.hasMember(playername)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have permission to set taxes for " + args[2] + ".");
                return true;
            }
            
            //Check if member has permission
            if (sr.hasMember(playername) && !sr.getMember(playername).contains("settaxes")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have permission to set taxes for " + args[2] + ".");
                return true;
            }
            
            //Check if valid amount
            double taxes = 0;
            try {
                taxes = Double.parseDouble(args[1]);
                double maxTax = configManager.getMaxTax();
                if (taxes < 0 && (maxTax == 0 || taxes <= maxTax)) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You cant set negative taxes.");
                    return true;
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Use /hs settaxes <amount> <superregionname>.");
                return true;
            }
            
            
            
            //Set the taxes
            regionManager.setTaxes(sr, taxes);
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] You've set " + args[2] + "'s taxes to " + args[1]);
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("listperms")) {
            //Get target player
            String playername = "";
            if (args.length > 3) {
                Player currentPlayer = getServer().getPlayer(args[1]);
                if (currentPlayer == null) {
                    player.sendMessage(ChatColor.GOLD + "[HeroStronghold] Could not find " + args[1]);
                    return true;
                } else {
                    playername = currentPlayer.getName();
                }
            } else {
                playername = player.getName();
            }
            
            String message = ChatColor.GRAY + "[HeroStronghold] " + playername + " perms for " + args[2] + ":";
            String message2 = ChatColor.GOLD + "";
            //Check if the player is a owner or member of the super region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no region called " + args[2]);
                return true;
            }
            if (sr.hasOwner(playername)) {
                player.sendMessage(message);
                player.sendMessage(message2 + "All Permissions");
                return true;
            } else if (sr.hasMember(playername)) {
                player.sendMessage(message);
                int j=0;
                for (String s : sr.getMember(label)) {
                    if (message2.length() + s.length() + 2 > 57) {
                        player.sendMessage(message2);
                        message2 = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message2 += s + ", ";
                    }
                }
                if (!sr.getMember(label).isEmpty()) {
                    player.sendMessage(message2.substring(0, message2.length() - 2));
                }
                return true;
            }
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " doesn't belong to that region.");
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("listallperms")) {
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] List of all member permissions:");
            player.sendMessage(ChatColor.GRAY + "member = is a member of the super-region.");
            player.sendMessage(ChatColor.GRAY + "title:<title> = player's title in channel");
            player.sendMessage(ChatColor.GRAY + "addmember = lets the player use /hs addmember");
            player.sendMessage(ChatColor.GRAY + "<regiontype> = lets the player build that region type");
            player.sendMessage(ChatColor.GRAY + "withdraw = lets the player withdraw from the bank");
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("ch")) {
            //Check if wanting to be set to any other channel
            if (args.length == 1 || args[1].equalsIgnoreCase("o") || args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("none")) {
                dpeListener.setPlayerChannel(player, "");
                return true;
            }
            
            if (args.length < 2) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] /hs ch channelname.  /hs ch (to go to all chat)");
                return true;
            }
            
            //Check if valid super region
            SuperRegion sr = regionManager.getSuperRegion(args[1]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region by that name (" + args[1] + ").");
                player.sendMessage(ChatColor.GRAY + "Try /hs ch to go to all chat.");
                return true;
            }
            
            //Check if player is a member or owner of that super-region
            String playername = player.getName();
            if (!sr.hasMember(playername) && !sr.hasOwner(playername)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You must be a member of " + args[1] + " before joining thier channel");
                return true;
            }
            
            //Set the player as being in that channel
            dpeListener.setPlayerChannel(player, args[1]);
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("addmember")) {
            //Check if valid super region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region by that name (" + args[2] + ").");
                return true;
            }
            
            //Check if player is a member or owner of that super-region
            String playername = player.getName();
            boolean isOwner = sr.hasOwner(playername);
            boolean isMember = sr.hasMember(playername);
            boolean isAdmin = HeroStronghold.perms.has(player, "herostronghold.admin");
            if (!isMember && !isOwner && !isAdmin) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You arent a member of " + args[2]);
                return true;
            }
            
            //Check if player has permission to invite players
            if (!isAdmin && isMember && !sr.getMember(playername).contains("addmember")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need permission addmember from an owner of " + args[2]);
                return true;
            }
            
            //Check if valid player
            Player invitee = getServer().getPlayer(args[1]);
            if (invitee == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " is not online.");
                return true;
            }
            
            //Check permission herostronghold.join
            if (!perms.has(invitee, "herostronghold.join")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " doesnt have permission to join a super-region.");
                return true;
            }
            
            //Check if has housing effect and if has enough housing
            if (regionManager.getSuperRegionType(sr.getType()).hasEffect("housing") && !regionManager.hasAvailableHousing(sr)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You cant addmember people to " + sr.getName() + " until you build more housing");
            }
            
            //Send an invite
            pendingInvites.put(invitee.getName(), args[2]);
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You have invited " + ChatColor.GOLD + invitee.getDisplayName() + ChatColor.GRAY + " to join " + ChatColor.GOLD + args[2]);
            if (invitee != null)
                invitee.sendMessage(ChatColor.GOLD + "[HeroStronghold] You have been invited to join " + args[2] + ". /hs accept " + args[2]);
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("accept")) {
            //Check if player has a pending invite to that super-region
            if (!pendingInvites.containsKey(player.getName()) || !pendingInvites.get(player.getName()).equals(args[1])) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't have an invite to " + args[1]);
                return true;
            }
            
            //Check if valid super region
            SuperRegion sr = regionManager.getSuperRegion(args[1]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region by that name (" + args[1] + ").");
                return true;
            }
            
            //Check if player is a member or owner of that super-region
            String playername = player.getName();
            if (sr.hasMember(playername) || sr.hasOwner(playername)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are already a member of " + args[1]);
                return true;
            }
            
            //Add the player to the super region
            ArrayList<String> perm = new ArrayList<String>();
            perm.add("member");
            regionManager.setMember(sr, player.getName(), perm);
            pendingInvites.remove(player.getName());
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] Welcome to " + args[1]);
            for (String s : sr.getMembers().keySet()) {
                Player p = getServer().getPlayer(s);
                if (p != null) {
                    p.sendMessage(ChatColor.GOLD + playername + " has joined " + args[1]);
                }
            }
            for (String s : sr.getOwners()) {
                Player p = getServer().getPlayer(s);
                if (p != null) {
                    p.sendMessage(ChatColor.GOLD + playername + " has joined " + args[1]);
                }
            }
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("addowner")) {
            Player p = getServer().getPlayer(args[1]);
            String playername = args[1];
            
            //Check valid super-region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region named " + args[2]);
                return true;
            }
            
            //Check valid player
            if (p == null && !sr.hasMember(playername)) {
                player.sendMessage(ChatColor.GRAY + "[Herostronghold] There is no player online named: " + args[1]);
                return true;
            } else {
                playername = p.getName();
            }
            
            
            
            //Check if player is an owner of that region
            if (!sr.hasOwner(player.getName()) && !HeroStronghold.perms.has(player, "herostronghold.admin")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You arent an owner of " + args[2]);
                return true;
            }
            
            //Check if playername is already an owner
            if (sr.hasOwner(playername)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " is already an owner of " + args[2]);
                return true;
            }
            
            //Check if player is member of super-region
            if (!sr.hasMember(playername)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " is not a member of " + args[2]);
                return true;
            }
            
            regionManager.removeMember(sr, playername);
            if (p != null)
                p.sendMessage(ChatColor.GOLD + "[HeroStronghold] You are now an owner of " + args[2]);
            for (String s : sr.getMembers().keySet()) {
                Player pl = getServer().getPlayer(s);
                if (pl != null) {
                    pl.sendMessage(ChatColor.GOLD + playername + " is now an owner of " + args[2]);
                }
            }
            for (String s : sr.getOwners()) {
                Player pl = getServer().getPlayer(s);
                if (pl != null) {
                    pl.sendMessage(ChatColor.GOLD + playername + " is now an owner of " + args[2]);
                }
            }
            regionManager.setOwner(sr, playername);
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("remove")) {
            Player p = getServer().getPlayer(args[1]);
            String playername = args[1];
            
            
            //Check valid super-region
            SuperRegion sr = regionManager.getSuperRegion(args[2]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region named " + args[2]);
                return true;
            }
            
            //Check valid player
            if (p != null) {
                playername = p.getName();
            }
            
            boolean isMember = sr.hasMember(playername); 
            boolean isOwner = sr.hasOwner(playername); 
            boolean isAdmin = HeroStronghold.perms.has(player, "herostronghold.admin");
            
            //Check if player is member or owner of super-region
            if (!isMember && !isOwner) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " is not a member of " + args[2]);
                return true;
            }
            //Check if player is removing self
            if (playername.equalsIgnoreCase(player.getName())) {
                if (isMember) {
                    regionManager.removeMember(sr, playername);
                } else if (isOwner) {
                    regionManager.setOwner(sr, playername);
                }
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You have left " + args[2]);
                for (String s : sr.getMembers().keySet()) {
                    Player pl = getServer().getPlayer(s);
                    if (pl != null) {
                        pl.sendMessage(ChatColor.GOLD + playername + " left " + args[2]);
                    }
                }
                for (String s : sr.getOwners()) {
                    Player pl = getServer().getPlayer(s);
                    if (pl != null) {
                        pl.sendMessage(ChatColor.GOLD + playername + " left " + args[2]);
                    }
                }
                return true;
            }
            
            //Check if player has remove permission
            if (!sr.hasOwner(player.getName()) &&  !(!sr.hasMember(player.getName()) || !sr.getMember(player.getName()).contains("remove"))
                    && !isAdmin) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't have permission to remove that member.");
                return true;
            }
            
            
            if (isMember) {
                regionManager.removeMember(sr, playername);
            } else if (isOwner) {
                regionManager.setOwner(sr, playername);
            } else {
                return true;
            }
            if (p != null)
                p.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are no longer a member of " + args[2]);
            
            for (String s : sr.getMembers().keySet()) {
                Player pl = getServer().getPlayer(s);
                if (pl != null) {
                    pl.sendMessage(ChatColor.GOLD + playername + " was removed from " + args[2]);
                }
            }
            for (String s : sr.getOwners()) {
                Player pl = getServer().getPlayer(s);
                if (pl != null) {
                    pl.sendMessage(ChatColor.GOLD + playername + " was removed from " + args[2]);
                }
            }
            return true;
        } else if (args.length > 3 && args[0].equalsIgnoreCase("toggleperm")) {
            Player p = getServer().getPlayer(args[1]);
            String playername = args[1];
            
            //Check valid super-region
            SuperRegion sr = regionManager.getSuperRegion(args[3]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region named " + args[3]);
                return true;
            }
            
            //Check if player is an owner of the super region
            if (!sr.hasOwner(player.getName())) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You aren't an owner of " + args[3]);
                return true;
            }
            
            //Check valid player
            if (p == null && !sr.hasMember(args[1])) {
                player.sendMessage(ChatColor.GRAY + "[Herostronghold] There is no player named: " + args[1]);
                return true;
            } else if (p != null) {
                playername = p.getName();
            }
            
            //Check if player is member and not owner of super-region
            if (!sr.hasMember(playername)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " either owns, or is not a member of " + args[3]);
                return true;
            }
            
            List<String> perm = sr.getMember(playername);
            if (perm.contains(args[2])) {
                perm.remove(args[2]);
                regionManager.setMember(sr, playername, perm);
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Removed perm " + args[2] + " for " + args[1] + " in " + args[3]);
                if (p != null)
                    p.sendMessage(ChatColor.GRAY + "[HeroStronghold] Your perm " + args[2] + " was revoked in " + args[3]);
                return true;
            } else {
                perm.add(args[2]);
                regionManager.setMember(sr, playername, perm);
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Added perm " + args[2] + " for " + args[1] + " in " + args[3]);
                if (p != null)
                    p.sendMessage(ChatColor.GRAY + "[HeroStronghold] You were granted permission " + args[2] + " in " + args[3]);
                return true;
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("whatshere")) {
            Location loc = player.getLocation();
            boolean foundRegion = false;
            for (Region r : regionManager.getContainingRegions(loc)) {
                foundRegion = true;
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Found Region ID: " + ChatColor.GOLD + r.getID());
                String message = ChatColor.GRAY + "Type: " + r.getType();
                if (!r.getOwners().isEmpty()) {
                    message += ", Owned by: " + r.getOwners().get(0);
                }
                player.sendMessage(message);
            }
            
            for (SuperRegion sr : regionManager.getContainingSuperRegions(loc)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Found Super-Region named: " + ChatColor.GOLD + sr.getName());
                String message = ChatColor.GRAY + "Type: " + sr.getType();
                if (!sr.getOwners().isEmpty()) {
                    message += ", Owned by: " + sr.getOwners().get(0);
                }
                player.sendMessage(message);
            }
            if (!foundRegion) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There are no regions here.");
            }
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("info")) {
            //Check if valid regiontype or super-regiontype
            RegionType rt = regionManager.getRegionType(args[1]);
            SuperRegionType srt = regionManager.getSuperRegionType(args[1]);
            if (rt == null && srt == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no region type called " + args[1]);
                return true;
            }
            if (rt != null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Info for region type " + ChatColor.GOLD + args[1] + ":");
                player.sendMessage(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + rt.getMoneyRequirement() + ChatColor.GRAY +
                        ", Payout: " + ChatColor.GOLD + rt.getMoneyOutput() + ChatColor.GRAY + ", Radius: " + ChatColor.GOLD + (int) Math.sqrt(rt.getRadius()));
                
                String message = ChatColor.GRAY + "Description: " + ChatColor.GOLD;
                int j=0;
                if (rt.getDescription() != null) {
                    String tempMess = rt.getDescription();
                    if (tempMess.length() + message.length() <= 55) {
                        player.sendMessage(message + tempMess);
                        tempMess = null;
                    }
                    while (tempMess != null && j<12) {
                        if (tempMess.length() > 53) {
                            message += tempMess.substring(0, 53);
                            player.sendMessage(message);
                            tempMess = tempMess.substring(53);
                            message = ChatColor.GOLD + "";
                            j++;
                        } else {
                            player.sendMessage(message + tempMess);
                            tempMess = null;
                            j++;
                        }
                    }
                }
                
                message = ChatColor.GRAY + "Effects: " + ChatColor.GOLD;
                if (rt.getEffects() != null) {
                    for (String is : rt.getEffects()) {
                        String addLine = is.split("\\.")[0] + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (rt.getEffects() == null || rt.getEffects().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
                message = ChatColor.GRAY + "Requirements: " + ChatColor.GOLD;
                if (rt.getRequirements() != null) {
                    for (ItemStack is : rt.getRequirements()) {
                        String addLine = is.getAmount() + ":" + is.getType().name() + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (rt.getRequirements() == null || rt.getRequirements().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
                message = ChatColor.GRAY + "Reagents: " + ChatColor.GOLD;
                if (rt.getReagents() != null) {
                    for (ItemStack is : rt.getReagents()) {
                        String addLine = is.getAmount() + ":" + is.getType().name() + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (rt.getReagents() == null || rt.getReagents().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
                message = ChatColor.GRAY + "UpkeepCost: " + ChatColor.GOLD;
                if (rt.getUpkeep() != null) {
                    for (ItemStack is : rt.getUpkeep()) {
                        String addLine = is.getAmount() + ":" + is.getType().name() + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (rt.getUpkeep() == null || rt.getUpkeep().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
                message = ChatColor.GRAY + "Output: " + ChatColor.GOLD;
                if (rt.getOutput() != null) {
                    for (ItemStack is : rt.getOutput()) {
                        String addLine = is.getAmount() + ":" + is.getType().name() + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (rt.getOutput() == null || rt.getOutput().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
            } else if (srt != null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Info for super-region type " + ChatColor.GOLD + args[1] + ":");
                player.sendMessage(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + srt.getMoneyRequirement() + ChatColor.GRAY +
                        ", Payout: " + ChatColor.GOLD + srt.getOutput());
                player.sendMessage(ChatColor.GRAY + "Power: " + ChatColor.GOLD + srt.getMaxPower() + " (+" + srt.getDailyPower() + "), " +
                        ChatColor.GRAY + "Charter: " + ChatColor.GOLD + srt.getCharter() + ChatColor.GRAY + ", Radius: " + ChatColor.GOLD + (int) Math.sqrt(srt.getRadius()));
                
                String message = ChatColor.GRAY + "Description: " + ChatColor.GOLD;
                int j=0;
                if (srt.getDescription() != null) {
                    String tempMess = srt.getDescription();
                    if (tempMess.length() + message.length() <= 55) {
                        player.sendMessage(message + tempMess);
                        tempMess = null;
                    }
                    while (tempMess != null && j<12) {
                        if (tempMess.length() > 53) {
                            message += tempMess.substring(0, 53);
                            player.sendMessage(message);
                            tempMess = tempMess.substring(53);
                            message = ChatColor.GOLD + "";
                            j++;
                        } else {
                            player.sendMessage(message + tempMess);
                            tempMess = null;
                            j++;
                        }
                    }
                }
                message = ChatColor.GRAY + "Effects: " + ChatColor.GOLD;
                if (srt.getEffects() != null) {
                    for (String is : srt.getEffects()) {
                        String addLine = is + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 11) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (srt == null || srt.getEffects().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
                message = ChatColor.GRAY + "Requirements: " + ChatColor.GOLD;
                if (srt.getRequirements() != null) {
                    for (String is : srt.getRequirements().keySet()) {
                        String addLine = is + ":" + srt.getRequirement(is) + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (srt.getRequirements() == null || srt.getRequirements().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
                message = ChatColor.GRAY + "Evolves from: " + ChatColor.GOLD;
                if (srt.getChildren() != null) {
                    for (String is : srt.getChildren()) {
                        String addLine = is + ", ";
                        if (message.length() + addLine.length() > 55) {
                            player.sendMessage(message.substring(0, message.length() - 2));
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j < 12) {
                            message += addLine;
                        } else {
                            break;
                        }
                    }
                }
                if (srt.getChildren() == null || srt.getChildren().isEmpty()) {
                    message += "None";
                    player.sendMessage(message);
                } else {
                    player.sendMessage(message.substring(0, message.length()-2));
                }
            }
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("addowner")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer != null) {
                playername = aPlayer.getName();
            }
            
            Location loc = player.getLocation();
            for (Region r : regionManager.getContainingBuildRegions(loc)) {
                if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                    if (r.isOwner(playername)) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " is already an owner of this region.");
                        return true;
                    }
                    if (r.isMember(playername)) {
                        regionManager.setMember(r, playername);
                    }
                    regionManager.setOwner(r, playername);
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " as an owner.");
                    if (aPlayer != null) {
                        aPlayer.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "You're now a co-owner of " + player.getDisplayName() + "'s " + r.getType());
                    }
                    return true;
                } else {
                    boolean takeover = false;
                    for (SuperRegion sr : regionManager.getContainingSuperRegions(loc)) {
                        if (!sr.hasOwner(player.getName())) {
                            takeover = false;
                            break;
                        }
                        if (regionManager.getSuperRegionType(sr.getType()).hasEffect("control")) {
                            takeover = true;
                        }
                    }
                    if (takeover) {
                        if (r.isOwner(playername)) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " is already an owner of this region.");
                            return true;
                        }
                        if (r.isMember(playername)) {
                            regionManager.setMember(r, playername);
                        }
                        regionManager.setOwner(r, playername);
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " as an owner.");
                        if (aPlayer != null) {
                            aPlayer.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "You're now a co-owner of " + player.getDisplayName() + "'s " + r.getType());
                        }
                        return true;
                    } else {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                        return true;
                    }
                }
            }
            
            
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("addmember")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer == null) {
                SuperRegion sr = regionManager.getSuperRegion(args[1]);
                if (sr == null) {
                    playername = args[1];
                } else {
                    playername = "sr:" + sr.getName();
                }
            } else {
                playername = aPlayer.getName();
            }
            Location loc = player.getLocation();
            for (Region r : regionManager.getContainingBuildRegions(loc)) {
                if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                    if (r.isMember(playername)) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " is already a member of this region.");
                        return true;
                    }
                    if (r.isOwner(playername) && !(playername.equals(player.getName()) && r.getOwners().get(0).equals(player.getName()))) {
                        regionManager.setOwner(r, playername);
                    }
                    regionManager.setMember(r, playername);
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " to the region.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                    return true;
                }
            }
            
            
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length > 2 && args[0].equals("addmemberid")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer == null) {
                SuperRegion sr = regionManager.getSuperRegion(args[1]);
                if (sr == null) {
                    playername = args[1];
                } else {
                    playername = "sr:" + sr.getName();
                }
            } else {
                playername = aPlayer.getName();
            }
            Region r = null;
            try {
                r = regionManager.getRegionByID(Integer.parseInt(args[2]));
                r.getType();
            } catch (Exception e) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + args[1] + " is not a valid id");
                return true;
            }
            if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                if (r.isMember(playername)) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " is already a member of this region.");
                    return true;
                }
                if (r.isOwner(playername) && !(playername.equals(player.getName()) && r.getOwners().get(0).equals(player.getName()))) {
                    regionManager.setOwner(r, playername);
                }
                regionManager.setMember(r, playername);
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " to the region.");
                return true;
            } else {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                return true;
            }
        } else if (args.length > 1 && args[0].equalsIgnoreCase("whereis")) {
            RegionType rt = regionManager.getRegionType(args[1]);
            if (rt == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no region type " + args[1]);
                return true;
            }
            boolean found = false;
            for (Region r : regionManager.getSortedRegions()) {
                if (r.isOwner(player.getName()) && r.getType().equals(args[1])) {
                    player.sendMessage(ChatColor.GOLD + "[HeroStronghold] " + args[1] + " at " + ((int) r.getLocation().getX())
                            + ", " + ((int) r.getLocation().getY()) + ", " + ((int) r.getLocation().getZ()));
                    found = true;
                }
            }
            if (!found) {
                player.sendMessage(ChatColor.GOLD + "[HeroStronghold] " + args[1] + " not found.");
            }
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("setowner")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer != null) {
                playername = aPlayer.getName();
            } else {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " must be online to setowner");
                return true;
            }
            
            Location loc = player.getLocation();
            for (Region r : regionManager.getContainingBuildRegions(loc)) {
                if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                    //Check if too far away
                    try {
                        if (player.getLocation().distanceSquared(aPlayer.getLocation()) > regionManager.getRegionType(r.getType()).getRawRadius()) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " must be close by also.");
                            return true;
                        }
                    } catch (IllegalArgumentException iae) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " must be close by also.");
                        return true;
                    }
                    
                    if (r.isMember(playername)) {
                        regionManager.setMember(r, playername);
                    }
                    regionManager.setMember(r, player.getName());
                    regionManager.setOwner(r, player.getName());
                    regionManager.setPrimaryOwner(r, playername);
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " as an owner.");
                    if (aPlayer != null) {
                        aPlayer.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "You're now a co-owner of " + player.getDisplayName() + "'s " + r.getType());
                    }
                    return true;
                } else {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                    return true;
                }
            }
            
            
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("remove")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer != null) {
                playername = aPlayer.getName();
            }
            Location loc = player.getLocation();
            for (Region r : regionManager.getContainingBuildRegions(loc)) {
                if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                    if (r.isPrimaryOwner(playername)) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You must use /hs setowner to change the original owner.");
                        return true;
                    }
                    if (!r.isMember(playername) && !r.isOwner(playername)) {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " doesn't belong to this region");
                        return true;
                    }
                    if (r.isMember(playername)) {
                        regionManager.setMember(r, playername);
                    } else if (r.isOwner(playername)) {
                        regionManager.setOwner(r, playername);
                    }
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Removed " + playername + " from the region.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                    return true;
                }
            }
            
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("destroy")) {
            Location loc = player.getLocation();
            Location locationToDestroy = null;
            for (Region r : regionManager.getContainingRegions(loc)) {
                if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                    regionManager.destroyRegion(r.getLocation());
                    locationToDestroy = r.getLocation();
                    break;
                } else {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                    return true;
                }
            }
            
            if (locationToDestroy != null) {
                regionManager.removeRegion(locationToDestroy);
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Region destroyed.");
            } else {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            }
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("destroy")) {
            //Check if valid region
            SuperRegion sr = regionManager.getSuperRegion(args[1]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no region named " + args[1]);
                return true;
            }
            
            //Check if owner or admin of that region
            if ((perms == null || !perms.has(player, "herostronghold.admin")) && (sr.getOwners().isEmpty() || !sr.getOwners().contains(player.getName()))) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not the owner of that region.");
                return true;
            }
            
            regionManager.destroySuperRegion(args[1], true);
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            int j=0;
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] list of Region Types");
            String message = ChatColor.GOLD + "";
            boolean permNull = perms == null;
            boolean createAll = permNull || perms.has(player, "herostronghold.create.all");
            for (String s : regionManager.getRegionTypes()) {
                if (createAll || permNull || perms.has(player, "herostronghold.create." + s)) {
                    if (message.length() + s.length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += s + ", ";
                    }
                }
            }
            for (String s : regionManager.getSuperRegionTypes()) {
                if (createAll || permNull || perms.has(player, "herostronghold.create." + s)) {
                    if (message.length() + s.length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += s + ", ";
                    }
                }
            }
            if (!message.equals(ChatColor.GOLD + "")) {
                player.sendMessage(message.substring(0, message.length() - 2));
            }
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("rename")) {
            //Check if valid super-region
            SuperRegion sr = regionManager.getSuperRegion(args[1]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no super-region by that name");
                return true;
            }
            
            //Check if valid name
            if (args[2].length() > 16 && Util.validateFileName(args[2])) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That name is too long. Use 15 characters or less");
                return true;
            }
            
            //Check if player can rename the super-region
            if (!sr.hasOwner(player.getName()) && !HeroStronghold.perms.has(player, "herostronghold.admin")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't have permission to rename that super-region.");
                return true;
            }
            
            double cost = configManager.getRenameCost();
            if (HeroStronghold.econ != null && cost > 0) {
                if (!HeroStronghold.econ.has(player.getName(), cost)) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] It costs " + ChatColor.RED + cost + " to rename that.");
                    return true;
                } else {
                    HeroStronghold.econ.withdrawPlayer(player.getName(), cost);
                }
            }
            
            regionManager.destroySuperRegion(args[1], false);
            regionManager.addSuperRegion(args[2], sr.getLocation(), sr.getType(), sr.getOwners(), sr.getMembers(), sr.getPower(), sr.getBalance());
            player.sendMessage(ChatColor.GOLD + "[HeroStronghold] " + args[1] + " is now " + args[2]);
            return true;
        } else if (args.length > 0 && (args[0].equalsIgnoreCase("show"))) {
            
            return true;
        } else if (args.length > 0 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("who"))) {
            if (args.length == 1) {
                Location loc = player.getLocation();
                for (Region r : regionManager.getContainingBuildRegions(loc)) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] ==:|" + ChatColor.GOLD + r.getID() + " (" + r.getType() + ") " + ChatColor.GRAY + "|:==");
                    String message = ChatColor.GRAY + "Owners: " + ChatColor.GOLD;
                    int j = 0;
                    for (String s : r.getOwners()) {
                        if (message.length() + s.length() + 2 > 55) {
                            player.sendMessage(message);
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j > 14) {
                          break;  
                        } else {
                            message += s + ", ";
                        }
                    }
                    if (!r.getOwners().isEmpty()) {
                        player.sendMessage(message.substring(0, message.length() - 2));
                    } else {
                        player.sendMessage(message);
                    }
                    message = ChatColor.GRAY + "Members: " + ChatColor.GOLD;
                    for (String s : r.getMembers()) {
                        if (message.length() + 2 + s.length() > 55) {
                            player.sendMessage(message);
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j > 14) {
                            break;
                        } else {
                            message += s + ", ";
                        }
                    }
                    if (!r.getMembers().isEmpty()) {
                        player.sendMessage(message.substring(0, message.length() - 2));
                    } else {
                        player.sendMessage(message);
                    }
                    return true;
                }

                
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There are no regions here.");
                return true;
            }
            
            SuperRegion sr = regionManager.getSuperRegion(args[1]);
            
            if (sr != null) {
                //TODO make revenue include all owned regions within the super region
                SuperRegionType srt = regionManager.getSuperRegionType(sr.getType());
                int population = sr.getOwners().size() + sr.getMembers().size();
                double revenue = sr.getTaxes() * population + srt.getOutput();
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] ==:|" + ChatColor.GOLD + sr.getName() + " (" + sr.getType() + ") " + ChatColor.GRAY + "|:==");
                player.sendMessage(ChatColor.GRAY + "Population: " + ChatColor.GOLD + population + ChatColor.GRAY +
                        " Bank: " + (sr.getBalance() < srt.getOutput() ? ChatColor.RED : ChatColor.GOLD) + sr.getBalance() + ChatColor.GRAY +
                        " Power: " + (sr.getPower() < srt.getDailyPower() ? ChatColor.RED : ChatColor.GOLD) + sr.getPower() + 
                        " (+" + srt.getDailyPower() + ") / " + srt.getMaxPower());
                player.sendMessage(ChatColor.GRAY + "Taxes: " + ChatColor.GOLD + sr.getTaxes()
                        + ChatColor.GRAY + " Total Revenue: " + (revenue < 0 ? ChatColor.RED : ChatColor.GOLD) + revenue +
                        ChatColor.GRAY + " Disabled: " + (regionManager.hasAllRequiredRegions(sr) ? (ChatColor.GOLD + "false") : (ChatColor.RED + "true")));
                //TODO state why the sr is disabled
                if (sr.hasMember(player.getName()) || sr.hasOwner(player.getName())) {
                    player.sendMessage(ChatColor.GRAY + "Location: " + ChatColor.GOLD + (int) sr.getLocation().getX() + ", " + (int) sr.getLocation().getY() + ", " + (int) sr.getLocation().getZ());
                }
                if (sr.getTaxes() != 0) {
                    String message = ChatColor.GRAY + "Tax Revenue History: " + ChatColor.GOLD;
                    for (double d : sr.getTaxRevenue()) {
                        message += d + ", ";
                    }
                    if (!sr.getTaxRevenue().isEmpty()) {
                        player.sendMessage(message.substring(0, message.length() - 2));
                    } else {
                        player.sendMessage(message);
                    }
                }
                String message = ChatColor.GRAY + "Owners: " + ChatColor.GOLD;
                int j = 0;
                for (String s : sr.getOwners()) {
                    if (message.length() + s.length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                      break;  
                    } else {
                        message += s + ", ";
                    }
                }
                if (!sr.getOwners().isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                } else {
                    player.sendMessage(message);
                }
                message = ChatColor.GRAY + "Members: " + ChatColor.GOLD;
                for (String s : sr.getMembers().keySet()) {
                    if (message.length() + 2 + s.length() > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                        break;
                    } else {
                        message += s + ", ";
                    }
                }
                if (!sr.getMembers().isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                } else {
                    player.sendMessage(message);
                }
                message = ChatColor.GRAY + "Wars: " + ChatColor.GOLD;
                for (SuperRegion srr : regionManager.getWars(sr)) {
                    String s = srr.getName();
                    if (message.length() + s.length() + 2 > 55) {
                        player.sendMessage(message);
                        message = ChatColor.GOLD + "";
                        j++;
                    }
                    if (j > 14) {
                      break;  
                    } else {
                        message += s + ", ";
                    }
                }
                if (!sr.getOwners().isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                } else {
                    player.sendMessage(message);
                }
                return true;
            }
            
            Player p = getServer().getPlayer(args[1]);
            if (p != null) {
                String playername = p.getName();
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + p.getDisplayName() + " is a member of:");
                String message = ChatColor.GOLD + "";
                int j = 0;
                for (SuperRegion sr1 : regionManager.getSortedSuperRegions()) {
                    if (sr1.hasOwner(playername) || sr1.hasMember(playername)) {
                        if (message.length() + sr1.getName().length() + 2 > 55) {
                            player.sendMessage(message);
                            message = ChatColor.GOLD + "";
                            j++;
                        }
                        if (j > 14) {
                            break;
                        } else {
                            message += sr1.getName() + ", ";
                        }
                    }
                }
                if (!regionManager.getSortedRegions().isEmpty()) {
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
                return true;
            }
            
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] Could not find player or super-region by that name");
            return true;
        } else if (args.length > 0 && effectCommands.contains(args[0])) {
            Bukkit.getServer().getPluginManager().callEvent(new CommandEffectEvent(args, player));
            return true;
        } else {
            //TODO add a page 3 to help for more instruction?
            if (args.length > 0 && args[args.length - 1].equals("2")) {
                sender.sendMessage(ChatColor.GRAY + "[HeroStronghold] by " + ChatColor.GOLD + "Multitallented" + ChatColor.GRAY + ": <> = required, () = optional" +
                        ChatColor.GOLD + " Page 2");
                sender.sendMessage(ChatColor.GRAY + "/hs accept <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs rename <name> <newname>");
                sender.sendMessage(ChatColor.GRAY + "/hs settaxes <amount> <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs withdraw|deposit <amount> <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs listperms <playername> <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs listallperms");
                sender.sendMessage(ChatColor.GRAY + "/hs toggleperm <playername> <perm> <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs destroy (name)");
                sender.sendMessage(ChatColor.GRAY + "/hs ch (channel) -- Use /hs ch for all chat");
                sender.sendMessage(ChatColor.GRAY + "Google 'HeroStronghold bukkit' for more info | " + ChatColor.GOLD + "Page 2/3");
            } else if (args.length > 0 && args[args.length - 1].equals("3")) {
                sender.sendMessage(ChatColor.GRAY + "[HeroStronghold] by " + ChatColor.GOLD + "Multitallented" + ChatColor.GRAY + ": <> = required, () = optional" +
                        ChatColor.GOLD + " Page 3");
                sender.sendMessage(ChatColor.GRAY + "/hs war <mysuperregion> <enemysuperregion>");
                sender.sendMessage(ChatColor.GRAY + "/hs peace <mysuperregion> <enemysuperregion>");
                sender.sendMessage(ChatColor.GRAY + "Google 'HeroStronghold bukkit' for more info | " + ChatColor.GOLD + "Page 3/3");
            } else {
                sender.sendMessage(ChatColor.GRAY + "[HeroStronghold] by " + ChatColor.GOLD + "Multitallented" + ChatColor.GRAY + ": () = optional" +
                        ChatColor.GOLD + " Page 1");
                sender.sendMessage(ChatColor.GRAY + "/hs list");
                sender.sendMessage(ChatColor.GRAY + "/hs info <regiontype|superregiontype>");
                sender.sendMessage(ChatColor.GRAY + "/hs charter <superregiontype> <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs charterstats <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs signcharter <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs cancelcharter <name>");
                sender.sendMessage(ChatColor.GRAY + "/hs create <regiontype> (name)");
                sender.sendMessage(ChatColor.GRAY + "/hs addowner|addmember|remove <playername> (name)");
                sender.sendMessage(ChatColor.GRAY + "/hs whatshere");
                sender.sendMessage(ChatColor.GRAY + "Google 'HeroStronghold bukkit' for more info |" + ChatColor.GOLD + " Page 1/3");
            }
            
            return true;
        }
    }
    
    public void addCommand(String command) {
        effectCommands.add(command);
    }
    
    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
            if (econ != null)
                System.out.println("[HeroStronghold] Hooked into " + econ.getName());
        }
        return econ != null;
    }
    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
            if (perms != null)
                System.out.println("[HeroStronghold] Hooked into " + perms.getName());
        }
        return (perms != null);
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public void warning(String s) {
        String warning = "[HeroStronghold] " + s;
        Logger.getLogger("Minecraft").warning(warning);
    }
    
    public void setConfigManager(ConfigManager cm) {
        configManager = cm;
    }
    
    public void setCharters(Map<String, List<String>> input) {
        this.pendingCharters = input;
    }
    
}
