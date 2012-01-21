package multitallented.redcastlemedia.bukkit.herostronghold.events;

import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class SuperRegionCreatedEvent extends Event {
    private final String name;
    public SuperRegionCreatedEvent(String name) {
        super("SuperRegionCreated");
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
}
