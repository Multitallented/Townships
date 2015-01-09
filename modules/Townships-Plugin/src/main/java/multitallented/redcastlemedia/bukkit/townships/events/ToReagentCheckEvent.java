package multitallented.redcastlemedia.bukkit.townships.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

/**
 *
 * @author Multitallented
 */
public class ToReagentCheckEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private final Location loc;
	private boolean cancelled;

	public ToReagentCheckEvent(Location loc) {
		this.loc = loc;

	}

	public Location getLocation() {
		return loc;
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
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
}
