package main.java.multitallented.plugins.herostronghold.region;

import java.util.ArrayList;
import org.bukkit.Location;

/**
 *
 * @author Multitallented
 */
public class Region implements Comparable {
    private int id;
    private Location loc;
    private String type;
    private ArrayList<String> owners;
    private ArrayList<String> members;
    private final RegionManager rm;
    
    public Region(RegionManager rm, int id, Location loc, String type, ArrayList<String> owners, ArrayList<String> members) {
        this.rm = rm;
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
    
    public void addOwner(String name) {
        owners.add(name);
    }
    
    public void addMember(String name) {
        members.add(name);
    }
    
    public boolean remove(String name) {
        if (owners.contains(name)) {
            owners.remove(name);
            return true;
        } else if (members.contains(name)) {
            members.remove(name);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isOwner(String name) {
        return owners.contains(name);
    }
    
    public boolean isMember(String name) {
        return members.contains(name);
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Region))
            return 0;
        Region r = (Region) o;
        return (int) (r.getLocation().getX() - rm.getRegionType(r.getType()).getRadius() - (loc.getX() - rm.getRegionType(type).getRadius()));
    }
}

