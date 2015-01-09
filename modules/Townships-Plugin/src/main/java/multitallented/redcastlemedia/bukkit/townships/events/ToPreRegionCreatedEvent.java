package multitallented.redcastlemedia.bukkit.townships.events;

import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class ToPreRegionCreatedEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Location l;
    private final RegionType rt;
    private boolean cancellable;
    private final Player player;
    public ToPreRegionCreatedEvent(Location l, RegionType rt, Player player) {
        this.l = l;
        this.rt = rt;
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public RegionType getRegionType() {
        return rt;
    }
    
    public Location getLocation() {
        return l;
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
        return cancellable;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.cancellable=bln;
    }
    
}
