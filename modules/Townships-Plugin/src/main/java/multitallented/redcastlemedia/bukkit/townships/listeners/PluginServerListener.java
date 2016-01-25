package multitallented.redcastlemedia.bukkit.townships.listeners;

import multitallented.redcastlemedia.bukkit.townships.Townships;
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

    public PluginServerListener(Townships p) {
        this.plugin = p;

    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();

        if (name.equals("HeroScoreboard")) {
            Townships.hsb = null;
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        if (name.equals("HeroScoreboard")) {
            Townships.hsb = (HeroScoreboard) currentPlugin;
        }
    }
}
