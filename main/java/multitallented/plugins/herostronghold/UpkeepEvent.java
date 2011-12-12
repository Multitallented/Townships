package main.java.multitallented.plugins.herostronghold;

import org.bukkit.Location;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class UpkeepEvent extends Event {
    private final Location loc;
    public UpkeepEvent(Location loc) {
        super("UpkeepEvent");
        
        this.loc = loc;
    }
    
    public Location getRegionLocation() {
        return loc;
    }
}
