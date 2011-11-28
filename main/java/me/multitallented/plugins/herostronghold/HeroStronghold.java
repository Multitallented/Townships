package main.java.me.multitallented.plugins.herostronghold;
/**
 *
 * @author Multitallented
 */
import com.herocraftonline.dev.heroes.Heroes;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.logging.Logger;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroStronghold extends JavaPlugin {
    private HeroStrongholdServerListener serverListener;
    private HeroStrongholdCommandListener commandHandler;
    @Override
    public void onDisable() {
        Logger log = Logger.getLogger("Minecraft");
        log.info("[HeroStronghold] is now disabled!");
    }

    @Override
    public void onEnable() {
        serverListener = new HeroStrongholdServerListener(this);
        commandHandler = new HeroStrongholdCommandListener();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.PLUGIN_ENABLE, serverListener, Priority.Low, this);
        pm.registerEvent(Type.PLUGIN_DISABLE, serverListener, Priority.Low, this);
        
        Logger log = Logger.getLogger("Minecraft");
        log.info("[HeroStronghold] is looking for Heroes and Worldguard...");
        Plugin currentPlugin = pm.getPlugin("Heroes");
        if (currentPlugin != null) {
            log.info("[HeroStronghold] found Heroes!");
            serverListener.setupHeroes((Heroes) currentPlugin);
        }
        
        currentPlugin = pm.getPlugin("WorldGuard");
        if (currentPlugin != null) {
            log.info("[HeroStronghold] found WorldGuard!");
            serverListener.setupWorldGuard((WorldGuardPlugin) currentPlugin);
        }
        
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
    
    public WorldGuardPlugin getWorldGuard() {
        if (serverListener == null)
            return null;
        return serverListener.getWorldGuard();
    }
}
