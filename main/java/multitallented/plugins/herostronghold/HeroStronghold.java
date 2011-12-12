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
import java.util.Map;
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
    private Permission perms;
    
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
        
        
        //Register Listeners Here
        serverListener = new PluginServerListener(this);
        blockListener = new RegionBlockListener(regionManager);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Low, this);
        pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Low, this);
        pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Low, this);
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
        
        //TODO handle /herostrong
        
        //TODO handle /herostrong help
        
        //TODO handle /herostrong list
        
        if (args.length > 1 && args[0].equalsIgnoreCase("create")) {
            String regionName = args[1];
            //Permission Check
            if (!player.hasPermission("herostronghold.create." + regionName)) {
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
                String message = ChatColor.GRAY + "[HeroStronghold] ";
                for (String s : regionManager.getRegionTypes()) {
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
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("addowner")) {
            String playername = args[1];
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName())) {
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
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName())) {
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
            Location loc = player.getLocation();
            for (Location l : regionManager.getRegionLocations()) {
                Region r = regionManager.getRegion(l);
                if (Math.sqrt(l.distanceSquared(loc)) < regionManager.getRegionType(r.getType()).getRadius()) {
                    if (r.isOwner(player.getName())) {
                        if (!r.isMember(playername) || !r.isOwner(playername)) {
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
        }
        
        return false;
    }
    
    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
        return econ != null;
    }
    
    public void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null)
            perms = rsp.getProvider();
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
        Logger.getLogger("Minecraft").info(s);
    }
    
    
}
