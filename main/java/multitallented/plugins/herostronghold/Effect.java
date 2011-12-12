package main.java.multitallented.plugins.herostronghold;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public abstract class Effect {
    private final HeroStronghold plugin;
    private final String name;
    
    public Effect(HeroStronghold plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public HeroStronghold getPlugin() {
        return plugin;
    }
    
    protected void registerEvent(Type type, Listener listener, Priority priority) {
        plugin.getServer().getPluginManager().registerEvent(type, listener, priority, plugin);
    }
    
    public int regionHasEffect(ArrayList<String> effects, String name) {
        int data = 0;
        for (String effect : effects) {
            String[] params = effect.split("\\.");
            if (params.length > 1 && params[0].equalsIgnoreCase(name)) {
                data = Integer.parseInt(params[1]);
            }
        }
        if (data < 1)
            return 0;
        return data;
    }
    
    public boolean isOwnerOfRegion(Player p, Location l) {
        return getPlugin().getRegionManager().getRegion(l).getOwners().contains(p.getName());
    }
    
    public boolean isMemberOfRegion(Player p, Location l) {
        return getPlugin().getRegionManager().getRegion(l).getMembers().contains(p.getName());
    }
    
    public abstract void init();
}
