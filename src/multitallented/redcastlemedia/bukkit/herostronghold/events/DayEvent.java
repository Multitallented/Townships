package multitallented.redcastlemedia.bukkit.herostronghold.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class DayEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
