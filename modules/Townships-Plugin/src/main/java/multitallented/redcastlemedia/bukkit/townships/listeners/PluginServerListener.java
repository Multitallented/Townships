package multitallented.redcastlemedia.bukkit.townships.listeners;

import com.griefcraft.lwc.LWCPlugin;
import com.herocraftonline.heroes.Heroes;
import java.util.logging.Logger;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Multitallented
 */
public class PluginServerListener implements Listener {
    private Townships plugin;
    private Heroes heroes;

    public PluginServerListener(Townships p) {
        this.plugin = p;
        
        if (!Bukkit.getPluginManager().isPluginEnabled("LWC")) {
            return;
        }
        LWCPlugin lwc = (LWCPlugin) Bukkit.getPluginManager().getPlugin("LWC");
        LWCListener lwcListener = new LWCListener(plugin, lwc.getLWC());
        lwc.getLWC().getModuleLoader().registerModule(plugin, lwcListener);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        
        if (name.equals("Heroes")) {
            Logger log = Logger.getLogger("Minecraft");
            String message = "[Townships] " + name + " has been disabled!";
            log.info(message);
            Townships.heroes = null;
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        
        if (name.equals("Heroes")) {
            Townships.heroes = (Heroes) currentPlugin;
            Logger log = Logger.getLogger("Minecraft");
            log.info("[Townships] Successfully hooked Heroes.");
        } else if (name.equals("LWC")) {
            LWCPlugin lwc = (LWCPlugin) currentPlugin;
            LWCListener lwcListener = new LWCListener(plugin, lwc.getLWC());
            lwc.getLWC().getModuleLoader().registerModule(plugin, lwcListener);
            Logger log = Logger.getLogger("Minecraft");
            log.info("[Townships] Successfully hooked LWC.");
        }
    }
}
