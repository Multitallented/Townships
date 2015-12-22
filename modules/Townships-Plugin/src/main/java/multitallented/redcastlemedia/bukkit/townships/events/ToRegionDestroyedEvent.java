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
    private final boolean checkDestroy;
    public ToRegionDestroyedEvent(Region r) {
        this.r = r;
        this.checkDestroy = true;
    }

    public ToRegionDestroyedEvent(Region r, boolean checkDestroy) {
        this.r = r;
        this.checkDestroy = checkDestroy;
    }

    public Region getRegion() {
        return r;
    }
    public boolean getCheckDestroy() {
        return checkDestroy;
    }


    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
