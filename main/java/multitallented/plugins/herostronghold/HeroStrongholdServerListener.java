/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.multitallented.plugins.herostronghold;

import com.herocraftonline.dev.heroes.Heroes;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.logging.Logger;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Multitallented
 */
public class HeroStrongholdServerListener extends ServerListener {
    private HeroStronghold plugin;
    private Heroes heroes;
    private WorldGuardPlugin worldGuard;

    public HeroStrongholdServerListener(HeroStronghold p) {
        this.plugin = p;
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();

        if (name.equals("WorldGuard") || name.equals("Heroes")) {
            Logger log = Logger.getLogger("Minecraft");
            String message = "[HeroStronghold] is disabling itself because " + name + " has been disabled!";
            log.info(message);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        Plugin currentPlugin = event.getPlugin();
        String name = currentPlugin.getDescription().getName();
        
        if (name.equals("Heroes")) {
            heroes = (Heroes) currentPlugin;
        } else if (name.equals("WorldGuard")) {
            worldGuard = (WorldGuardPlugin) currentPlugin;
        }
    }
    
    public void setupHeroes(Heroes heroes) {
        if (this.heroes != null && heroes != null) {
            this.heroes = heroes;
        }
    }
    
    public void setupWorldGuard(WorldGuardPlugin worldGuard) {
        if (this.worldGuard != null && worldGuard != null) {
            this.worldGuard = worldGuard;
        }
    }
    
    public Heroes getHeroes() {
        return heroes;
    }
    
    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }
}
