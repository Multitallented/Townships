package main.java.multitallented.plugins.herostronghold;

import java.util.ArrayList;
import org.bukkit.Location;

/**
 *
 * @author Multitallented
 */
public class Region {
    private String name;
    private Location loc;
    private String type;
    private ArrayList<String> owners;
    private ArrayList<String> members;
    
    public Region(String name, Location loc, String type, ArrayList<String> owners, ArrayList<String> members) {
        this.name = name;
        this.loc = loc;
        this.type = type;
        this.owners = owners;
        this.members = members;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return loc;
    }
    
    public String getType() {
        return type;
    }
    public boolean ownersContains(String name) {
        return owners.contains(name);
    }
     
    public boolean membersContains(String name) {
        return members.contains(name);
    }
}
