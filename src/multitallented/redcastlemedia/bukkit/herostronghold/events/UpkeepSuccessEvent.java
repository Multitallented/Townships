package multitallented.redcastlemedia.bukkit.herostronghold.events;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author multitallented
 */
public class UpkeepSuccessEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Location loc;
    private HSEvent event;
    public UpkeepSuccessEvent(HSEvent event) {
        this.loc = event.getLocation();
        this.event = event;
    }
    
    public Location getRegionLocation() {
        return loc;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public HSEvent getEvent() {
        return this.event;
    }
    
    public void setEvent(HSEvent event) {
        this.event = event;
    }
    
    public void setRegionsToCreate(ArrayList<Region> r) {
        event.setRegionsToCreate(r);
    }
    
    public ArrayList<Region> getRegionsToCreate() {
        return event.getRegionsToCreate();
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
