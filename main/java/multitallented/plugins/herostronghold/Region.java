package main.java.multitallented.plugins.herostronghold;

import java.util.ArrayList;
import org.bukkit.Location;

/**
 *
 * @author Multitallented
 */
public class Region {
    private int id;
    private Location loc;
    private String type;
    private ArrayList<String> owners;
    private ArrayList<String> members;
    
    public Region(int id, Location loc, String type, ArrayList<String> owners, ArrayList<String> members) {
        this.id = id;
        this.loc = loc;
        this.type = type;
        this.owners = owners;
        this.members = members;
    }
    
    public int getID() {
        return id;
    }
    
    public Location getLocation() {
        return loc;
    }
    
    public String getType() {
        return type;
    }
    
    public ArrayList<String> getOwners() {
        return owners;
    }
    
    public ArrayList<String> getMembers() {
        return members;
    }
}

