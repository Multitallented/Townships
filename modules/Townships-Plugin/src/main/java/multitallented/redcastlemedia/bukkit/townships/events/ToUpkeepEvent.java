package multitallented.redcastlemedia.bukkit.townships.events;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class ToUpkeepEvent extends Event implements ToEvent, Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Location loc;
    private ArrayList<Location> destroyRegions = new ArrayList<Location>();
    private ArrayList<Region> createRegions = new ArrayList<Region>();
    private boolean cancelled;
    public ToUpkeepEvent(Location loc) {
        this.loc = loc;
    }
    
    @Override
    public Location getLocation() {
        return loc;
    }
    
    @Override
    public ArrayList<Location> getRegionsToDestroy() {
        return destroyRegions;
    }
    
    @Override
    public void setRegionsToDestroy(ArrayList<Location> r) {
        this.destroyRegions = r;
    }
    
    @Override
    public ArrayList<Region> getRegionsToCreate() {
        return createRegions;
    }
    
    @Override
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        cancelled = bln;
    }
}
