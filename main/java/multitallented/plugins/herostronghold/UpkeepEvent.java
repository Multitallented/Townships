package main.java.multitallented.plugins.herostronghold;

import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class UpkeepEvent extends Event {
    private final int regionID;
    public UpkeepEvent(int regionID) {
        super(Type.CUSTOM_EVENT);
        
        this.regionID = regionID;
    }
    
    public int getRegionID() {
        return regionID;
    }
}
