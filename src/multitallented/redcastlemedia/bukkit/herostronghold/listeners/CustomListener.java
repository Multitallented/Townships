package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionDestroyedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class CustomListener implements Listener {
    private final HeroStronghold hs;
    public CustomListener(HeroStronghold hs) {
        this.hs = hs;
    }
    
    @EventHandler
    public void onCustomEvent(RegionDestroyedEvent event) {
        //Check if a super region needs to fall if a required region was destroyed
        hs.getRegionManager().checkIfDestroyedSuperRegion(event.getRegion().getLocation());
    }
}