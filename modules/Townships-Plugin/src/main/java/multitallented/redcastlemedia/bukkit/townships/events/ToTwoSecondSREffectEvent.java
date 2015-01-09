package multitallented.redcastlemedia.bukkit.townships.events;

import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class ToTwoSecondSREffectEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    private final SuperRegion sr;
    private final String[] effect;

    public ToTwoSecondSREffectEvent(SuperRegion sr, String[] effect) {
        this.sr = sr;
        this.effect = effect;
    }
    
    public String[] getEffect() {
        return effect;
    }
    
    public Location getLocation() {
        return sr.getLocation();
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
