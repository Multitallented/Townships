package multitallented.redcastlemedia.bukkit.townships.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;

/**
 *
 * @author Multitallented
 */
public class ToRenameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final SuperRegion sr;
    private final String oldName;
    private final String newName;
    private boolean cancelled;

    public ToRenameEvent(SuperRegion sr, String oldName, String newName) {
        this.sr = sr;
        this.oldName = oldName;
        this.newName = newName;
    }


    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public SuperRegion getSuperRegion() {
        return sr;
    }
}
