package main.java.multitallented.plugins.herostronghold;
/**
 *
 * @author Multitallented
 */
import com.herocraftonline.dev.heroes.Heroes;
import java.util.logging.Logger;
import main.java.multitallented.plugins.herostronghold.listeners.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroStronghold extends JavaPlugin {
    private HeroStrongholdServerListener serverListener;
    private CommandListener commandHandler;
    private Logger log;
    protected FileConfiguration config;
    private RegionManager regionManager;
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
        serverListener = new HeroStrongholdServerListener(this);
        commandHandler = new CommandListener();
        PluginManager pm = getServer().getPluginManager();
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
        
        //Setup repeating thread for checking regions
        //MessageSender theSender = new MessageSender(this);
        //getServer().getScheduler().scheduleSyncRepeatingTask(this, theSender, someInterval, someInterval);
        
        log.info("[HeroStronghold] is now enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return commandHandler.dispatch(sender, label, args);
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
