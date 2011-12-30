package main.java.multitallented.plugins.herostronghold.region;

import java.util.List;
import java.util.Map;
import org.bukkit.Location;

/**
 *
 * @author Multitallented
 */
public class SuperRegion implements Comparable {
    private String name;
    private final Location l;
    private final String type;
    private final Map<String, List<String>> members;
    private final List<String> owners;
    private int power;
    private double taxes = 0; //TODO implement daily member taxes
    private final RegionManager rm;
    
    public SuperRegion(RegionManager rm, String name, Location l, String type, List<String> owner, Map<String, List<String>> members, int power, double taxes) {
        this.name = name;
        this.rm = rm;
        this.l = l;
        this.type=type;
        this.owners = owner;
        this.members = members;
        this.power = power;
        this.taxes = taxes;
    }
    
    public SuperRegion(RegionManager rm, String name, Location l, String type, List<String> owner, Map<String, List<String>> members, int power) {
        this.rm = rm;
        this.name = name;
        this.l = l;
        this.type=type;
        this.owners = owner;
        this.members = members;
        this.power = power;
    }
    
    public double getTaxes() {
        return taxes;
    }
    
    public void setTaxes(double taxes) {
        this.taxes = taxes;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Location getLocation() {
        return l;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean hasMember(String name) {
        return members.containsKey(name);
    }
    
    public boolean addMember(String name, List<String> perms) {
        return members.put(name, perms) != null;
    }
    
    public List<String> getMember(String name) {
        return members.get(name);
    }
    
    public Map<String, List<String>> getMembers() {
        return members;
    }
    
    public boolean togglePerm(String name, String perm) {
        boolean removed = false;
        try {
            if (!members.get(name).remove(perm))
                members.get(name).add(perm);
            else
                removed = true;
        } catch (NullPointerException npe) {
            
        }
        return removed;
    }
    
    public boolean hasOwner(String name) {
        return owners.contains(name);
    }
    
    public boolean addOwner(String name) {
        return owners.add(name);
    }
    
    public List<String> getOwners() {
        return owners;
    }
    
    public boolean remove(String name) {
        if (!owners.remove(name))
            return members.remove(name) != null;
        else
            return true;
    }
    
    public int getPower() {
        return power;
    }
    
    public void setPower(int i) {
        power = i;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof SuperRegion))
            return 0;
        SuperRegion r = (SuperRegion) o;
        return (int) (r.getLocation().getX() - rm.getSuperRegionType(r.getType()).getRadius() - (l.getX() - rm.getSuperRegionType(type).getRadius()));
    }
}
