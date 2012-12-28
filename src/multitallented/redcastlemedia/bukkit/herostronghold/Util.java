/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitallented.redcastlemedia.bukkit.herostronghold;

import java.util.ArrayList;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author multitallented
 * @author Alex_M
 */
public class Util {
    public static boolean validateFileName(String fileName) {
        return fileName.matches("^[^.\\\\/:*?\"<>|]?[^\\\\/:*?\"<>|]*") 
        && getValidFileName(fileName).length()>0;
    }

    public static String getValidFileName(String fileName) throws IllegalStateException {
        String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
        if(newFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fileName + " results in a empty fileName!");
        return newFileName;
    }
    
    public static boolean containsItems(ArrayList<ItemStack> req, Inventory inv) {
        if (inv == null) {
            return false;
        }
        outer: for (ItemStack is : req) {
            if (is == null) {
                continue;
            }
            int amount = is.getAmount();
            for (ItemStack iss : inv.getContents()) {
                if (iss == null) {
                    continue;
                }
                if (iss.getType() == is.getType()) {
                    amount -= iss.getAmount();
                    if (amount < 1) {
                        continue outer;
                    }
                }
            }
            return false;
        }
        return true;
    }
    
    public static boolean removeItems(ArrayList<ItemStack> removeItems, Inventory inv) {
        ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
        boolean removedAll = true;
        outer: for (ItemStack is : removeItems) {
            if (is == null) {
                continue;
            }
            int amount = is.getAmount();
            for (int i=0; i<inv.getSize(); i++) {
                ItemStack iss = inv.getItem(i);
                if (iss == null) {
                    continue;
                }
                if (iss.getType() == is.getType()) {
                    if (iss.getAmount() > amount) {
                        iss.setAmount(iss.getAmount() - amount);
                        continue outer;
                    } else {
                        removeIndexes.add(i);
                        amount -= iss.getAmount();
                    }
                    if (amount < 1) {
                        continue outer;
                    }
                }
            }
            removedAll = false;
            break;
        }
        
        for (Integer i : removeIndexes) {
            inv.setItem(i, null);
        }
        
        return removedAll;
    }
    
    public static ArrayList<ItemStack> addItems(ArrayList<ItemStack> addItems, Inventory inv) {
        ArrayList<ItemStack> remainingItems = new ArrayList<ItemStack>();
        outer: for (ItemStack is : addItems) {
            if (is == null) {
                continue;
            }
            int amount = is.getAmount();
            int max = is.getMaxStackSize();
            for (ItemStack iss : inv) {
                if (iss == null) {
                    if (amount > max) {
                        inv.addItem(new ItemStack(is.getType(), max));
                        amount -= max;
                        continue;
                    } else {
                        inv.addItem(new ItemStack(is.getType(), amount));
                        continue outer;
                    }
                }
                if (iss.getType() == is.getType() && iss.getDurability() == is.getDurability() && iss.getAmount() < iss.getMaxStackSize()) {
                    if (amount + iss.getAmount() > iss.getMaxStackSize()) {
                        amount = amount - (iss.getMaxStackSize() - iss.getAmount());
                        iss.setAmount(iss.getMaxStackSize());
                    } else {
                        iss.setAmount(amount + iss.getAmount());
                        continue outer;
                    }
                }
            }
            remainingItems.add(new ItemStack(is.getType(), amount));
        }
        
        return remainingItems;
    }
    
    public static ArrayList<ItemStack> matchItems(ArrayList<ItemStack> list1, ArrayList<ItemStack> list2) {
        return null;
    }
    
    public static boolean hasRequiredBlocks(Region region, RegionManager rm) {
        if (region.getLocation().getBlock().getType() != Material.CHEST) {
            return false;
        }
        
        RegionType rt = rm.getRegionType(region.getType());
        int x = (int) region.getLocation().getX() - rt.getRawBuildRadius();
        int y = (int) region.getLocation().getY() - rt.getRawBuildRadius();
        y = y < 0 ? 0 : y;
        int z = (int) region.getLocation().getZ() - rt.getRawBuildRadius();
        int xMax = (int) region.getLocation().getX() + rt.getRawBuildRadius();
        int yMax = (int) region.getLocation().getY() + rt.getRawBuildRadius();
        yMax = yMax > region.getLocation().getWorld().getMaxHeight() - 1 ? region.getLocation().getWorld().getMaxHeight() - 1 : yMax;
        int zMax = (int) region.getLocation().getZ() + rt.getRawBuildRadius();
        World world = region.getLocation().getWorld();
        
        HashMap<Material, Integer> requirements = new HashMap<Material, Integer>();
        for (ItemStack is : rt.getRequirements()) {
            requirements.put(is.getType(), is.getAmount());
        }
        
        for (int i = x; i < xMax; i++) {
            for (int j = y; j < yMax; j++) {
                for (int k = z; k < zMax; k++) {
                    Material mat = world.getBlockAt(i, j, k).getType();
                    if (requirements.containsKey(mat)) {
                        requirements.put(mat, requirements.get(mat) - 1);
                        if (requirements.get(mat) < 1) {
                            requirements.remove(mat);
                        }
                    }
                }
            }
        }
        if (!requirements.isEmpty()) {
            return false;
        }
        
        return true;
    }
}
