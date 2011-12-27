package main.java.multitallented.plugins.herostronghold;
/**
 *
 * @author Multitallented
 */
import com.herocraftonline.dev.heroes.Heroes;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import main.java.multitallented.plugins.herostronghold.listeners.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
    
    @Override
    public void onDisable() {
        log = Logger.getLogger("Minecraft");
        log.info("[HeroStronghold] is now disabled!");
    }

    @Override
    public void onEnable() {
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
        dpeListener = new RegionPlayerInteractListener(regionManager);
        regionEntityListener = new RegionEntityListener(regionManager);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
        pm.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.High, this);
        pm.registerEvent(Type.BLOCK_DAMAGE, blockListener, Priority.High, this);
        pm.registerEvent(Type.BLOCK_FROMTO, blockListener, Priority.Normal, this);
        pm.registerEvent(Type.BLOCK_IGNITE, blockListener, Priority.High, this);
        pm.registerEvent(Type.BLOCK_BURN, blockListener, Priority.High, this);
        pm.registerEvent(Type.SIGN_CHANGE, blockListener, Priority.High, this);
        pm.registerEvent(Type.BLOCK_PISTON_EXTEND, blockListener, Priority.High, this);
        pm.registerEvent(Type.BLOCK_PISTON_RETRACT, blockListener, Priority.High, this);
        
        pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
        pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
        
        pm.registerEvent(Type.PAINTING_PLACE, regionEntityListener, Priority.High, this);
        pm.registerEvent(Type.ENDERMAN_PLACE, regionEntityListener, Priority.High, this);
        pm.registerEvent(Type.PAINTING_BREAK, regionEntityListener, Priority.High, this);
        pm.registerEvent(Type.EXPLOSION_PRIME, regionEntityListener, Priority.High, this);
        pm.registerEvent(Type.ENTITY_EXPLODE, regionEntityListener, Priority.High, this);
        pm.registerEvent(Type.ENDERMAN_PICKUP, regionEntityListener, Priority.High, this);
        
        pm.registerEvent(Type.PLAYER_INTERACT, dpeListener, Priority.High, this);
        pm.registerEvent(Type.PLAYER_BED_ENTER, dpeListener, Priority.High, this);
        pm.registerEvent(Type.PLAYER_BUCKET_FILL, dpeListener, Priority.High, this);
        pm.registerEvent(Type.PLAYER_BUCKET_EMPTY, dpeListener, Priority.High, this);
        log = Logger.getLogger("Minecraft");
        
        //Check for Heroes
        log.info("[HeroStronghold] is looking for Heroes...");
        Plugin currentPlugin = pm.getPlugin("Heroes");
        if (currentPlugin != null) {
            log.info("[HeroStronghold] found Heroes!");
            serverListener.setupHeroes((Heroes) currentPlugin);
        } else {
            log.info("[HeroStronghold] didnt find Heroes, waiting for Heroes to be enabled.");
        }
        
        new EffectManager(this);
        
        //Setup repeating sync task for checking regions
        CheckRegionTask theSender = new CheckRegionTask(getServer(), regionManager);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, theSender, 40L, 40L);
        
        log.info("[HeroStronghold] is now enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[HeroStronghold] doesn't recognize non-player commands.");
            return true;
        }
        Player player = (Player) sender;
        
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            String regionName = args[1];
            //Permission Check
            if (perms != null && !perms.has(player.getWorld(), player.getName(), "herostronghold.create.all") &&
                    !perms.has(player.getWorld(), player.getName(), "herostronghold.create." + regionName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionName);
                return true;
            }
            /*if (!player.hasPermission("herostronghold.create.all") || !player.hasPermission("herostronghold.create." + regionName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionName);
                return true;
            }*/
            
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
                String message = ChatColor.GRAY + "[HeroStronghold] ";
                for (String s : regionManager.getRegionTypes()) {
                    if (perms == null || (perms.has(player.getWorld(),player.getName(), "herostronghold.create.all") ||
                            perms.has(player.getWorld(), player.getName(), "herostronghold.create." + s)))
                        message += s + ", ";
                }
                message = message.substring(0, message.length() - 2);
                player.sendMessage(message);
                return true;
            }
            
            //Check if player can afford to create this herostronghold
            if (econ != null) {
                double cost = currentRegionType.getMoneyRequirement();
                if (econ.getBalance(player.getName()) < cost) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need $" + cost + " to make this type of structure.");
                    return true;
                } else {
                    econ.withdrawPlayer(player.getName(), cost);
                }
                
            }
            
            //Check if too close to other HeroStrongholds
            for (Location loc : regionManager.getRegionLocations()) {
                if (loc.distanceSquared(currentLocation) <= 2 * regionManager.getRegionType(regionManager.getRegion(loc).getType()).getRadius()) {
                    player.sendMessage (ChatColor.GRAY + "[HeroStronghold] You are too close to another HeroStronghold");
                    return true;
                }
            }
            
            int radius = currentRegionType.getRadius();
            ArrayList<ItemStack> requirements = (ArrayList<ItemStack>) currentRegionType.getRequirements();
            Map<Material, Integer> reqMap = new EnumMap<Material, Integer>(Material.class);
            for (ItemStack currentIS : requirements) {
                reqMap.put(currentIS.getType(), currentIS.getAmount());
            }
            //Check the area for required blocks
            if (!requirements.isEmpty()) {
                outer: for (int x= (int) (currentLocation.getX()-radius); x< radius + currentLocation.getX(); x++) {
                    for (int y = currentLocation.getY() - radius > 1 ? (int) (currentLocation.getY() - radius) : 1; y < radius + currentLocation.getY() && y < 128; y++) {
                        for (int z = ((int) currentLocation.getZ() - radius); z<Math.abs(radius + currentLocation.getZ()); z++) {
                            if (currentLocation.getWorld().getBlockAt(x, y, z).getTypeId() != 0) {
                                for (Iterator<Material> iter = reqMap.keySet().iterator(); iter.hasNext(); ) {
                                    Material mat = iter.next();
                                    if (currentLocation.getWorld().getBlockAt(x, y, z).getType().equals(mat)) {
                                        if (reqMap.get(mat) <= 1) {
                                            reqMap.remove(mat);
                                            if (requirements.isEmpty())
                                                break outer;
                                        } else {
                                            reqMap.put(mat, reqMap.get(mat) - 1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!reqMap.isEmpty()) {
                //TODO fix this message
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have all of the required blocks in this structure.");
                String message = ChatColor.GRAY + "[HeroStronghold] ";
                for (Material mat : reqMap.keySet()) {
                    message += reqMap.get(mat) + " " + mat.name() + ", ";
                }
                player.sendMessage(message.substring(0, message.length() - 2));
                return true;
            }
            
            //Create chest at players feet for tracking reagents and removing upkeep items
            currentBlock.setType(Material.CHEST);
            
            ArrayList<String> owners = new ArrayList<String>();
            owners.add(player.getName());
            regionManager.addRegion(currentLocation, regionName, owners);
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "You successfully create a " + ChatColor.RED + regionName);
            
            //TODO fix this message
            //Tell the player what reagents are required for it to work
            String message = ChatColor.GOLD + "[HeroStronghold] Reagents: ";
            for (ItemStack is : currentRegionType.getReagents()) {
                message += is.getAmount() + ":" + is.getType().name() + ", ";
            }
            if (currentRegionType.getReagents().isEmpty()) {
                message += "None";
            } else {
                message = message.substring(0, message.length()-3);
            }
            player.sendMessage(message);
            
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("create")) {
            if (args[2].length() > 50) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That name is too long.");
                return true;
            }
            String regionTypeName = args[1];
            //Permission Check
            if (perms != null && !perms.has(player.getWorld(), player.getName(), "herostronghold.create.all") &&
                    !perms.has(player.getWorld(), player.getName(), "herostronghold.create." + regionTypeName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionTypeName);
                return true;
            }
            
            Location currentLocation = player.getLocation();
            SuperRegionType currentRegionType = regionManager.getSuperRegionType(regionTypeName);
            if (currentRegionType == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + regionTypeName + " isnt a valid region type");
                //TODO fix this message
                String message = ChatColor.GRAY + "[HeroStronghold] ";
                for (String s : regionManager.getRegionTypes()) {
                    if (perms == null || (perms.has(player.getWorld(),player.getName(), "herostronghold.create.all") ||
                            perms.has(player.getWorld(), player.getName(), "herostronghold.create." + s)))
                        message += s + ", ";
                }
                message = message.substring(0, message.length() - 2);
                player.sendMessage(message);
                return true;
            }
            
            //Check if player can afford to create this herostronghold
            if (econ != null) {
                double cost = currentRegionType.getMoneyRequirement();
                if (econ.getBalance(player.getName()) < cost) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need $" + cost + " to make this type of HeroStronghold.");
                    return true;
                } else {
                    econ.withdrawPlayer(player.getName(), cost);
                }
                
            }
            
            int radius = currentRegionType.getRadius();
            Map<String, Integer> requirements = currentRegionType.getRequirements();
            
            //Check for required regions
            
            if (!requirements.isEmpty()) {
                for (Location l : regionManager.getSuperRegionLocations()) {
                    try {
                        if (Math.sqrt(l.distanceSquared(currentLocation)) < radius) {
                            SuperRegion sr = regionManager.getSuperRegion(l);
                            String rType = sr.getType();
                            if (requirements.containsKey(rType)) {
                                int amount = requirements.get(rType);
                                if (amount <= 1) {
                                    requirements.remove(rType);
                                } else {
                                    requirements.put(rType, amount - 1);
                                }
                            }
                        }
                    } catch (IllegalArgumentException e) {

                    }
                }
                
                if (!requirements.isEmpty()) {
                    for (Location l : regionManager.getRegionLocations()) {
                        try {
                            if (Math.sqrt(l.distanceSquared(currentLocation)) < radius) {
                                String rType = regionManager.getRegion(l).getType();
                                if (requirements.containsKey(rType)) {
                                    int amount = requirements.get(rType);
                                    if (amount <= 1) {
                                        requirements.remove(rType);
                                    } else {
                                        requirements.put(rType, amount - 1);
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {

                        }
                    }
                }
            }
            
            if (!requirements.isEmpty()) {
                //TODO fix this message
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have all of the required blocks in this structure.");
                String message = ChatColor.GRAY + "[HeroStronghold] ";
                for (String s : requirements.keySet()) {
                    message += requirements.get(s) + " " + s + ", ";
                }
                player.sendMessage(message.substring(0, message.length() - 2));
                return true;
            }
            
            List<String> children = currentRegionType.getChildren();
            List<String> owners = new ArrayList<String>();
            Map<String, List<String>> members = new HashMap<String, List<String>>();
            List<Location> quietDestroy = new ArrayList<Location>();
            for (Location l : regionManager.getSuperRegionLocations()) {
                try {
                    if (Math.sqrt(l.distanceSquared(currentLocation)) < radius) {
                        SuperRegion sr = regionManager.getSuperRegion(l);
                        if (children.contains(sr.getType()) && sr.hasOwner(player.getName())) {
                            for (String s : sr.getOwners()) {
                                if (!owners.contains(s))
                                    owners.add(s);
                            }
                            for (String s : sr.getMembers().keySet()) {
                                if (!members.containsKey(s))
                                    members.put(s, sr.getMember(s));
                            }
                            quietDestroy.add(l);
                        }
                    }
                } catch (IllegalArgumentException e) {

                }
            }
            for (Location l : quietDestroy) {
                regionManager.destroySuperRegion(l, false);
            }
            String playername = player.getName();
            if (!owners.contains(playername))
                owners.add(playername);
            regionManager.addSuperRegion(args[2], currentLocation, regionTypeName, owners, members);
            
            //TODO create listjobs command
            //TODO create an invite command and confirm command
            //TODO create a kick command
            //TODO create a chat system
        } else if (args.length > 1 && args[0].equalsIgnoreCase("addowner")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer != null)
                playername = aPlayer.getName();
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player.getWorld(), player.getName(), "herostronghold.admin"))) {
                        if (r.isOwner(playername)) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " is already an owner of this region.");
                            return true;
                        }
                        if (r.isMember(playername))
                            r.remove(playername);
                        r.addOwner(playername);
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " to the region.");
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
            if (aPlayer != null)
                playername = aPlayer.getName();
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player.getWorld(), player.getName(), "herostronghold.admin"))) {
                        if (r.isMember(playername)) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " is already a member of this region.");
                            return true;
                        }
                        if (r.isOwner(playername))
                            r.remove(playername);
                        r.addMember(playername);
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Added " + playername + " to the region.");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                        return true;
                    }
                }
            }
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("remove")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer != null)
                playername = aPlayer.getName();
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player.getWorld(), player.getName(), "herostronghold.admin"))) {
                        if (!r.isMember(playername) && !r.isOwner(playername)) {
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " doesn't belong to this region");
                            return true;
                        }
                        r.remove(playername);
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "Removed " + playername + " from the region.");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                        return true;
                    }
                }
            }
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("destroy")) {
            Location loc = player.getLocation();
            Set<Location> locations = regionManager.getRegionLocations();
            for (Iterator<Location> iter = locations.iterator(); iter.hasNext();) {
                Location l = iter.next();
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player.getWorld(), player.getName(), "herostronghold.admin"))) {
                        
                        regionManager.destroyRegion(l);
                        iter.remove();
                        return true;
                    } else {
                        player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You don't own this region.");
                        return true;
                    }
                }
            }
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You're not standing in a region.");
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("list")) {
            //TODO fix this message
            String message = ChatColor.GRAY + "[HeroStronghold] ";
            for (String s : regionManager.getRegionTypes()) {
                if (perms == null || (perms.has(player.getWorld(),player.getName(), "herostronghold.create.all") ||
                        perms.has(player.getWorld(), player.getName(), "herostronghold.create." + s)))
                    message += s + ", ";
            }
            message = message.substring(0, message.length() - 2);
            player.sendMessage(message);
            return true;
        } else if ((args.length > 1 && args[0].equalsIgnoreCase("help")) || args.length == 1) {
            sender.sendMessage(ChatColor.GRAY + "[HeroStronghold] by Multitallented");
            sender.sendMessage(ChatColor.GRAY + "/herostrong list (lists all HSs you can make)");
            sender.sendMessage(ChatColor.GRAY + "/herostrong create <regiontype> (create a HS)");
            sender.sendMessage(ChatColor.GRAY + "/herostrong addowner|addmember|remove <playername> (manage members)");
            sender.sendMessage(ChatColor.GRAY + "/herostrong destroy (destroy your HS)");
            sender.sendMessage(ChatColor.GRAY + "Google 'HeroStronghold bukkit' for more info");
        }
        
        return false;
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
    private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
            if (perms != null)
                System.out.println("[HeroStronghold] Hooked into " + perms.getName());
        }
        return (perms != null);
    }
    
    public Heroes getHeroes() {
        if (serverListener == null)
            return null;
        return serverListener.getHeroes();
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public void warning(String s) {
        Logger.getLogger("Minecraft").warning("[HeroStronghold] " + s);
    }
    
    
}
