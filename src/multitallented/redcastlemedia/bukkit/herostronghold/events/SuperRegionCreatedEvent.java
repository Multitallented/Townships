package multitallented.redcastlemedia.bukkit.herostronghold.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class SuperRegionCreatedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String name;
    public SuperRegionCreatedEvent(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
