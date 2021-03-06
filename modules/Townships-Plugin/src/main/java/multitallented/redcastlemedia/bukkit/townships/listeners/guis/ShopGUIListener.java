package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

/**
 *
 * @author Phoenix_Frenzy
 * @author Multitallented
 */
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.region.*;
import net.milkbowl.vault.item.Items;
//import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUIListener implements Listener {
    private static RegionManager rm;
    private static Townships to;
    public ShopGUIListener(Townships to) {
        ShopGUIListener.to = to;
        ShopGUIListener.rm = to.getRegionManager();
    }
    
    public static void openCategoryShop(Player player, boolean showAll) {
        HashMap<String, ArrayList<String>> categories;
        if (showAll) {
            categories = rm.getRegionCategories();
        } else {
            categories = determineVisibleCategories(player);
        }
        int size = 9;
        int actualSize = categories.keySet().size();
        boolean hasSuperRegions = !rm.getSuperRegionTypes().isEmpty();
        actualSize = hasSuperRegions ? actualSize + 1 : actualSize;
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        //Inventory inv = Bukkit.createInventory(null, size, ChatColor.RED + "Townships Groups");
        String title;
        if (showAll) {
            title = ChatColor.RED + "All Shop Groups";
        } else {
            title = ChatColor.RED + "Shop Groups";
        }
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), 
                size, title);
        
        
        int i = 0;
        for (String category : categories.keySet()) {
            if (category.equals("")) {
                category = "Other";
            }
            Material mat = Townships.getConfigManager().getCategory(category.toLowerCase());
            if (mat == null) {
                mat = Material.CHEST;
            }
            ItemStack is = new ItemStack(mat);
            ItemMeta isMeta = is.getItemMeta();
            isMeta.setDisplayName(ChatColor.RESET + WordUtils.capitalize(category));
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        if (hasSuperRegions) {
            Material mat = Townships.getConfigManager().getCategory("towns");
            if (mat == null) {
                mat = Material.CHEST;
            }
            ItemStack is = new ItemStack(mat);
            ItemMeta isMeta = is.getItemMeta();
            isMeta.setDisplayName(ChatColor.RESET + "Towns");
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
        }
        
        player.openInventory(inv);
    }

    private static HashMap<String, ArrayList<String>> determineVisibleCategories(Player player) {
        HashMap<String, ArrayList<String>> returnCategories = new HashMap<String, ArrayList<String>>(rm.getRegionCategories());

        ArrayList<String> removeMe = new ArrayList<String>();
        for (String categoryName : returnCategories.keySet()) {
            ArrayList<String> categoryRegions = determineVisibleRegions(player, returnCategories.get(categoryName));
            if (categoryRegions.isEmpty()) {
                removeMe.add(categoryName);
            }
        }
        for (String removePlease : removeMe) {
            returnCategories.remove(removePlease);
        }

        return returnCategories;
    }

    public static ArrayList<String> determineVisibleRegions(Player player, ArrayList<String> baseRegions) {
        ArrayList<String> returnList = new ArrayList<String>(baseRegions);

        HashMap<String, Boolean> superRegions = new HashMap<String, Boolean>();
        HashSet<String> permRegions = new HashSet<String>();
        for (SuperRegion sr : rm.getSortedSuperRegions()) {
            if (sr.hasOwner(player.getName()) && (!superRegions.containsKey(sr.getType()) || !superRegions.get(sr.getType()))) {
                superRegions.put(sr.getType(), true);
            }
            if (sr.hasMember(player.getName())) {
                permRegions.addAll(sr.getMember(player.getName()));
            }
        }


        ArrayList<String> removeMe = new ArrayList<String>();
        regionLoop: for (String regionName : returnList) {
            RegionType rt = rm.getRegionType(regionName);
            //TODO filter out regions here

            //super region filter
            boolean srPerm = rt.getSuperRegions().isEmpty();
            for (String sr : rt.getSuperRegions()) {
                if (superRegions.containsKey(sr) && (superRegions.get(sr) || permRegions.contains(regionName))) {
                    srPerm = true;
                }
            }
            if (!srPerm) {
                removeMe.add(regionName);
                continue;
            }

            //filter for regions without unlock costs
            if (rt.getUnlockCost() < 1) {
                removeMe.add(regionName);
                continue;
            }
        }

        for (String removePlease : removeMe) {
            returnList.remove(removePlease);
        }

        return returnList;
    }

    public static void openListShop(ArrayList<RegionType> regions, ArrayList<SuperRegionType> superRegions, Player player, String category, boolean showAll) {
        int size = 9;
        int actualSize = regions.size() + superRegions.size() + 1;
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        
        category = category.toLowerCase();
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), 
                size, ChatColor.RED + WordUtils.capitalize(category) +  " Shop");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        int i = 0;
        for (RegionType r : regions) {
            ItemStack is = new ItemStack(r.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            String displayName = ChatColor.RESET + r.getName();
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "Region");
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Unlock Cost: " + formatter.format(r.getUnlockCost()));
            if (r.getDescription() != null && !r.getDescription().equals("")) {
                lore.addAll(Util.textWrap(ChatColor.RESET + "" + ChatColor.GOLD, r.getDescription()));
            }
            if (r.getMoneyRequirement() > 0) {
                lore.add(ChatColor.RESET + "" + ChatColor.BLUE + "Build Cost: " + formatter.format(r.getMoneyRequirement()));
            }
            /*if (r.getRequirements().size() > 0) {
                lore.add("Requirements");
                for (ArrayList<TOItem> items : r.getRequirements()) {
                    String reagents = "";
                    for (TOItem item : items) {
                        if (!reagents.equals("")) {
                            reagents += " or ";
                        }
                        String itemName = "";
                        if (item.isWildDamage()) {
                            itemName = item.getMat().name().replace("_", " ").toLowerCase();
                        } else {
                            ItemStack ist = new ItemStack(item.getMat(), 1, (short) item.getDamage());
                            itemName = Items.itemByStack(ist).getName();
                        }
                        reagents += item.getQty() + " " + itemName;
                    }
                    lore.addAll(Util.textWrap("", reagents));
                }
            }*/
            //Trim lore
            trimLore: {
                boolean addEllipses = lore.size() > 20;
                if (addEllipses) {
                    for (int k = lore.size(); k > 19; k--) {
                        lore.remove(k-1);
                    }
                    lore.add("To be continued...");
                }
            }
            
            isMeta.setDisplayName(displayName);
            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        for (SuperRegionType sr : superRegions) {
            ItemStack is = new ItemStack(sr.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            String displayName = ChatColor.RESET + sr.getName();
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "Super Region");
            lore.add(ChatColor.RESET  + "" + ChatColor.RED + "Unlock Cost: " + formatter.format(sr.getUnlockCost()));
            if (sr.getDescription() != null && !sr.getDescription().equals("")) {
                lore.addAll(Util.textWrap(ChatColor.GOLD + "", sr.getDescription()));
            }
            if (sr.getChildren().size() > 0) {
                lore.add(ChatColor.GREEN + "Upgrade from:");
                int lineCount = 0;
                String childString = "";
                for (String srt : sr.getChildren()) {
                    if (!childString.equals("")) {
                        childString += ", ";
                    } else {
                        childString += ChatColor.GREEN + "";
                    }
                    lineCount += srt.length();
                    if (lineCount > 50) {
                        lore.add(childString);
                        childString = new String();
                        lineCount = srt.length();
                    }
                    childString += srt;
                }
                lore.add(childString);
            }
            if (sr.getMoneyRequirement() > 0) {
                lore.add("Build Cost: " + formatter.format(sr.getMoneyRequirement()));
            }
            if (sr.getRequirements().size() > 0) {
                lore.add(ChatColor.BLUE + "Requirements:");
                for (String s : sr.getRequirements().keySet()) {
                    lore.add(ChatColor.BLUE + " " + sr.getRequirement(s) + " " + s);
                }
            }
            
            //Trim lore
            trimLore: {
                boolean addEllipses = lore.size() > 20;
                if (addEllipses) {
                    for (int k = lore.size(); k > 19; k--) {
                        lore.remove(k-1);
                    }
                    lore.add("To be continued...");
                }
            }
            isMeta.setDisplayName(displayName);
            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        ItemStack is = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta isMeta = is.getItemMeta();
        if (showAll) {
            isMeta.setDisplayName(ChatColor.RESET + "Back to All Groups");
        } else {
            isMeta.setDisplayName(ChatColor.RESET + "Back to Groups");
        }
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("list " + category);
        isMeta.setLore(lore);
        is.setItemMeta(isMeta);
        inv.setItem(size - 1 , is);
        player.openInventory(inv);
    }
    
    public static void openConfirmation(Player player, String regionName) {
        int size = 9;
        
        
        //Inventory inv = Bukkit.createInventory(null, size, ChatColor.RED + "Townships Groups");
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), 
                size, ChatColor.RED + "Unlock Confirmation");
        
        double unlockCost = 0;
        RegionType rt = rm.getRegionType(regionName);
        SuperRegionType srt = rm.getSuperRegionType(regionName);
        if (rt != null) {
            unlockCost = rt.getUnlockCost();
            ItemStack iconStack = new ItemStack(rt.getIcon());
            ItemMeta iconMeta = iconStack.getItemMeta();
            iconMeta.setDisplayName(org.apache.commons.lang.WordUtils.capitalize(rt.getName()));
            iconStack.setItemMeta(iconMeta);
            inv.setItem(0, iconStack);
        } else if (srt != null) {
            unlockCost = srt.getUnlockCost();
            ItemStack iconStack = new ItemStack(srt.getIcon());
            ItemMeta iconMeta = iconStack.getItemMeta();
            iconMeta.setDisplayName(org.apache.commons.lang.WordUtils.capitalize(srt.getName()));
            iconStack.setItemMeta(iconMeta);
            inv.setItem(0, iconStack);
        } else {
            return;
        }
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        ItemStack is = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Unlock for " + formatter.format(unlockCost));
        is.setItemMeta(isMeta);
        inv.setItem(3, is);
        
        is = new ItemStack(Material.SIGN);
        isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "More Info");
        is.setItemMeta(isMeta);
        inv.setItem(4, is);
        
        is = new ItemStack(Material.REDSTONE_BLOCK);
        isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Cancel Purchase");
        is.setItemMeta(isMeta);
        inv.setItem(5, is);
        
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String name = ChatColor.stripColor(event.getInventory().getName());
        if (name.equals("Unlock Confirmation")) {
            event.setCancelled(true);
            String regionName = ChatColor.stripColor(event.getInventory().getContents()[0].getItemMeta().getDisplayName()).toLowerCase();
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                player.closeInventory();
                player.performCommand("to unlock " + regionName);
            } else if (event.getCurrentItem().getType() == Material.SIGN) {
                player.closeInventory();
                player.performCommand("to info " + regionName);
            } else if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
                player.closeInventory();
            }
            return;
        }
        
        String category = "";
        boolean showAll = name.equalsIgnoreCase("All Shop Groups");
        boolean isCategory = showAll || name.equalsIgnoreCase("Shop Groups");
        String[] names = name.split(" ");
        if (!isCategory) {
            if (names.length != 2 || !names[1].equalsIgnoreCase("Shop")) {
                return;
            } else {
                category = names[0].toLowerCase();
            }
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR
                ||!event.getCurrentItem().hasItemMeta()){
            //player.closeInventory();
            return;
        }
        if (isCategory) {
            String categoryOpen = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
            player.closeInventory();
            if (showAll) {
                player.performCommand("to shop " + categoryOpen + " all");
            } else {
                player.performCommand("to shop " + categoryOpen);
            }
            return;
        }

        if (event.getCurrentItem().hasItemMeta() && 
                ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("Back to Groups")) {
            player.closeInventory();
            player.performCommand("to shop");
            return;
        }
        if (event.getCurrentItem().hasItemMeta() &&
                ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("Back to All Groups")) {
            player.closeInventory();
            player.performCommand("to shop all");
            return;
        }

        //player.performCommand("to info " + ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
        String regionName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase();
        RegionType rt = rm.getRegionType(regionName);
        SuperRegionType srt = null;
        if (rt == null) {
            srt = rm.getSuperRegionType(regionName);
            if (srt == null) {
                player.closeInventory();
                return;
            }
        }

        //String backButton = ChatColor.stripColor(event.getInventory().getItem(event.getInventory().getSize() - 1).getItemMeta().getLore().get(0));
        player.closeInventory();
        player.performCommand("to confirm " + regionName);
    }
}