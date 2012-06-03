package multitallented.redcastlemedia.bukkit.herostronghold.effect;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

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
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
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
    public boolean isOwnerOfRegion(Player p, Location l) {
        return getPlugin().getRegionManager().getRegion(l).getOwners().contains(p.getName());
    }
    
    public boolean isMemberOfRegion(Player p, Location l) {
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
        Map<Material, Integer> reagentMap = new EnumMap<Material, Integer>(Material.class);
        for (ItemStack is : rt.getReagents()) {
            reagentMap.put(is.getType(), is.getAmount());
        }
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest)) {
            return false;
        }
        for (ItemStack is : ((Chest) bs).getInventory().getContents()) {
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
        if (reagentMap.isEmpty())
            return true;
        
        return false;
    }
    
    public void forceUpkeep(Location l) {
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        RegionType rt = rm.getRegionType(r.getType());
        
        //Check and remove money from the player
        if (rt.getMoneyOutput() != 0 && HeroStronghold.econ != null) {
            Economy econ = HeroStronghold.econ;
            double output = rt.getMoneyOutput();
            if (r.getOwners().isEmpty())
                return;
            String playername = r.getOwners().get(0);
            if (output < 0  && econ.getBalance(playername) < Math.abs(output)) {
                return;
            } else if (output < 0) {
                econ.withdrawPlayer(playername, Math.abs(output));
            } else {
                econ.depositPlayer(playername, output);
            }
        }
        if (rt.getReagents().isEmpty() && rt.getOutput().isEmpty())
            return;
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest))
            return;
        Chest chest = (Chest) bs;
        
        if (chest.getInventory().firstEmpty() < 0) {
            return;
        }
        
        //Remove the upkeep items from the region chest and add items from output
        /*for (ItemStack is : rt.getUpkeep()) {
            if (chest.getInventory().contains(is)) {
                chest.getInventory().removeItem(is);
            }
        }
        for (ItemStack is : rt.getOutput()) {
            chest.getInventory().addItem(is);
        }*/
        Map<Material, Integer> upkeepMap = new EnumMap<Material, Integer>(Material.class);
        Map<Material, Integer> outputMap = new EnumMap<Material, Integer>(Material.class);
        for (ItemStack is : rt.getUpkeep()) {
            if (is != null)
                upkeepMap.put(is.getType(), is.getAmount());
        }
        for (ItemStack is: rt.getOutput()) {
            if (is != null)
                outputMap.put(is.getType(), is.getAmount());
        }
        ItemStack[] is = chest.getInventory().getContents();
        ItemStack[] realIS = is.clone();
        for (int i = 0 ; i<realIS.length; i++) {
            int maxSize = realIS[i].getMaxStackSize();
            Material mat = Material.AIR;
            if (realIS[i] != null)
                mat = realIS[i].getType();
            
            //chest has an item and item is in upkeep
            if (!mat.equals(Material.AIR) && upkeepMap.containsKey(mat)) {
                //chest amount is <= upkeep amount
                if (realIS[i].getAmount() <= upkeepMap.get(mat)) {
                    upkeepMap.put(mat, upkeepMap.get(mat) - realIS[i].getAmount());
                    is[i] = null;
                //chest amount is > upkeep amount
                } else {
                    int amount = realIS[i].getAmount() - upkeepMap.get(mat);
                    upkeepMap.remove(mat);
                    is[i].setAmount(amount);
                }
            //chest has an item and its an item in output
            } else if (!mat.equals(Material.AIR) && outputMap.containsKey(mat) && realIS[i].getAmount() < maxSize) {
                //chest amount + output amount is <= maxSize
                if (realIS[i].getAmount() + outputMap.get(mat) <= maxSize) {
                    is[i].setAmount(is[i].getAmount() + outputMap.get(mat));
                    outputMap.remove(mat);
                //chest amount + output amount is > maxSize
                } else {
                    int excess = is[i].getAmount() + outputMap.get(mat) - maxSize;
                    is[i].setAmount(maxSize);
                    outputMap.put(mat, excess);
                }
            //chest slot is empty and output isn't empty
            } else if (mat.equals(Material.AIR) && !outputMap.isEmpty()) {
                for (Material currentMat : outputMap.keySet()) {
                    int maxSize2 = new ItemStack(currentMat).getMaxStackSize();
                    if (outputMap.get(currentMat) <= maxSize2) {
                        is[i] = new ItemStack(currentMat, outputMap.get(currentMat));
                        outputMap.remove(currentMat);
                    } else {
                        is[i] = new ItemStack(currentMat, maxSize2);
                        outputMap.put(currentMat, outputMap.get(currentMat) - maxSize2);
                    }
                    break;
                }
            }
        }
        chest.getInventory().setContents(is);
        chest.update(true);
    }
    
    public boolean upkeep(Location l) {
        RegionManager rm = getPlugin().getRegionManager();
        Region r = rm.getRegion(l);
        RegionType rt = rm.getRegionType(r.getType());
        if (Math.random() > rt.getUpkeepChance())
            return false;
        
        //Check and remove money from the player
        if (rt.getMoneyOutput() != 0 && HeroStronghold.econ != null) {
            Economy econ = HeroStronghold.econ;
            double output = rt.getMoneyOutput();
            if (r.getOwners().isEmpty())
                return false;
            String playername = r.getOwners().get(0);
            if (output < 0  && econ.getBalance(playername) < Math.abs(output)) {
                return false;
            } else if (output < 0) {
                econ.withdrawPlayer(playername, Math.abs(output));
            } else {
                econ.depositPlayer(playername, output);
            }
        }
        
        if (rt.getReagents().isEmpty() && rt.getOutput().isEmpty())
            return true;
        
        BlockState bs = l.getBlock().getState();
        if (!(bs instanceof Chest))
            return false;
        Chest chest = (Chest) bs;
        
        if (chest.getInventory().firstEmpty() < 0) {
            return false;
        }
        
        //Remove the upkeep items from the region chest and add items from output
        /*for (ItemStack is : rt.getUpkeep()) {
            if (chest.getInventory().contains(is)) {
                chest.getInventory().removeItem(is);
            }
        }
        for (ItemStack is : rt.getOutput()) {
            chest.getInventory().addItem(is);
        }*/
        Map<Material, Integer> upkeepMap = new EnumMap<Material, Integer>(Material.class);
        Map<Material, Integer> outputMap = new EnumMap<Material, Integer>(Material.class);
        for (ItemStack is : rt.getUpkeep()) {
            if (is != null)
                upkeepMap.put(is.getType(), is.getAmount());
        }
        for (ItemStack is: rt.getOutput()) {
            if (is != null)
                outputMap.put(is.getType(), is.getAmount());
        }
        ItemStack[] is = chest.getInventory().getContents();
        ItemStack[] realIS = is.clone();
        for (int i = 0 ; i<realIS.length; i++) {
            int maxSize = realIS[i].getMaxStackSize();
            Material mat = Material.AIR;
            if (realIS[i] != null)
                mat = realIS[i].getType();
            //chest has an item and item is in upkeep
            if (!mat.equals(Material.AIR) && upkeepMap.containsKey(mat)) {
                //chest amount is <= upkeep amount
                if (realIS[i].getAmount() <= upkeepMap.get(mat)) {
                    upkeepMap.put(mat, upkeepMap.get(mat) - realIS[i].getAmount());
                    is[i] = null;
                //chest amount is > upkeep amount
                } else {
                    int amount = realIS[i].getAmount() - upkeepMap.get(mat);
                    upkeepMap.remove(mat);
                    is[i].setAmount(amount);
                }
            } else if (!mat.equals(Material.AIR) && outputMap.containsKey(mat) && realIS[i].getAmount() < maxSize) {
                if (realIS[i].getAmount() + outputMap.get(mat) <= maxSize) {
                    is[i].setAmount(is[i].getAmount() + outputMap.get(mat));
                    outputMap.remove(mat);
                } else {
                    int excess = is[i].getAmount() + outputMap.get(mat) - maxSize;
                    is[i].setAmount(maxSize);
                    outputMap.put(mat, excess);
                }
            } else if (mat.equals(Material.AIR) && !outputMap.isEmpty()) {
                for (Material currentMat : outputMap.keySet()) {
                    int maxSize2 = new ItemStack(currentMat).getMaxStackSize();
                    if (outputMap.get(currentMat) <= maxSize2) {
                        is[i] = new ItemStack(currentMat, outputMap.get(currentMat));
                        outputMap.remove(currentMat);
                    } else {
                        is[i] = new ItemStack(currentMat, maxSize2);
                        outputMap.put(currentMat, outputMap.get(currentMat) - maxSize2);
                    }
                    break;
                }
            }
        }
        chest.getInventory().setContents(is);
        chest.update(true);
        return true;
    }
    
    public void init(HeroStronghold plugin) {
        this.plugin = plugin;
    }
}
