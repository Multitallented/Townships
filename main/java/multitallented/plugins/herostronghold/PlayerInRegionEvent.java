package main.java.multitallented.plugins.herostronghold;

import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class PlayerInRegionEvent extends Event {
    private final int regionID;

    public PlayerInRegionEvent(int regionID) {
        super(Type.CUSTOM_EVENT);
        this.regionID = regionID;
        
    }
    
    public int getRegionID() {
        return regionID;
    }
    
}
