package multitallented.redcastlemedia.bukkit.herostronghold.effect;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.Util;
import multitallented.redcastlemedia.bukkit.herostronghold.events.HSEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.events.UpkeepSuccessEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class Effect {
    private HeroStronghold plugin;
    
    public Effect(HeroStronghold plugin) {
        this.plugin = plugin;
    }
    
    public HeroStronghold getPlugin() {
        return plugin;
    }
    
    protected void registerEvent(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
    
    /**
     * Returns the first region that contains that location.
     * 
     * @deprecated use regionManager.getContainingRegions(Location)
     * @param currentLocation the location to check
     * @return 
     */
    public Region getContainingRegion(Location currentLocation) {
        double x1 = currentLocation.getX();
        Location loc = null;
        RegionManager rm = plugin.getRegionManager();
        for (Region r : rm.getSortedRegions()) {
            int radius = rm.getRegionType(r.getType()).getRadius();
            Location l = r.getLocation();
            if (l.getX() + radius < x1) {
                return null;
            }
            try {
                if (!(l.getX() - radius > x1) && l.distanceSquared(currentLocation) < radius) {
                    loc = l;
                    break;
                }
            } catch (IllegalArgumentException iae) {

            }
        }
        if (loc == null) {
            return null;
        }
        return rm.getRegion(loc);
    }
    
    /**
     * Use this to find if a region (not super-region) has an effect and what 
     * number is associated with that effect. The number has no internal meaning
     * to HeroStronghold, only you decide what that number means. Example:
     * arrowturrets use the number to determine arrow velocity. Conveyors use the
     * number as the item id that needs to be moved.
     * 
     * @param effects regionManager.getRegionType(region.getType()).getEffects()
     * @param name example: "denyblockbreak"
     * @return the number following the effect (returns 0 if doesn't have that effect)
     */
    public int regionHasEffect(ArrayList<String> effects, String name) {
        int data = 0;
        if (effects == null || effects.isEmpty())
            return 0;
        
        for (String effect : effects) {
            String[] params = effect.split("\\.");
            if (params.length > 1 && params[0].equalsIgnoreCase(name)) {
                data = Integer.parseInt(params[1]);
            }
        }
        if (data < 1)
            return 0;
        return data;
    }
    
    /**
     * Use this to find if a region (not super-region) has an effect and what 
     * number is associated with that effect. The number has no internal meaning
     * to HeroStronghold, only you decide what that number means. Example:
     * arrowturrets use the number to determine arrow velocity. Conveyors use the
     * number as the item id that needs to be moved.
     * 
     * @param region the region to check
     * @param name example: "denyblockbreak"
     * @return the number following the effect (returns 0 if doesn't have that effect)
     */
    public int regionHasEffect(Region r, String name) {
        int data = 0;
        ArrayList<String> effects = plugin.getRegionManager().getRegionType(r.getType()).getEffects();
        if (effects == null || effects.isEmpty())
            return 0;
        
        for (String effect : effects) {
            String[] params = effect.split("\\.");
            if (params.length > 1 && params[0].equalsIgnoreCase(name)) {
                data = Integer.parseInt(params[1]);
            }
        }
        if (data < 1)
            return 0;
        return data;
    }
    
    /**
     * Checks if the player is an owner of the region. Equivalent to
     * region.isOwner(player.getName());
     * 
     * @param player
     * @param region
     * @return 
     */
    public boolean isOwnerOfRegion(Player p, Region r) {
        return r.isOwner(p.getName());
    }
    
    /**
     * Checks if the player is is a member of the region at the location
     * specified. This will throw a NullPointerException if there is no region
     * at that location.
     * 
     * @param player
     * @param location the location of the region center region.getLocation()
     * @return 
     */
    public boolean isOwnerOfRegion(Player p, Location l) throws NullPointerException {
        return getPlugin().getRegionManager().getRegion(l).getOwners().contains(p.getName());
    }
    
    /**
     * This function does more than just region.isMember(player.getName()). It
     * checks if the region has added a super-region that the player is a member
     * of. Also checks if the region has added "all" players.
     * 
     * @exception thows NPE if no region found at the given location
     * @param player
     * @param location the location of the region center region.getLocation()
     * @return 
     */
    public boolean isMemberOfRegion(Player p, Location l) throws NullPointerException {
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        if (r.isMember(p.getName())) {
            return true;
        } else if (r.isMember("all")) {
            return true;
        } else {
            for (String s : r.getMembers()) {
                if (s.contains("sr:")) {
                    String superRegionName = s.replace("sr:", "");
                    SuperRegion sr = rm.getSuperRegion(superRegionName);
                    if (sr != null && (sr.hasMember(p.getName()) || sr.hasOwner(p.getName()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the region has output and the chest is not full first. Then it
     * checks if the chest has the necessary reagents.
     * 
     * @param location the location of the center of the region region.getLocation()
     * @return 
     */
    public boolean hasReagents(Location l) {
        try {
            if (l.getBlock().getState() == null) {
                return false;
            }
        } catch (NullPointerException npe) {
            return false;
        }
        RegionManager rm = getPlugin().getRegionManager();
        RegionType rt = rm.getRegionType(rm.getRegion(l).getType());
        /*Map<Material, Integer> reagentMap = new EnumMap<Material, Integer>(Material.class);
        for (ItemStack is : rt.getReagents()) {
            reagentMap.put(is.getType(), is.getAmount());
        }*/
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest)) {
            return false;
        }
        //Check if chest is full and region has output
        Chest chest = ((Chest) bs);
        return Util.containsItems(rt.getReagents(), chest.getInventory());
        
        /*for (ItemStack is : rt.getReagents()) {
            if (!chest.getInventory().contains(is.getType(), is.getAmount())) {
                return false;
            }
        }
        return true;*/
        /*for (ItemStack is : chest.getInventory().getContents()) {
            Material mat = Material.AIR;
            if (is != null) {
                mat = is.getType();
                if (!mat.equals(Material.AIR) && reagentMap.containsKey(mat)) {
                    if (reagentMap.get(mat) <= is.getAmount()) {
                        reagentMap.remove(mat);
                    } else {
                        reagentMap.put(mat, reagentMap.get(mat) - is.getAmount());
                    }
                }
            }
        }
        if (reagentMap.isEmpty()) {
            return true;
        }
        
        return false;*/
    }
    
    /**
     * Forces the region to consume upkeep items and output money and items
     * regardless of the upkeep-chance.
     * 
     * @see forceUpkeep(HSEvent) Use this instead if possible
     * @param location the location of the center of the region region.getLocation()
     */
    public void forceUpkeep(Location l) {
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        RegionType rt = rm.getRegionType(r.getType());
        
        
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest)) {
            return;
        }
        Chest chest = (Chest) bs;
        
        
        //Remove the upkeep items from the region chest and add items from output
        if (chest.getInventory().firstEmpty() < 0 && !rt.getOutput().isEmpty()) {
            return;
        }
        if (!Util.containsItems(rt.getUpkeep(), chest.getInventory())) {
            return;
        }
        /*boolean hasUpkeep = true;
        for (ItemStack is : rt.getUpkeep()) {
            if (!chest.getInventory().contains(is.getType(), is.getAmount())) {
                hasUpkeep = false;
            }
        }
        if (!hasUpkeep) {
            return;
        }*/
        
        //Check and remove money from the player
        String playername = "";
        try {
            playername = r.getOwners().get(0);
        } catch (IndexOutOfBoundsException ioobe) {
            return;
        }
        double output = rt.getMoneyOutput();
        if (rt.getMoneyOutput() != 0 && HeroStronghold.econ != null) {
            if (output < 0  && HeroStronghold.econ.getBalance(playername) < Math.abs(output)) {
                return;
            }
        }
        
        Util.removeItems(rt.getUpkeep(), chest.getInventory());
        /*for (ItemStack is : rt.getUpkeep()) {
            chest.getInventory().removeItem(is);
        }*/
        
        if (HeroStronghold.econ != null) {
            if (output < 0) {
                HeroStronghold.econ.withdrawPlayer(playername, Math.abs(output));
            } else {
                HeroStronghold.econ.depositPlayer(playername, output);
            }
        }
        Util.addItems(rt.getOutput(), chest.getInventory());
        /*for (ItemStack is : rt.getOutput()) {
            chest.getInventory().addItem(is);
        }*/
        chest.update(true);
    }
    /**
     * Forces the region to consume upkeep items and output money and items
     * regardless of the upkeep-chance.
     * 
     * @param location the location of the center of the region region.getLocation()
     */
    public void forceUpkeep(HSEvent event) {
        Location l = event.getLocation();
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        RegionType rt = rm.getRegionType(r.getType());
        
        
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest)) {
            return;
        }
        Chest chest = (Chest) bs;
        
        
        //Remove the upkeep items from the region chest and add items from output
        
        if (chest.getInventory().firstEmpty() < 0 && !rt.getOutput().isEmpty()) {
            return;
        }
        if (!Util.containsItems(rt.getUpkeep(), chest.getInventory())) {
            return;
        }
        /*boolean hasUpkeep = true;
        for (ItemStack is : rt.getUpkeep()) {
            if (!chest.getInventory().contains(is.getType(), is.getAmount())) {
                hasUpkeep = false;
            }
        }
        if (!hasUpkeep) {
            return;
        }*/
        
        //Check and remove money from the player
        String playername = "";
        try {
            playername = r.getOwners().get(0);
        } catch (IndexOutOfBoundsException ioobe) {
            return;
        }
        double output = rt.getMoneyOutput();
        if (rt.getMoneyOutput() != 0 && HeroStronghold.econ != null) {
            if (output < 0  && HeroStronghold.econ.getBalance(playername) < Math.abs(output)) {
                return;
            }
        }
        
        
        /*for (ItemStack is : rt.getUpkeep()) {
            chest.getInventory().removeItem(is);
        }*/
        Util.removeItems(rt.getUpkeep(), chest.getInventory());
        
        if (HeroStronghold.econ != null) {
            if (output < 0) {
                HeroStronghold.econ.withdrawPlayer(playername, Math.abs(output));
            } else {
                HeroStronghold.econ.depositPlayer(playername, output);
            }
        }
        Util.addItems(rt.getOutput(), chest.getInventory());
        /*for (ItemStack is : rt.getOutput()) {
            chest.getInventory().addItem(is);
        }*/
        Bukkit.getPluginManager().callEvent(new UpkeepSuccessEvent(event));
        chest.update();
    }
    
    /**
     * Forces the region to consume upkeep items and output money and items
     * if a random number is lower than the upkeep-chance.
     * 
     * @see upkeep(HSEvent) Use this instead if possible
     * @param location the location of the center of the region region.getLocation()
     */
    public boolean upkeep(Location l) {
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        RegionType rt = rm.getRegionType(r.getType());
        if (Math.random() > rt.getUpkeepChance()) {
            return false;
        }
        
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest))
            return false;
        Chest chest = (Chest) bs;
        
        //Remove the upkeep items from the region chest and add items from output
        if (chest.getInventory().firstEmpty() < 0 && !rt.getOutput().isEmpty()) {
            return false;
        }
        if (!Util.containsItems(rt.getUpkeep(), chest.getInventory())) {
            return false;
        }
        /*boolean hasUpkeep = true;
        for (ItemStack is : rt.getUpkeep()) {
            if (!chest.getInventory().contains(is.getType(), is.getAmount())) {
                hasUpkeep = false;
            }
        }
        if (!hasUpkeep) {
            return false;
        }*/
        
        
        //Check and remove money from the player
        String playername = "";
        try {
            playername = r.getOwners().get(0);
        } catch (IndexOutOfBoundsException ioobe) {
            return false;
        }
        double output = rt.getMoneyOutput();
        if (output != 0 && HeroStronghold.econ != null) {
            Economy econ = HeroStronghold.econ;
            if (r.getOwners().isEmpty()) {
                return false;
            }
            if (output < 0  && econ.getBalance(playername) < Math.abs(output)) {
                return false;
            }
        }
        
        Util.removeItems(rt.getUpkeep(), chest.getInventory());
        /*for (ItemStack is : rt.getUpkeep()) {
            chest.getInventory().removeItem(is);
        }*/
        
        if (HeroStronghold.econ != null) {
            if (output < 0) {
                HeroStronghold.econ.withdrawPlayer(playername, Math.abs(output));
            } else {
                HeroStronghold.econ.depositPlayer(playername, output);
            }
        }
        /*for (ItemStack is : rt.getOutput()) {
            chest.getInventory().addItem(is);
        }*/
        Util.addItems(rt.getOutput(), chest.getInventory());
        
        chest.update(true);
        return true;
    }
    
    /**
     * Forces the region to consume upkeep items and output money and items
     * if a random number is lower than the upkeep-chance.
     * 
     * @param location the location of the center of the region region.getLocation()
     */
    public boolean upkeep(HSEvent event) {
        Location l = event.getLocation();
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        RegionType rt = rm.getRegionType(r.getType());
        if (Math.random() > rt.getUpkeepChance()) {
            return false;
        }
        
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest))
            return false;
        Chest chest = (Chest) bs;
        
        //Remove the upkeep items from the region chest and add items from output
        if (chest.getInventory().firstEmpty() < 0 && !rt.getOutput().isEmpty()) {
            return false;
        }
        if (!Util.containsItems(rt.getUpkeep(), chest.getInventory())) {
            return false;
        }
        /*boolean hasUpkeep = true;
        for (ItemStack is : rt.getUpkeep()) {
            if (!chest.getInventory().contains(is.getType(), is.getAmount())) {
                hasUpkeep = false;
            }
        }
        if (!hasUpkeep) {
            return false;
        }*/
        
        
        //Check and remove money from the player
        String playername = "";
        try {
            playername = r.getOwners().get(0);
        } catch (IndexOutOfBoundsException ioobe) {
            return false;
        }
        double output = rt.getMoneyOutput();
        if (output != 0 && HeroStronghold.econ != null) {
            Economy econ = HeroStronghold.econ;
            if (r.getOwners().isEmpty()) {
                return false;
            }
            if (output < 0  && econ.getBalance(playername) < Math.abs(output)) {
                return false;
            }
        }
        
        Util.removeItems(rt.getUpkeep(), chest.getInventory());
        /*for (ItemStack is : rt.getUpkeep()) {
            chest.getInventory().removeItem(is);
        }*/
        
        if (HeroStronghold.econ != null) {
            if (output < 0) {
                HeroStronghold.econ.withdrawPlayer(playername, Math.abs(output));
            } else {
                HeroStronghold.econ.depositPlayer(playername, output);
            }
        }
        Util.addItems(rt.getOutput(), chest.getInventory());
        /*for (ItemStack is : rt.getOutput()) {
            chest.getInventory().addItem(is);
        }*/
        Bukkit.getPluginManager().callEvent(new UpkeepSuccessEvent(event));
        chest.update(true);
        return true;
    }
    
    public void init(HeroStronghold plugin) {
        this.plugin = plugin;
    }
}
