package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import com.herocraftonline.dev.heroes.Heroes;
import java.util.logging.Logger;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Multitallented
 */
public class PluginServerListener extends ServerListener {
    private HeroStronghold plugin;
    private Heroes heroes;

    public PluginServerListener(HeroStronghold p) {
        this.plugin = p;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();

        if (name.equals("Heroes")) {
            Logger log = Logger.getLogger("Minecraft");
            String message = "[HeroStronghold] " + name + " has been disabled!";
            log.info(message);
            HeroStronghold.heroes = null;
        }
        /*else if (name.equals("iConomy") || name.equals("BOSEconomy") || name.equals("Essentials")) {
            HeroStronghold.econ = null;
        }*/
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        
        if (name.equals("Heroes")) {
            HeroStronghold.heroes = (Heroes) currentPlugin;
            Logger log = Logger.getLogger("Minecraft");
            log.info("[HeroStronghold] Successfully hooked Heroes.");
        }
        /*else if (name.equals("Vault") && (pm.isPluginEnabled("iConomy") || pm.isPluginEnabled("BOSEconomy") || pm.isPluginEnabled("Essentials"))
                && HeroStronghold.econ == null) {
            this.plugin.setupEconomy();
        } else if ((name.equals("iConomy") || name.equals("BOSEconomy") || name.equals("Essentials")) && pm.isPluginEnabled("Vault")
                && HeroStronghold.econ == null) {
            this.plugin.setupEconomy();
        }*/
    }
    
}
