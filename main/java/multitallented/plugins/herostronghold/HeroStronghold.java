package main.java.multitallented.plugins.herostronghold;
/**
 *
 * @author Multitallented
 */
import com.herocraftonline.dev.heroes.Heroes;
import java.util.ArrayList;
import java.util.logging.Logger;
import main.java.multitallented.plugins.herostronghold.listeners.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroStronghold extends JavaPlugin {
    private PluginServerListener serverListener;
    private Logger log;
    protected FileConfiguration config;
    private RegionManager regionManager;
    private RegionBlockListener blockListener;
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
        blockListener = new RegionBlockListener(this);
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
        
        //Setup repeating sync task for checking regions
        //CheckRegionTask theSender = new CheckRegionTask(getServer());
        //long someInterval = 6000L;
        //getServer().getScheduler().scheduleSyncRepeatingTask(this, theSender, someInterval, someInterval);
        
        log.info("[HeroStronghold] is now enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.GRAY + "[HeroStronghold]" + ChatColor.WHITE + " doesn't recognize non-player commands.");
            return true;
        }
        Player player = (Player) sender;
        System.out.println("[HeroStronghold] " + player.getDisplayName() + ": " + label);
        if (args.length > 1 && args[0].equalsIgnoreCase("create")) {
            String regionName = args[1];
            //Permission Check
            if (!player.hasPermission("herostronghold.create." + regionName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionName);
                return true;
            }
            //TODO check player money?
            
            
            Location currentLocation = player.getLocation();
            //Check if player is standing someplace where a chest can be placed.
            BlockState currentBlockState = currentLocation.getBlock().getState();
            if (currentBlockState.getTypeId() != 0) {
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
            
            int radius = currentRegionType.getRadius();
            ArrayList<ItemStack> requirements = (ArrayList<ItemStack>) currentRegionType.getRequirements().clone();
            //Check the area for required blocks
            outer: for (int x=((int) currentLocation.getX()-radius); x<Math.abs(radius + currentLocation.getY()); x++) {
                for (int y = currentLocation.getY()- radius > -128 ? ((int) currentLocation.getY() - radius) : -128; y< Math.abs((radius) + currentLocation.getY()) && (y + currentLocation.getY() < 128); y++) {
                    for (int z = ((int) currentLocation.getZ() - radius); z<Math.abs(radius + currentLocation.getZ()); z++) {
                        for (ItemStack is : requirements) {
                            if (currentLocation.getWorld().getBlockAt(x, y, z).getType().equals(is.getType())) {
                                if (is.getAmount() == 1) {
                                    requirements.remove(is);
                                    if (requirements.isEmpty())
                                        break outer;
                                } else {
                                    is.setAmount(is.getAmount() -1 );
                                }
                            }
                        }
                    }
                }
            }
            if (!requirements.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have all of the required blocks in this structure.");
                String message = ChatColor.GRAY + "[HeroStronghold] ";
                for (ItemStack is : requirements) {
                    message += is.getType().name() + ", ";
                }
                player.sendMessage(message.substring(0, message.length() - 2));
                return true;
            }
            
            //Create chest at players feet for tracking reagents and removing upkeep items
            currentBlockState.setType(Material.CHEST);
            
            ArrayList<String> owners = new ArrayList<String>();
            owners.add(player.getName());
            regionManager.addRegion(new Region(currentLocation, regionName, owners, new ArrayList<String>()));
        }
        
        /*if (args.length > 0 && args[0].equalsIgnoreCase("destroy")) {
            if (!player.hasPermission("herostronghold.destroy")) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you don't have permission to destroy this structure");
                return true;
            }
            Location currentLoc = player.getLocation();
            Location location;
            boolean destroy = false;
            exit: for (Location loc : regionManager.getRegionLocations()) {
                location = loc;
                RegionType currentRegionType = regionManager.getRegionType(regionManager.getRegion(loc).getType());
                int radius = currentRegionType.getRadius();
                if (Math.sqrt(currentLoc.distanceSquared(loc)) < radius) {
                    ArrayList<ItemStack> requirements = currentRegionType.getRequirements();
                    for (int x=((int) currentLoc.getX()-radius); x<Math.abs(radius + currentLoc.getY()); x++) {
                        for (int y = currentLoc.getY()- radius > -128 ? ((int) currentLoc.getY() - radius) : -128; y< Math.abs((radius) + currentLoc.getY()) && (y + currentLoc.getY() < 128); y++) {
                            for (int z = ((int) currentLoc.getZ() - radius); z<Math.abs(radius + currentLoc.getZ()); z++) {
                                for (ItemStack is : requirements) {
                                    if (currentLoc.getWorld().getBlockAt(x, y, z).getType().equals(is.getType())) {
                                        if (is.getAmount() == 1) {
                                            requirements.remove(is);
                                            if (requirements.isEmpty()) {
                                                destroy = true;
                                                break exit;
                                            }
                                        } else {
                                            is.setAmount(is.getAmount() -1 );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            //Destroy the structure
            
            
            //TODO handle herostrong destroy
        }*/
        
        //TODO handle herostrong addowner
        
        //TODO handle herostrong addmember
        
        //TODO handle herostrong remove
        
        return false;
    }
    
    public Heroes getHeroes() {
        if (serverListener == null)
            return null;
        return serverListener.getHeroes();
    }
    
    public void warning(String s) {
        Logger.getLogger("Minecraft").info(s);
    }
    
    
}
