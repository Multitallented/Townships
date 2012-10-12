package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import com.herocraftonline.heroes.Heroes;
import com.massivecraft.factions.Factions;
import com.palmergames.bukkit.towny.Towny;
import java.util.logging.Logger;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
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
    private HeroStronghold plugin;
    private Heroes heroes;

    public PluginServerListener(HeroStronghold p) {
        this.plugin = p;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        
        if (name.equals("Heroes")) {
            Logger log = Logger.getLogger("Minecraft");
            String message = "[HeroStronghold] " + name + " has been disabled!";
            log.info(message);
            HeroStronghold.heroes = null;
        } else if (name.equals("Towny")) {
            HeroStronghold.towny = null;
        } else if (name.equals("Factions")) {
            HeroStronghold.factions = null;
        }
        /*else if (name.equals("iConomy") || name.equals("BOSEconomy") || name.equals("Essentials")) {
            HeroStronghold.econ = null;
        }*/
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        
        if (name.equals("Heroes")) {
            HeroStronghold.heroes = (Heroes) currentPlugin;
            Logger log = Logger.getLogger("Minecraft");
            log.info("[HeroStronghold] Successfully hooked Heroes.");
        } else if (name.equals("Towny")) {
            HeroStronghold.towny = (Towny) currentPlugin;
            Logger log = Logger.getLogger("Minecraft");
            log.info("[HeroStronghold] Successfully hooked Towny.");
        } else if (name.equals("Factions")) {
            HeroStronghold.factions = (Factions) currentPlugin;
            Logger log = Logger.getLogger("Minecraft");
            log.info("[HeroStronghold] Successfully hooked Factions.");
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
