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
import org.bukkit.OfflinePlayer;
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
    private Map<String, String> pendingInvites = new HashMap<String, String>();
    
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
        
        //TODO add a chat Listener for chat channels with player titles
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
        getServer().getScheduler().scheduleSyncRepeatingTask(this, theSender, 10L, 10L);
        
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
            boolean nullPerms = perms == null;
            boolean createAll = nullPerms || perms.has(player, "herostronghold.create.all");
            if (!(nullPerms || createAll || perms.has(player, "herostronghold.create." + regionName))) {
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
                int j=0;
                String message = ChatColor.GOLD + "";
                for (String s : regionManager.getRegionTypes()) {
                    if (perms == null || (perms.has(player, "herostronghold.create.all") ||
                            perms.has(player, "herostronghold.create." + s))) {
                        message += s + ", ";
                        if (j >= 2) {
                            player.sendMessage(message.substring(0, message.length() - 3));
                            message = ChatColor.GOLD + "";
                            j=-1;
                        }
                        j++;
                    }
                }
                if (j!=0)
                    player.sendMessage(message.substring(0, message.length() - 3));
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
                try {
                    if (loc.distanceSquared(currentLocation) <= 2 * regionManager.getRegionType(regionManager.getRegion(loc).getType()).getRadius()) {
                        player.sendMessage (ChatColor.GRAY + "[HeroStronghold] You are too close to another HeroStronghold");
                        return true;
                    }
                } catch (IllegalArgumentException iae) {

                }
            }
            
            //Check if in a super region and if has permission to make that region
            String playername = player.getName();
            String currentRegionName = currentRegionType.getName();
            for (String s : regionManager.getSuperRegionNames()) {
                SuperRegion sr = regionManager.getSuperRegion(s);
                SuperRegionType srt = regionManager.getSuperRegionType(sr.getType());
                Location l = sr.getLocation();
                try {
                    if (Math.sqrt(l.distanceSquared(player.getLocation())) < srt.getRadius()) {
                        if (!sr.hasOwner(playername)) {
                            if (!sr.hasMember(playername) || !sr.getMember(playername).contains(currentRegionName)) {
                                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You dont have permission from an owner of " + s + " to create a " + currentRegionName + " here");
                                return true;
                            }
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    
                }
            }
            
            //Prepare a requirements checklist
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
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have all of the required blocks in this structure.");
                String message = ChatColor.GOLD + "";
                int j=0;
                for (Material mat : reqMap.keySet()) {
                    message += reqMap.get(mat) + " " + mat.name() + ", ";
                    if (j >= 2) {
                        player.sendMessage(message.substring(0, message.length() - 3));
                        message = ChatColor.GOLD + "";
                        j=-1;
                    }
                    j++;
                }
                if (j!=0)
                    player.sendMessage(message.substring(0, message.length() - 3));
                return true;
            }
            
            //Create chest at players feet for tracking reagents and removing upkeep items
            currentBlock.setType(Material.CHEST);
            
            ArrayList<String> owners = new ArrayList<String>();
            owners.add(player.getName());
            regionManager.addRegion(currentLocation, regionName, owners);
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + ChatColor.WHITE + "You successfully create a " + ChatColor.RED + regionName);
            
            //Tell the player what reagents are required for it to work
            String message = ChatColor.GOLD + "Reagents: ";
            int j=0;
            for (ItemStack is : currentRegionType.getReagents()) {
                message += is.getAmount() + ":" + is.getType().name() + ", ";
                if (j >= 2) {
                    player.sendMessage(message.substring(0, message.length()-3));
                    message = ChatColor.GOLD + "";
                    j=-1;
                }
                j++;
            }
            if (currentRegionType.getReagents().isEmpty()) {
                message += "None";
                player.sendMessage(message);
            } else if (j!= 0)
                player.sendMessage(message.substring(0, message.length()-3));
            
            return true;
        } else if (args.length > 2 && args[0].equalsIgnoreCase("create")) {
            if (args[2].length() > 50) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] That name is too long.");
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
                for (String s : regionManager.getRegionTypes()) {
                    if (perms == null || (perms.has(player, "herostronghold.create.all") ||
                            perms.has(player, "herostronghold.create." + s))) {
                        message += s + ", ";
                        if (j >= 2) {
                            player.sendMessage(message.substring(0, message.length() - 3));
                            message = ChatColor.GOLD + "";
                            j=-1;
                        }
                        j++;
                    }
                }
                if (j!= 0)
                    player.sendMessage(message.substring(0, message.length() - 3));
                return true;
            }
            
            //Check if player can afford to create this herostronghold
            if (econ != null) {
                double cost = currentRegionType.getMoneyRequirement();
                if (econ.getBalance(player.getName()) < cost) {
                    player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You need $" + cost + " to make this type of region.");
                    return true;
                } else {
                    econ.withdrawPlayer(player.getName(), cost);
                }
                
            }
            
            int radius = currentRegionType.getRadius();
            Map<String, Integer> requirements = currentRegionType.getRequirements();
            
            //Check for required regions
            List<String> children = currentRegionType.getChildren();
            for (String s : children) {
                if (!requirements.containsKey(s))
                    requirements.put(s, 1);
            }
            List<String> quietDestroy = new ArrayList<String>();
            if (!requirements.isEmpty()) {
                for (String s : regionManager.getSuperRegionNames()) {
                    SuperRegion sr = regionManager.getSuperRegion(s);
                    Location l = sr.getLocation();
                    try {
                        if (Math.sqrt(l.distanceSquared(currentLocation)) < radius) {
                            if (children.contains(sr.getType()) && sr.hasOwner(player.getName())) {
                                quietDestroy.add(s);
                            }
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
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have all of the required regions in this structure.");
                int j=0;
                String message = ChatColor.GOLD + "";
                for (String s : requirements.keySet()) {
                    message += requirements.get(s) + " " + s + ", ";
                    if (j>=2) {
                        player.sendMessage(message.substring(0, message.length() -3));
                        message = ChatColor.GOLD + "";
                        j=-1;
                    }
                    j++;
                }
                if (j!=0)
                    player.sendMessage(message.substring(0, message.length() -3));
                return true;
            }
            
            //Assimulate any child super regions
            List<String> owners = new ArrayList<String>();
            Map<String, List<String>> members = new HashMap<String, List<String>>();
            for (String s : quietDestroy) {
                SuperRegion sr = regionManager.getSuperRegion(s);
                for (String so : sr.getOwners()) {
                    if (!owners.contains(so))
                        owners.add(so);
                }
                for (String sm : sr.getMembers().keySet()) {
                    if (!members.containsKey(sm))
                        members.put(sm, sr.getMember(sm));
                }
                regionManager.destroySuperRegion(s, false);
            }
            String playername = player.getName();
            if (!owners.contains(playername))
                owners.add(playername);
            regionManager.addSuperRegion(args[2], currentLocation, regionTypeName, owners, members, currentRegionType.getMaxPower());
        } else if (args.length > 2 && args[0].equalsIgnoreCase("listjobs")) {
            //Get target player
            String playername = "";
            if (args.length > 3) {
                Player currentPlayer = getServer().getPlayer(args[2]);
                if (currentPlayer == null) {
                    OfflinePlayer op = getServer().getOfflinePlayer(args[2]);
                    if (op != null)
                        playername = op.getName();
                    else {
                        player.sendMessage(ChatColor.GOLD + "[HeroStronghold] Could not find " + args[2]);
                        return true;
                    }
                } else {
                    playername = currentPlayer.getName();
                }
            } else {
                playername = player.getName();
            }
            
            String message = ChatColor.GRAY + "[HeroStronghold] " + playername + " perms for " + args[3] + ":";
            String message2 = ChatColor.GOLD + "";
            //Check if the player is a owner or member of the super region
            for (String st : regionManager.getSuperRegionNames()) {
                SuperRegion sr = regionManager.getSuperRegion(st);
                if (sr.getName().equalsIgnoreCase(args[3])) {
                    if (sr.hasOwner(playername)) {
                        player.sendMessage(message);
                        player.sendMessage(message2 + "All Permissions");
                        return true;
                    } else if (sr.hasMember(playername)) {
                        player.sendMessage(message);
                        int j=0;
                        for (String s : sr.getMember(label)) {
                            message2 += s + ", ";
                            if (j >= 3) {
                                player.sendMessage(message2.substring(0, message2.length() - 3));
                                message2 = ChatColor.GOLD + "";
                                j = -1;
                            }
                            j++;
                        }
                        if (j != 0)
                            player.sendMessage(message2.substring(0, message2.length() - 3));
                    }
                }
            }
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] " + playername + " doesn't belong to that region.");
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("ch")) {
            //TODO create channel switching command
        } else if (args.length > 2 && args[0].equalsIgnoreCase("addmember")) {
            //TODO create an addmember superperm command
        } else if (args.length > 1 && args[0].equalsIgnoreCase("accept")) {
            //TODO create accept command
        } else if (args.length > 2 && args[0].equalsIgnoreCase("addowner")) {
            //TODO create addowner for super region
        } else if (args.length > 2 && args[0].equalsIgnoreCase("remove")) {
            //TODO create remove from super region command
        } else if (args.length > 3 && args[0].equalsIgnoreCase("toggleperm")) {
            //TODO create toggleperm command
        } else if (args.length > 1 && args[0].equalsIgnoreCase("addowner")) {
            String playername = args[1];
            Player aPlayer = getServer().getPlayer(playername);
            if (aPlayer != null)
                playername = aPlayer.getName();
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
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
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
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
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
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
        } else if (args.length == 1 && args[0].equalsIgnoreCase("destroy")) {
            Location loc = player.getLocation();
            Set<Location> locations = regionManager.getRegionLocations();
            for (Iterator<Location> iter = locations.iterator(); iter.hasNext();) {
                Location l = iter.next();
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName()) || (perms != null && perms.has(player, "herostronghold.admin"))) {
                        
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
        } else if (args.length > 1 && args[0].equalsIgnoreCase("destroy")) {
            //Check if valid region
            SuperRegion sr = regionManager.getSuperRegion(args[1]);
            if (sr == null) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] There is no region named " + args[1]);
                return true;
            }
            
            //Check if owner or admin of that region
            if (!(perms != null && perms.has(player, "herostronghold.admin") &&
                    !sr.getOwners().get(0).equalsIgnoreCase(player.getName()))) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] You are not the owner of that region.");
                return true;
            }
            
            regionManager.destroySuperRegion(args[1], true);
        } else if (args.length > 1 && args[0].equalsIgnoreCase("list")) {
            int j=0;
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] list of Region Types");
            String message = ChatColor.GOLD + "";
            boolean permNull = perms == null;
            boolean createAll = permNull || perms.has(player, "herostronghold.create.all");
            for (String s : regionManager.getRegionTypes()) {
                if (createAll || permNull || perms.has(player, "herostronghold.create." + s)) {
                    message += s + ", ";
                    if (j>=2) {
                        player.sendMessage(message.substring(0, message.length() - 3));
                        message = ChatColor.GOLD + "";
                        j=-1;
                    }
                    j++;
                }
            }
            if (j!=0)
                player.sendMessage(message.substring(0, message.length() - 3));
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
