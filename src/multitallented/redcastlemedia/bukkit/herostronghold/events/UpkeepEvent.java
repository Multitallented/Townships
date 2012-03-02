package multitallented.redcastlemedia.bukkit.herostronghold.events;

import java.util.ArrayList;
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
    private ArrayList<Location> destroyRegions;
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
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
