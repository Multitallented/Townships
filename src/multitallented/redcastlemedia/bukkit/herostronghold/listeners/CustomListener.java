package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionDestroyedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
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
        //Check if a super region needs to fall if a required region was destroyed
        rm.checkIfDestroyedSuperRegion(((RegionDestroyedEvent) event).getLocation());
    }
}
