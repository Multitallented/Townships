package multitallented.redcastlemedia.bukkit.herostronghold.events;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public class PlayerInRegionEvent extends Event {
    private final Location loc;
    private final Player player;
    private ArrayList<Location> destroyRegions;

    public PlayerInRegionEvent(Location loc, Player player) {
        super("PlayerInRegionEvent");
        this.loc = loc;
        this.player = player;
        
    }
    
    public Location getRegionLocation() {
        return loc;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public ArrayList<Location> getRegionsToDestroy() {
        return destroyRegions;
    }
    
    public void setRegionsToDestroy(ArrayList<Location> r) {
        this.destroyRegions = r;
    }
    
}
