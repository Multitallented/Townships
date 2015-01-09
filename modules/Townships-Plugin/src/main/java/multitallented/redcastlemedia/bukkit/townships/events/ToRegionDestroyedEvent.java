package multitallented.redcastlemedia.bukkit.townships.events;

import multitallented.redcastlemedia.bukkit.townships.region.Region;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class ToRegionDestroyedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Region r;
    public ToRegionDestroyedEvent(Region r) {
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
