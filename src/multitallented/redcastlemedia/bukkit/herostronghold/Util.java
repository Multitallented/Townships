/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitallented.redcastlemedia.bukkit.herostronghold;

import java.util.ArrayList;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.herostronghold.region.HSItem;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import net.milkbowl.vault.item.Items;
import net.minecraft.server.v1_7_R3.NBTTagCompound;
import net.minecraft.server.v1_7_R3.NBTTagList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author multitallented
 * @author Alex_M
 */
public class Util {
    public static CycleItems cycles = new CycleItems();
    
    public static class CycleItems implements Runnable {
        private final HashMap<Inventory, HashMap<Integer, HashMap<Integer, ArrayList<HSItem>>>> threads = new HashMap<Inventory, HashMap<Integer, HashMap<Integer, ArrayList<HSItem>>>>();
        
        @Override
        public void run() {
            while (!threads.isEmpty()) {
                for (Inventory inv : threads.keySet()) {
                    for (Integer index : threads.get(inv).keySet()) {
                        HashMap<Integer, ArrayList<HSItem>> data = threads.get(inv).get(index);
                        
                        for (Integer position : data.keySet()) {
                            int pos = position;
                            if (data.get(position).size() - 2 > position) {
                                pos = 0;
                            }
                            HSItem nextItem = data.get(pos).get(pos);
                            ItemStack is;
                            if (nextItem.isWildDamage()) {
                                is = new ItemStack(nextItem.getMat(), nextItem.getQty());
                                ItemMeta isMeta = is.getItemMeta();
                                ArrayList<String> lore = new ArrayList<String>();
                                lore.add("Any type acceptable");
                                isMeta.setLore(lore);
                                is.setItemMeta(isMeta);
                            } else {
                                is = new ItemStack(nextItem.getMat(), nextItem.getQty(), (short) nextItem.getDamage());
                            }
                            inv.setItem(index, is);
                            break;
                        }
                    }
                }
                try {
                    this.wait(2000);
                } catch (Exception e) {
                    
                }
            }
        }
        
        public void addItemCycle(Inventory inv, int index, ArrayList<HSItem> items) {
            HashMap<Integer, HashMap<Integer, ArrayList<HSItem>>> indexes = new HashMap<Integer, HashMap<Integer, ArrayList<HSItem>>>();
            HashMap<Integer, ArrayList<HSItem>> positions = new HashMap<Integer, ArrayList<HSItem>>();
            indexes.put(index, positions);
            threads.put(inv, indexes);
            
            if (threads.size() < 2) {
                run();
            }
        }
        
        public void removeCycleItem(Inventory inv) {
            threads.remove(inv);
        }
    }
    
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
    
    public static ArrayList<String> textWrap(String prefix, String input) {
        ArrayList<String> lore = new ArrayList<String>();
        String sendMe = new String(input);
        String[] sends = sendMe.split(" ");
        String outString = "";
        for (String s : sends) {
            if (outString.length() > 40) {
                lore.add(outString);
                outString = "";
            }
            if (!outString.equals("")) {
                outString += prefix + " ";
            } else {
                outString += prefix;
            }
            outString += s;
        }
        lore.add(outString);
        return lore;
    }
    
