package multitallented.redcastlemedia.bukkit.herostronghold.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class RegionCreatedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Location l;
    public RegionCreatedEvent(Location l) {
        this.l = l;
    }
    
    public Location getLocation() {
        return l;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
