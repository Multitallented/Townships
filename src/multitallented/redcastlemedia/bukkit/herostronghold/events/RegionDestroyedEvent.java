package multitallented.redcastlemedia.bukkit.herostronghold.events;

import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class RegionDestroyedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Region r;
    public RegionDestroyedEvent(Region r) {
        this.r = r;
    }
    
    public Region getRegion() {
        return r;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
