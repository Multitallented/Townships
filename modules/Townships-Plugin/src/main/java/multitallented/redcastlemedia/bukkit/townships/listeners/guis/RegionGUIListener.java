package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

/**
 *
 * @author Multitallented
 */
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
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

public class RegionGUIListener implements Listener {
    private static RegionManager rm;
    public RegionGUIListener(RegionManager rm) {
        this.rm = rm;
    }

    public static void openCategoryInventory(Player player) {
        HashMap<String, ArrayList<String>> categories = rm.getRegionCategories();
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
        //Inventory inv = Bukkit.createInventory(null, size, ChatColor.RED + "Townships Categories");
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), size, ChatColor.RED + "Built Categories");

        HashSet<String> regionCategories = new HashSet<String>();
        for (Region r : rm.getSortedRegions()) {
            if (!r.getOwners().contains(player.getName())) {
                continue;
            }
            for (String category : rm.getRegionCategories().keySet()) {
                if (regionCategories.contains(category)) {
                    if (rm.getRegionCategories().get(category).contains(r.getType())) {
                        break;
                    }
                    continue;
                }

                if (rm.getRegionCategories().get(category).contains(r.getType())) {
                    regionCategories.add(category);
                    break;
                }
            }
        }


        int i = 0;
        for (String category : regionCategories) {

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
            boolean hasAtLeastOne = false;

            for (SuperRegion sr : rm.getSortedSuperRegions()) {
                if (sr.hasOwner(player.getName())) {
                    hasAtLeastOne = true;
                    break;
                }
            }

            if (hasAtLeastOne) {
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
        }

        player.openInventory(inv);
    }

    public static void openListInventory(ArrayList<Region> regions, ArrayList<SuperRegion> superRegions, Player player, String category) {
        int size = 9;
        int actualSize = regions.size() + superRegions.size() + 1;
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        category = category.toLowerCase();
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.RED + WordUtils.capitalize(category) +  " Builds");

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        int i = 0;
        for (Region region : regions) {
            RegionType regionType = rm.getRegionType(region.getType());
            ItemStack is = new ItemStack(regionType.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            String displayName = ChatColor.RESET + regionType.getName() + " " + region.getID();
            ArrayList<String> lore = new ArrayList<String>();

            ArrayList<SuperRegion> superContRegions = rm.getContainingSuperRegions(region.getLocation());
            if (!superContRegions.isEmpty()) {
                lore.add(ChatColor.GOLD + superContRegions.get(0).getName() + " " + Math.round(region.getLocation().getX()) + "x " + Math.round(region.getLocation().getY()) + "y " + Math.round(region.getLocation().getZ()) + "z");
            } else {
                lore.add(ChatColor.GOLD + "wild " + Math.round(region.getLocation().getX()) + "x " + Math.round(region.getLocation().getY()) + "y " + Math.round(region.getLocation().getZ()) + "z");
            }
            if (Effect.hasReagents(rm, region)) {
                lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Region is working");
            } else {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "Missing needed items/money to work");
            }

            if (region.getOwners().isEmpty() || !region.getOwners().get(0).equals(player.getName())) {
                lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "You co-own this region");
            } else {
                lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "You own this region");
            }

            if (regionType.getDescription() != null && !regionType.getDescription().equals("")) {
                lore.addAll(Util.textWrap(ChatColor.RESET + "" + ChatColor.GOLD, regionType.getDescription()));
            }
            isMeta.setDisplayName(displayName);

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


            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        for (SuperRegion superRegion : superRegions) {
            SuperRegionType superRegionType = rm.getSuperRegionType(superRegion.getType());
            ItemStack is = new ItemStack(superRegionType.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            String displayName = ChatColor.RESET + superRegionType.getName();
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "Super Region");
            if (superRegion.getBalance() > 0) {
                lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Town Bank: " + superRegion.getBalance());
            } else {
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "Town Bank: " + superRegion.getBalance());
                lore.add(ChatColor.RESET + "" + ChatColor.RED + "Deposit money immediately or risk destruction!");
            }

            if (superRegionType.getDescription() != null && !superRegionType.getDescription().equals("")) {
                lore.addAll(Util.textWrap(ChatColor.GOLD + "", superRegionType.getDescription()));
            }
            isMeta.setDisplayName(displayName);

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

            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        ItemStack is = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta isMeta = is.getItemMeta();
        isMeta.setDisplayName(ChatColor.RESET + "Back to Categories");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add("list " + category);
        isMeta.setLore(lore);
        is.setItemMeta(isMeta);
        inv.setItem(size - 1 , is);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        String name = ChatColor.stripColor(event.getInventory().getName());
        String category = "";
        boolean isCategory = name.equalsIgnoreCase("Built Categories");
        String[] names = name.split(" ");
        if (!isCategory) {
            if (names.length != 2 || !names[1].equals("Builds")) {
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
            player.performCommand("to built " + categoryOpen);
            return;
        }

        if (event.getCurrentItem().hasItemMeta() && ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("Back to Categories")) {
            player.closeInventory();
            RegionGUIListener.openCategoryInventory(player);
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        int id = -1;
        try {
            id = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().split(" ")[1]);
        } catch (Exception e) {

        }
        if (id < 0) {
            return;
        }
        Region region = rm.getRegionByID(id);
        if (region == null) {
            return;
        }
        RegionInfoGUIListener.openInfoInventory(region, player, "built " + category);
    }
}