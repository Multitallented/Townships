package multitallented.redcastlemedia.bukkit.herostronghold.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Multitallented
 */
public class CommandEffectEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String[] args;

    public CommandEffectEvent(String[] args, Player player) {
        this.args=args;
        this.player = player;
        
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
}
