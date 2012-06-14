package multitallented.redcastlemedia.bukkit.herostronghold.events;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class UpkeepEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Location loc;
    private ArrayList<Location> destroyRegions = new ArrayList<Location>();
    private ArrayList<Region> createRegions = new ArrayList<Region>();
    public UpkeepEvent(Location loc) {
        this.loc = loc;
    }
    
    public Location getRegionLocation() {
        return loc;
    }
    
    public ArrayList<Location> getRegionsToDestroy() {
        return destroyRegions;
    }
    
    public void setRegionsToDestroy(ArrayList<Location> r) {
        this.destroyRegions = r;
    }
    
    public ArrayList<Region> getRegionsToCreate() {
        return createRegions;
    }
    
    public void setRegionsToCreate(ArrayList<Region> r) {
        this.createRegions = r;
    } 
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