    public static ItemStack removeAttributes(ItemStack is) {
        net.minecraft.server.v1_7_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag;
        if (!nmsStack.hasTag()){
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        else {
            tag = nmsStack.getTag();
        }
        NBTTagList am = new NBTTagList();
        tag.set("AttributeModifiers", am);
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }
    
    public static boolean containsItems(ArrayList<ArrayList<HSItem>> req, Inventory inv) {
        if (inv == null) {
            return false;
        }
        
        outer: for (ArrayList<HSItem> orReqs : req) {
            for (HSItem orReq : orReqs) {

                int amount = 0;
                for (ItemStack iss : inv.getContents()) {
                    if (iss == null) {
                        continue;
                    }
                    
                    if (iss.getType() == orReq.getMat() && (orReq.isWildDamage() || orReq.getDamage() == (int) (iss.getDurability()))) {
                        if ((iss.getAmount() + amount) >= orReq.getQty()) {
                            continue outer;
                        } else {
                            amount += iss.getAmount();
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }
    
    public static boolean removeItems(ArrayList<ArrayList<HSItem>> req, Inventory inv) {
        if (inv == null) {
            return false;
        }
        
        //clone the list
        ArrayList<ArrayList<HSItem>> hsItemsList = new ArrayList<ArrayList<HSItem>>();
        for (ArrayList<HSItem> hsItems : req) {
            ArrayList<HSItem> tempList = new ArrayList<HSItem>();
            for (HSItem hsItem : hsItems) {
                tempList.add(hsItem.clone());
            }
            hsItemsList.add(tempList);
        }
        
        
        ArrayList<Integer> removeItems = new ArrayList<Integer>();
        HashMap<Integer, Integer> reduceItems = new HashMap<Integer, Integer>();
        
        for (int i =0; i< inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) {
                continue;
            }
            
            int j=0;
            boolean removeIndex = false;
            outer1: for (ArrayList<HSItem> hsItems : hsItemsList) {
                for (HSItem hsItem : hsItems) {
                    if (item.getType() == hsItem.getMat() && (hsItem.isWildDamage() || hsItem.getDamage() == (int) item.getDurability())) {
                        
                        if (item.getAmount() > hsItem.getQty()) {
                            reduceItems.put(i, hsItem.getQty());
                            removeIndex = true;
                        } else if (item.getAmount() == hsItem.getQty()) {
                            removeItems.add(i);
                            removeIndex = true;
                        } else {
                            removeItems.add(i);
                            hsItem.setQty(hsItem.getQty() - item.getAmount());
                        }
                        break outer1;
                        
                    }
                }
                j++;
            }
            if (removeIndex) {
                hsItemsList.remove(j);
            }
        }
        
        if (!hsItemsList.isEmpty()) {
            return false;
        }
        
        for (Integer i : reduceItems.keySet()) {
            inv.getItem(i).setAmount(inv.getItem(i).getAmount() - reduceItems.get(i));
        }
        
        for (Integer i : removeItems) {
            inv.setItem(i, null);
        }
        
        return true;
        
        /*ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
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
        
        return removedAll;*/
    }
    
    public static ArrayList<ItemStack> addItems(ArrayList<ArrayList<HSItem>> addItems, Inventory inv) {
        ArrayList<ItemStack> remainingItems = new ArrayList<ItemStack>();
        
        outer: for (ArrayList<HSItem> tempItems : addItems) {
            double rand = Math.random();
            double prevChance = 0;
            for (HSItem item : tempItems) {
                if ((prevChance < rand) && (prevChance + item.getChance() > rand)) {
                    ItemStack is = null;
                    if (!item.isWildDamage()) {
                        is = new ItemStack(item.getMat(), 1, (short) item.getDamage());
                    } else {
                        is = new ItemStack(item.getMat(), 1);
                    }
                    if (inv == null) {
                        remainingItems.add(is);
                        continue;
                    }
                    int amount = item.getQty();
                    int max = is.getMaxStackSize();
                    int damageValue = item.isWildDamage() ? 0 : item.getDamage();
                    for (ItemStack iss : inv) {
                        if (iss == null) {
                            if (amount > max) {
                                inv.addItem(new ItemStack(is.getType(), max, (short) damageValue));
                                amount -= max;
                                continue;
                            } else {
                                inv.addItem(new ItemStack(is.getType(), amount, (short) damageValue));
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
                    
                    if (amount > 0) {
                        is.setAmount(amount);
                        remainingItems.add(is);
                    }
                    continue outer;
                    
                }
                prevChance += item.getChance();
            }
        }
        /*outer: for (ArrayList<HSItem> iss : addItems) {
            if (iss == null) {
                continue;
            }
            for (HSItem is : iss) {
                double rand = Math.random();
                double prevChance = 0;
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
        }*/
        
        return remainingItems;
    }
    
    public static String hasCreationRequirements(Location loc, RegionType rt, RegionManager rm) {
        if (rt.getRequirements().isEmpty()) {
            return "";
        }
        
        ArrayList<ArrayList<HSItem>> reqMap = new ArrayList<ArrayList<HSItem>>();

        for (ArrayList<HSItem> currentStack : rt.getRequirements()) {
            ArrayList<HSItem> tempMap = new ArrayList<HSItem>();
                    
            for (HSItem hsItem : currentStack) {
                tempMap.add(hsItem.clone());
            }
            reqMap.add(tempMap);
        }
        
        int x = (int) loc.getX() - rt.getRawBuildRadius();
        int y = (int) loc.getY() - rt.getRawBuildRadius();
        y = y < 0 ? 0 : y;
        int z = (int) loc.getZ() - rt.getRawBuildRadius();
        int xMax = (int) loc.getX() + rt.getRawBuildRadius();
        int yMax = (int) loc.getY() + rt.getRawBuildRadius();
        yMax = yMax > loc.getWorld().getMaxHeight() - 1 ? loc.getWorld().getMaxHeight() - 1 : yMax;
        int zMax = (int) loc.getZ() + rt.getRawBuildRadius();
        World world = loc.getWorld();

        for (int i = x; i < xMax; i++) {
            for (int j = y; j < yMax; j++) {
                for (int k = z; k < zMax; k++) {
                    ItemStack is = world.getBlockAt(i,j,k).getState().getData().toItemStack();
                    
                    int p = 0;
                    boolean destroyIndex = false;
                    outer1: for (ArrayList<HSItem> tempMap : reqMap) {
                        for (HSItem item : tempMap) {
                            if (item.getMat() == is.getType() && (item.isWildDamage() || item.damageMatches(is.getDurability()))) {
                                if (item.getQty() < 2) {
                                    destroyIndex = true;
                                } else {
                                    item.setQty(item.getQty() - 1);
                                }
                                break outer1;
                            }
                        }

                        
                        p++;
                    }
                    if (destroyIndex) {
                        reqMap.remove(p);
                        
                        if (reqMap.isEmpty()) {
                            return "";
                        }
                    }
                }
            }
        }
        
        String message = "";
        
        for (ArrayList<HSItem> items : reqMap) {
            for (HSItem item : items) {
                String itemName = "";
                if (item.isWildDamage()) {
                    itemName = item.getMat().name();
                } else {
                    ItemStack ist = new ItemStack(item.getMat(), 1, (short) item.getDamage());
                    itemName = Items.itemByStack(ist).getName();
                }
                message += item.getQty() + ":" + itemName + " or ";
            }
            message = message.substring(0, message.length() - 4);
            message += ", ";
        }
        
        return message.substring(0, message.length() -2);
    }
    
    public static boolean hasRequiredBlocks(Location loc, RegionType rt, RegionManager rm) {
        if (loc.getBlock().getType() != Material.CHEST) {
            return false;
        }
        
        if (rt.getRequirements().isEmpty()) {
            return true;
        }
        
        ArrayList<ArrayList<HSItem>> reqMap = new ArrayList<ArrayList<HSItem>>();

        for (ArrayList<HSItem> currentStack : rt.getRequirements()) {
            ArrayList<HSItem> tempMap = new ArrayList<HSItem>();
                    
            for (HSItem hsItem : currentStack) {
                tempMap.add(hsItem.clone());
            }
            reqMap.add(tempMap);
        }
        
        int x = (int) loc.getX() - rt.getRawBuildRadius();
        int y = (int) loc.getY() - rt.getRawBuildRadius();
        y = y < 0 ? 0 : y;
        int z = (int) loc.getZ() - rt.getRawBuildRadius();
        int xMax = (int) loc.getX() + rt.getRawBuildRadius();
        int yMax = (int) loc.getY() + rt.getRawBuildRadius();
        yMax = yMax > loc.getWorld().getMaxHeight() - 1 ? loc.getWorld().getMaxHeight() - 1 : yMax;
        int zMax = (int) loc.getZ() + rt.getRawBuildRadius();
        World world = loc.getWorld();

        for (int i = x; i < xMax; i++) {
            for (int j = y; j < yMax; j++) {
                for (int k = z; k < zMax; k++) {
                    ItemStack is = world.getBlockAt(i,j,k).getState().getData().toItemStack();
                    
                    int p = 0;
                    boolean destroyIndex = false;
                    outer1: for (ArrayList<HSItem> tempMap : reqMap) {
                        for (HSItem item : tempMap) {
                            if (item.getMat() == is.getType() && (item.isWildDamage() || item.damageMatches(is.getDurability()))) {
                                if (item.getQty() < 2) {
                                    destroyIndex = true;
                                } else {
                                    item.setQty(item.getQty() - 1);
                                }
                                break outer1;
                            }
                        }

                        
                        p++;
                    }
                    if (destroyIndex) {
                        reqMap.remove(p);
                        
                        if (reqMap.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean hasRequiredBlocks(Region region, RegionManager rm) {
        return Util.hasRequiredBlocks(region.getLocation(), rm.getRegionType(region.getType()), rm);
    }
    
    public static ItemStack stringToItemStack(String input) {
        try {
            String[] inputArray = input.split("\\.");
            ItemStack returnStack;
            if (inputArray.length > 2) {
                int qty = Integer.parseInt(inputArray[2]);
                int damage = Integer.parseInt(inputArray[1]);
                Material mat = Material.getMaterial(inputArray[0]);
                if (mat == null) {
                    returnStack = new ItemStack(Integer.parseInt(inputArray[0]), qty, (short) damage);
                } else {
                    returnStack = new ItemStack(Material.getMaterial(inputArray[0]), qty, (short) damage);
                }
            } else if (inputArray.length > 1) {
                int qty = Integer.parseInt(inputArray[1]);
                Material mat = Material.getMaterial(inputArray[0]);
                if (mat == null) {
                    returnStack = new ItemStack(Integer.parseInt(inputArray[0]), qty);
                } else {
                    returnStack = new ItemStack(Material.getMaterial(inputArray[0]), qty);
                }
            } else {
                Material mat = Material.getMaterial(inputArray[0]);
                if (mat == null) {
                    returnStack = new ItemStack(Integer.parseInt(inputArray[0]));
                } else {
                    returnStack = new ItemStack(Material.getMaterial(inputArray[0]));
                }
            }
            return returnStack;
        } catch (Exception e) {
            return new ItemStack(1);
        }
    }
}
