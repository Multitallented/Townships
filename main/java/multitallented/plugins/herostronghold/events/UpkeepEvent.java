package main.java.multitallented.plugins.herostronghold.events;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class UpkeepEvent extends Event {
    private final Location loc;
    private ArrayList<Location> destroyRegions;
    public UpkeepEvent(Location loc) {
        super("UpkeepEvent");
        
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
}
