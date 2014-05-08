package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.events.RegionDestroyedEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionCondition;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

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
    
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        if (event.isCancelled() || !(event.getRemover() instanceof Player)) {
            return;
        }
        Player remover = (Player) event.getRemover();
        ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
        conditions.add(new RegionCondition("denyblockbreak", true, 0));
        conditions.add(new RegionCondition("denyblockbreaknoreagent", false, 0));
        if (hs.getRegionManager().shouldTakeAction(event.getEntity().getLocation(), remover, conditions)) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
    }
}