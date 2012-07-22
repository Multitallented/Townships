package multitallented.redcastlemedia.bukkit.herostronghold.events;

import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class SuperRegionDestroyedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SuperRegion sr;
    public SuperRegionDestroyedEvent(SuperRegion sr) {
        this.sr = sr;
    }
    
    public SuperRegion getSuperRegion() {
        return sr;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
