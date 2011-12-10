package main.java.multitallented.plugins.herostronghold;

import java.util.ArrayList;
import org.bukkit.Location;

/**
 *
 * @author Multitallented
 */
public class Region {
    private Location loc;
    private String type;
    private ArrayList<String> owners;
    private ArrayList<String> members;
    
    public Region(Location loc, String type, ArrayList<String> owners, ArrayList<String> members) {
        this.loc = loc;
        this.type = type;
        this.owners = owners;
        this.members = members;
    }
    
    public Location getLocation() {
        return loc;
    }
    
    public String getType() {
        return type;
    }
}
