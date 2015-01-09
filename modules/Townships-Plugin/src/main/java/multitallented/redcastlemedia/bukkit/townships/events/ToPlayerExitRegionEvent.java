package multitallented.redcastlemedia.bukkit.townships.events;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class ToPlayerExitRegionEvent extends Event implements ToEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Location loc;
    private final Player player;
    private ArrayList<Location> destroyRegions = new ArrayList<Location>();
    private ArrayList<Region> createRegions = new ArrayList<Region>();
    private boolean loggingOut = false;

    public ToPlayerExitRegionEvent(Location loc, Player player) {
        this.loc = loc;
        this.player = player;
    }
    public ToPlayerExitRegionEvent(Location loc, Player player, boolean loggingOut) {
        this.loc = loc;
        this.player = player;
        this.loggingOut = loggingOut;
    }

    public boolean getLoggingOut() {
        return loggingOut;
    }

    @Override
    public void setRegionsToCreate(ArrayList<Region> newRegions) {
        this.createRegions = newRegions;
    }

    @Override
    public ArrayList<Region> getRegionsToCreate() {
        return createRegions;
    }

    @Override
    public Location getLocation() {
        return loc;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public ArrayList<Location> getRegionsToDestroy() {
        return destroyRegions;
    }

    @Override
    public void setRegionsToDestroy(ArrayList<Location> r) {
        this.destroyRegions = r;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
