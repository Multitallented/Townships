package main.java.multitallented.plugins.herostronghold;

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
    private final ArrayList<String> effects;

    public PlayerInRegionEvent(Location loc, Player player, ArrayList<String> effects) {
        super("PlayerInRegionEvent");
        this.loc = loc;
        this.player = player;
        this.effects = effects;
        
    }
    
    public Location getRegionLocation() {
        return loc;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public ArrayList<String> getEffects() {
        return effects;
    }
    
    public ArrayList<Location> getRegionsToDestroy() {
        return destroyRegions;
    }
    
    public void setRegionsToDestroy(ArrayList<Location> r) {
        this.destroyRegions = r;
    }
    
}
