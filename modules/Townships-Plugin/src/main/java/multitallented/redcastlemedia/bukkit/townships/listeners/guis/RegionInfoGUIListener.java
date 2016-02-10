package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

/**
 *
 * @author Multitallented
 */

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.region.*;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RegionInfoGUIListener implements Listener {
    private static RegionManager rm;
    public RegionInfoGUIListener(RegionManager rm) {
        RegionInfoGUIListener.rm = rm;
    }
    
    public static void openInfoInventory(Region region, Player player, String back) {
        int size = 18;
        //Inventory inv = Bukkit.createInventory(null, size, ChatColor.RED + "Region Info");
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), size, ChatColor.RED + "Region");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        RegionType regionType = rm.getRegionType(region.getType());

        ItemStack iconStack = new ItemStack(regionType.getIcon());
        ItemMeta iconMeta = iconStack.getItemMeta();
        iconMeta.setDisplayName(WordUtils.capitalize(regionType.getName()) + " " + region.getID());
        ArrayList<String> lore = new ArrayList<String>();

        int diameter = (int) (Math.floor(regionType.getRawBuildRadius()) * 2 + 1);
        int effectDiameter = (int) (Math.floor(regionType.getRawRadius()) * 2 + 1);

        String sizeString = diameter + "x" + diameter + "x" + diameter;
        String rangeString = effectDiameter + "x" + effectDiameter + "x" + effectDiameter;

        lore.add(ChatColor.RESET + "" + ChatColor.RED + "Size: " + sizeString);
        if (effectDiameter != diameter) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Range: " + rangeString);
        }

        if (regionType.getDescription() != null && !regionType.getDescription().equals("")) {
            lore.addAll(Util.textWrap(ChatColor.RESET + "" + ChatColor.GOLD, regionType.getDescription()));
        }
        iconMeta.setLore(lore);
        iconStack.setItemMeta(iconMeta);
        inv.setItem(0, iconStack);

        {
            short damageValue = 14;
            if (Effect.hasReagents(rm, region)) {
                damageValue = 5;
            }
            ItemStack workingStack = new ItemStack(Material.WOOL, 1, damageValue);
            ItemMeta im = workingStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();
            if (damageValue == 5) {
                im.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Working");
                cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "This region is running smoothly");
            } else {
                im.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Not Working");
                cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "This region needs items or money");
            }
            im.setLore(cLore);
            workingStack.setItemMeta(im);
            inv.setItem(9, workingStack);
        }

        {
            ItemStack locationStack = new ItemStack(Material.COMPASS);
            ItemMeta im = locationStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();
            ArrayList<SuperRegion> superRegionArrayList = rm.getContainingSuperRegions(region.getLocation());
            if (!superRegionArrayList.isEmpty()) {
                cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + superRegionArrayList.get(0).getName());
            }
            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD +
                    Math.round(region.getLocation().getX()) + "x " +
                    Math.round(region.getLocation().getY()) + "y " +
                    Math.round(region.getLocation().getZ()) + "z");
            im.setLore(cLore);
            locationStack.setItemMeta(im);
            inv.setItem(10, locationStack);
        }

        {
            ItemStack infoStack = new ItemStack(Material.SIGN);
            ItemMeta im = infoStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Region Info");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to view all info");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "on " + regionType.getName());
            im.setLore(cLore);
            infoStack.setItemMeta(im);
            inv.setItem(11, infoStack);
        }

        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Add Member");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to add a player");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "as a member");
            im.setLore(cLore);
            im.setOwner("MHF_ArrowRight");
            playerItem.setItemMeta(im);
            inv.setItem(12, playerItem);
        }
        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Add Owner");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to add a player");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "as an owner");
            im.setLore(cLore);
            im.setOwner("MHF_ArrowUp");
            playerItem.setItemMeta(im);
            inv.setItem(13, playerItem);
        }
        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Set Main Owner");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to set as the");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "main owner");
            im.setLore(cLore);
            im.setOwner("MHF_Exclamation");
            playerItem.setItemMeta(im);
            inv.setItem(14, playerItem);
        }
        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Remove Player");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to remove a");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "player from the region");
            im.setLore(cLore);
            im.setOwner("MHF_ArrowDown");
            playerItem.setItemMeta(im);
            inv.setItem(15, playerItem);
        }

        {
            ItemStack backStack = new ItemStack(Material.TNT);
            ItemMeta backMeta = backStack.getItemMeta();
            backMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Destroy Region");
            lore = new ArrayList<String>();
            lore.add(ChatColor.RED + "You cant undo this.");
            backMeta.setLore(lore);
            backStack.setItemMeta(backMeta);
            inv.setItem(6, backStack);
        }


        ItemStack backStack = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta backMeta = backStack.getItemMeta();
        backMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Press to go BACK");
        lore = new ArrayList<String>();
        if (back == null) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Exit");
        } else {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + back);
        }
        backMeta.setLore(lore);
        backStack.setItemMeta(backMeta);
        inv.setItem(8, backStack);
        
        player.openInventory(inv);
    }
    
    public static void openInfoInventory(SuperRegionType region, Player player, String back) {
        int size = 18;
        //Inventory inv = Bukkit.createInventory(null, size, "Region Info");
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)),
                size, ChatColor.RED + "SuperRegion");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        
        ItemStack iconStack = new ItemStack(region.getIcon());
        ItemMeta iconMeta = iconStack.getItemMeta();
        iconMeta.setDisplayName(WordUtils.capitalize(region.getName()));
        ArrayList<String> lore = new ArrayList<String>();
        int diameter = (int) (Math.floor(region.getRawRadius()) * 2 + 1);
        String sizeString = diameter + "x" + diameter;
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "Size: " + sizeString);
        if (region.getDescription() != null && !region.getDescription().equals("")) {
            lore.addAll(Util.textWrap(ChatColor.RESET + "" + ChatColor.GOLD, region.getDescription()));
        }
        iconMeta.setLore(lore);
        iconStack.setItemMeta(iconMeta);
        inv.setItem(0, iconStack);
        
        ItemStack costStack = new ItemStack(Material.EMERALD);
        ItemMeta costMeta = costStack.getItemMeta();
        costMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Money:");
        if (region.getMoneyRequirement() > 0) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Build Cost: " + formatter.format(region.getMoneyRequirement()));
        }
        if (region.getOutput() > 0) {
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Per Use: +" + formatter.format(region.getOutput()));
        } else if (region.getOutput() < 0) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Per Use: " + formatter.format(region.getOutput()));
        }
        costMeta.setLore(lore);
        costStack.setItemMeta(costMeta);
        inv.setItem(9, costStack);
        
        ItemStack requireStack = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta requireMeta = requireStack.getItemMeta();
        requireMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Requirements:");
        lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Build a town with these structures.");
        if (region.getRequirements().size() > 0) {
            lore.add(ChatColor.BLUE + "Requirements:");
            for (String s : region.getRequirements().keySet()) {
                lore.add(ChatColor.BLUE + " " + region.getRequirement(s) + " " + s);
            }
        }
        requireMeta.setLore(lore);
        requireStack.setItemMeta(requireMeta);
        inv.setItem(10, Util.removeAttributes(requireStack));
        
        ItemStack limitsStack = new ItemStack(Material.BEDROCK);
        ItemMeta limitsMeta = limitsStack.getItemMeta();
        limitsMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Limits:");
        lore = new ArrayList<String>();
        if (region.getRequirements().size() > 0) {
            lore.add(ChatColor.BLUE + "Max number of structures:");
            for (String s : region.getRegionLimits().keySet()) {
                lore.add(ChatColor.BLUE + " " + region.getRegionLimits().get(s) + " " + s);
            }
        }
        limitsMeta.setLore(lore);
        limitsStack.setItemMeta(limitsMeta);
        inv.setItem(11, Util.removeAttributes(limitsStack));
        
        ItemStack effectsStack = new ItemStack(Material.POTION, 1, (short) 1);
        ItemMeta effectMeta = effectsStack.getItemMeta();
        effectMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Effects:");
        effectMeta.setLore(region.getEffects());
        effectsStack.setItemMeta(effectMeta);
        inv.setItem(12, Util.removeAttributes(effectsStack));
        
        ItemStack backStack = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta backMeta = backStack.getItemMeta();
        backMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Press to go BACK");
        lore = new ArrayList<String>();
        if (back == null) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Exit");
        } else {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + back);
        }
        backMeta.setLore(lore);
        backStack.setItemMeta(backMeta);
        inv.setItem(8, backStack);
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName())
                .equalsIgnoreCase("Region") &&
                !ChatColor.stripColor(event.getInventory().getName())
                .equalsIgnoreCase("SuperRegion")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR){
            //player.closeInventory();
            return;
        }

        Region region = null;
        SuperRegion superRegion = null;
        String[] nameParts = event.getClickedInventory().getItem(0).getItemMeta().getDisplayName().split(" ");
        if (nameParts.length > 1 && ChatColor.stripColor(event.getInventory().getName()).equals("Region")) {
            region = rm.getRegionByID(Integer.parseInt(nameParts[1]));
        } else {
            superRegion = rm.getSuperRegion(nameParts[0]);
        }

        if (event.getCurrentItem().getType() == Material.TNT) {
            player.closeInventory();
            if (region != null) {
                rm.destroyRegion(region.getLocation());
                rm.removeRegion(region.getLocation());
                return;
            }
            if (superRegion != null) {
                rm.destroySuperRegion(superRegion.getName(), true);
                return;
            }
            return;
        }
        if (event.getCurrentItem().getType() == Material.SIGN) {
            player.closeInventory();
            if (region != null) {
                InfoGUIListener.openInfoInventory(rm.getRegionType(region.getType()), player, "");
                return;
            }
            if (superRegion != null) {
                InfoGUIListener.openInfoInventory(rm.getSuperRegionType(superRegion.getType()), player, "");
                return;
            }
            return;
        }

        String backState = ChatColor.stripColor(event.getInventory().getItem(8).getItemMeta().getLore().get(0));
        if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
            player.closeInventory();
            String[] parts = backState.split(" ");
            if (backState.startsWith("list")) {
                if (parts.length > 1 && rm.getRegionCategories().containsKey(parts[1])) {
                    player.performCommand("to list " + parts[1]);
                } else {
                    player.performCommand("to list");
                }
            } else if (parts.length > 1 && parts[0].equals("who")) {
                player.performCommand("to who " + parts[1]);
            } else if (parts.length > 1 && parts[0].equals("info")) {
                player.performCommand("to info " + parts[1]);
            } else {
                MainMenuGUIListener.openMainMenu(player);
            }
            return;
        }
        String regionTypeName = "";
        regionTypeName = ChatColor.stripColor(event.getInventory().getItem(0).getItemMeta().getDisplayName()).toLowerCase();
        
        RegionType rt = rm.getRegionType(regionTypeName);

        if (event.getClickedInventory() != null && event.getClickedInventory().getItem(1) != null &&
                event.getClickedInventory().getItem(1).equals(event.getCurrentItem())) {
            player.closeInventory();
            RegionType type = rm.getRegionType(event.getCurrentItem().getItemMeta().getDisplayName().toLowerCase());
//            RegionInfoGUIListener.openInfoInventory(type, player, "info " + regionTypeName);
//            player.performCommand("to info " + event.getCurrentItem().getItemMeta().getDisplayName());
            return;
        }
        if (event.getClickedInventory() != null && event.getClickedInventory().getItem(2) != null &&
                event.getClickedInventory().getItem(2).equals(event.getCurrentItem())) {
            player.closeInventory();
            RegionType type = rm.getRegionType(event.getCurrentItem().getItemMeta().getDisplayName().toLowerCase());
//            RegionInfoGUIListener.openInfoInventory(type, player, "info " + regionTypeName);
//            player.performCommand("to info " + event.getCurrentItem().getItemMeta().getDisplayName());
            return;
        }

        if (rt != null && event.getCurrentItem().getType() == Material.IRON_PICKAXE) {
            player.closeInventory();
            RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<TOItem>>(rt.getRequirements()), player, rt.getName()+ " requirements", backState + " " + regionTypeName);
            return;
        }
        if (rt != null && event.getCurrentItem().getType() == Material.IRON_AXE) {
            player.closeInventory();
            player.performCommand("to create " + rt.getName());
            return;
        }
        if (event.getCurrentItem().getType() == Material.CHEST) {
            player.closeInventory();
            RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<TOItem>>(rt.getReagents()), player, rt.getName() + " reagents", backState + " " + regionTypeName);
            return;
        }
        if (event.getCurrentItem().getType() == Material.HOPPER) {
            player.closeInventory();
            RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<TOItem>>(rt.getUpkeep()), player, rt.getName() + " input", backState + " " + regionTypeName);
            return;
        }
        if (event.getCurrentItem().getType() == Material.DISPENSER) {
            player.closeInventory();
            RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<TOItem>>(rt.getOutput()), player, rt.getName() + " output", backState + " " + regionTypeName);
            return;
        }
        player.closeInventory();
        //player.performCommand("hs info " + ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
    }
}