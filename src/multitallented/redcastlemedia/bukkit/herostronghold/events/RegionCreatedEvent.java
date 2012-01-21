package multitallented.redcastlemedia.bukkit.herostronghold.events;

import org.bukkit.Location;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class RegionCreatedEvent extends Event {
    private final Location l;
    public RegionCreatedEvent(Location l) {
        super("RegionCreated");
        this.l = l;
    }
    
    public Location getLocation() {
        return l;
    }
    
}
