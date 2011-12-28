package main.java.multitallented.plugins.herostronghold.listeners;

import main.java.multitallented.plugins.herostronghold.events.RegionDestroyedEvent;
import main.java.multitallented.plugins.herostronghold.region.RegionManager;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class CustomListener extends CustomEventListener {
    private final RegionManager rm;
    public CustomListener(RegionManager rm) {
        this.rm = rm;
    }
    
    @Override
    public void onCustomEvent(Event event) {
        if (!(event instanceof RegionDestroyedEvent))
            return;
        
        rm.checkIfDestroyedSuperRegion(((RegionDestroyedEvent) event).getLocation());
    }
}
