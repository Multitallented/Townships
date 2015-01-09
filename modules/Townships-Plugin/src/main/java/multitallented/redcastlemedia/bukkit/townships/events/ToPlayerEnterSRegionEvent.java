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
public class ToPlayerEnterSRegionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String name;
    private final Player player;

    public ToPlayerEnterSRegionEvent(String name, Player player) {
        this.name = name;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
