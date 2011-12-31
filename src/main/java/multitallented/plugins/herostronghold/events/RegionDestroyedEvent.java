package main.java.multitallented.plugins.herostronghold.events;

import org.bukkit.Location;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class RegionDestroyedEvent extends Event {
    private final Location l;
    public RegionDestroyedEvent(Location l) {
        super("RegionDestroyed");
        this.l = l;
    }
    
    public Location getLocation() {
        return l;
    }
    
}
