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
    private UpkeepEvent upkeep;
    public UpkeepSuccessEvent(UpkeepEvent upkeep) {
        this.loc = upkeep.getRegionLocation();
        this.upkeep = upkeep;
    }
    
    public Location getRegionLocation() {
        return loc;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public UpkeepEvent getEvent() {
        return this.upkeep;
    }
    
    public void setEvent(UpkeepEvent event) {
        this.upkeep = event;
    }
    
    public void setRegionsToCreate(ArrayList<Region> r) {
        upkeep.setRegionsToCreate(r);
    }
    
    public ArrayList<Region> getRegionsToCreate() {
        return upkeep.getRegionsToCreate();
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
