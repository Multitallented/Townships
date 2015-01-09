package multitallented.redcastlemedia.bukkit.townships.events;

import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class ToSuperRegionDestroyedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SuperRegion sr;
    private final boolean isEvolving;
    private final boolean isDevolving;
    public ToSuperRegionDestroyedEvent(SuperRegion sr) {
        this.sr = sr;
        this.isEvolving = false;
        this.isDevolving = false;
    }
    
    public ToSuperRegionDestroyedEvent(SuperRegion sr, boolean isEvolving) {
        this.sr = sr;
        this.isEvolving = isEvolving;
        this.isDevolving = false;
    }

    public ToSuperRegionDestroyedEvent(SuperRegion sr, boolean isEvolving, boolean isDevolving) {
        this.sr = sr;
        this.isEvolving = isEvolving;
        this.isDevolving = isDevolving;
    }

    public boolean isDevolving() {
        return isDevolving;
    }

    public boolean isEvolving() {
        return isEvolving;
    }
    
    public SuperRegion getSuperRegion() {
        return sr;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
