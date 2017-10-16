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
import java.util.HashSet;
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

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "View Owners");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to see all members");
            im.setLore(cLore);
            playerItem.setItemMeta(im);
            inv.setItem(12, playerItem);
        }
        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "View Members");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to see all members");
            im.setLore(cLore);
            playerItem.setItemMeta(im);
            inv.setItem(13, playerItem);
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
            inv.setItem(14, playerItem);
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
            inv.setItem(15, playerItem);
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
            inv.setItem(16, playerItem);
        }
        if (region.isOwner(player.getName())) {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Remove Player");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to remove a");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "player from the region");
            im.setLore(cLore);
            im.setOwner("MHF_ArrowDown");
            playerItem.setItemMeta(im);
            inv.setItem(17, playerItem);
        }

        if (region.isOwner(player.getName())) {
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
    
    public static void openInfoInventory(SuperRegion superRegion, Player player, String back) {
        int size = 18;
        //Inventory inv = Bukkit.createInventory(null, size, "Region Info");
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)),
                size, ChatColor.RED + "SuperRegion");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        SuperRegionType superRegionType = rm.getSuperRegionType(superRegion.getType());

        ItemStack iconStack = new ItemStack(superRegionType.getIcon());
        ItemMeta iconMeta = iconStack.getItemMeta();
        iconMeta.setDisplayName(superRegion.getName());
        ArrayList<String> lore = new ArrayList<String>();
        int diameter = (int) (Math.floor(superRegionType.getRawRadius()) * 2 + 1);
        String sizeString = diameter + "x" + diameter;
        lore.add(ChatColor.RESET + "" + ChatColor.RED + "Size: " + sizeString);
        if (superRegionType.getDescription() != null && !superRegionType.getDescription().equals("")) {
            lore.addAll(Util.textWrap(ChatColor.RESET + "" + ChatColor.GOLD, superRegionType.getDescription()));
        }
        iconMeta.setLore(lore);
        iconStack.setItemMeta(iconMeta);
        inv.setItem(0, iconStack);


        if (Townships.getConfigManager().getUsePower()) {
            ItemStack infoStack = new ItemStack(Material.REDSTONE);
            ItemMeta im = infoStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.BLUE + "POWER");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + superRegion.getPower() + "/" + superRegion.getMaxPower());
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Daily Power: " + superRegionType.getDailyPower());
            im.setLore(cLore);
            infoStack.setItemMeta(im);
            inv.setItem(1, infoStack);
        }

        int population = superRegion.getOwners().size() + superRegion.getMembers().size();
        boolean reqs = rm.hasAllRequiredRegions(superRegion);
        boolean hasMoney = superRegion.getBalance() > 0;
        boolean notDisabled = reqs && hasMoney && superRegion.getPower() > 0;
        boolean hasGrace = rm.refreshGracePeriod(superRegion, hasMoney && reqs);
        long gracePeriod = rm.getRemainingGracePeriod(superRegion);
        int housing = 0;
        if (superRegionType.hasEffect("housing")) {
            int housin = 0;
            for (Region r : rm.getContainedRegions(superRegion)) {
                housin += rm.getRegionType(r.getType()).getHousing();
            }
            housing += housin;
        } else {
            housing = -1;
        }

        {
            short damageValue = 14;
            if (notDisabled || hasGrace) {
                damageValue = 5;
            }
            ItemStack workingStack = new ItemStack(Material.WOOL, 1, damageValue);
            ItemMeta im = workingStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();
            if (damageValue == 5) {
                im.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Working");
                cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "This region is running smoothly");

                if (hasGrace) {
                    long hours = (gracePeriod / (1000 * 60 * 60)) % 24;
                    long minutes = (gracePeriod / (1000 * 60)) % 60;
                    long seconds = (gracePeriod / 1000) % 60;
                    cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + "Grace Period: " + ChatColor.RED + hours + "h " + minutes + "m " + seconds + "s");
                }
            } else {
                im.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Disabled");
                if (!hasMoney) {
                    cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "This region needs money");
                }
                if (superRegion.getPower() < 1) {
                    cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "This region needs power");
                }
                if (!reqs) {
                    cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "This region is missing requirements");
                    cLore.add(ChatColor.RESET + "" + ChatColor.RED + rm.hasAllRequiredRegions(superRegion, null));
                }
            }
            im.setLore(cLore);
            workingStack.setItemMeta(im);
            inv.setItem(2, workingStack);
        }
        if (superRegion.hasOwner(player.getName()) || superRegion.hasMember(player.getName())) {
            ItemStack locationStack = new ItemStack(Material.COMPASS);
            ItemMeta im = locationStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();
            ArrayList<SuperRegion> superRegionArrayList = rm.getContainingSuperRegions(superRegion.getLocation());
            if (!superRegionArrayList.isEmpty()) {
                cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + "World: " + superRegion.getLocation().getWorld().getName());
            }
            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD +
                    Math.round(superRegion.getLocation().getX()) + "x " +
                    Math.round(superRegion.getLocation().getY()) + "y " +
                    Math.round(superRegion.getLocation().getZ()) + "z");
            im.setLore(cLore);
            locationStack.setItemMeta(im);
            inv.setItem(3, locationStack);
        }

        HashSet<SuperRegion> warringRegions = rm.getWars(superRegion);
        if (!warringRegions.isEmpty()) {
            ItemStack infoStack = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta im = infoStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.RED + "Wars");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to view all");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "enemy towns.");
            im.setLore(cLore);
            infoStack.setItemMeta(im);
            inv.setItem(4, infoStack);
        }

        {
            ItemStack infoStack = new ItemStack(Material.EMERALD);
            ItemMeta im = infoStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GREEN + "Bank");
            if (superRegion.getBalance() < 0) {
                cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Balance: " + ChatColor.RED + formatter.format(superRegion.getBalance()));
            } else {
                cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Balance: " + ChatColor.GREEN + formatter.format(superRegion.getBalance()));
            }
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Tax: " + formatter.format(superRegion.getTaxes()));
            String pastIncome = ChatColor.RESET + "" + ChatColor.GOLD + "Past Taxes: ";
            for (Double pastTax : superRegion.getTaxRevenue()) {
                pastIncome += formatter.format(pastTax) + ", ";
            }
            if (superRegion.getTaxRevenue().isEmpty()) {
                pastIncome += "No History";
            } else {
                pastIncome = pastIncome.substring(0, pastIncome.length() - 2);
            }
            cLore.add(pastIncome);
            im.setLore(cLore);
            infoStack.setItemMeta(im);
            inv.setItem(9, infoStack);
        }

        {
            ItemStack infoStack = new ItemStack(Material.SIGN);
            ItemMeta im = infoStack.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Region Info");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to view all info");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "on " + superRegionType.getName());
            im.setLore(cLore);
            infoStack.setItemMeta(im);
            inv.setItem(10, infoStack);
        }

        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "View Owners");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to see all members");
            if (housing > -1) {
                cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + "Population: " + population + "/" + housing);
            } else {
                cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + "Population: " + population);
            }
            im.setLore(cLore);
            playerItem.setItemMeta(im);
            inv.setItem(11, playerItem);
        }
        {
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            ArrayList<String> cLore = new ArrayList<String>();

            im.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "View Members");
            cLore.add(ChatColor.RESET + "" + ChatColor.WHITE + "Click to see all members");
            if (housing > -1) {
                cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + "Population: " + population + "/" + housing);
            } else {
                cLore.add(ChatColor.RESET + "" + ChatColor.GOLD + "Population: " + population);
            }
            im.setLore(cLore);
            playerItem.setItemMeta(im);
            inv.setItem(12, playerItem);
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
            inv.setItem(13, playerItem);
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
            inv.setItem(14, playerItem);
        }
        if (superRegion.hasOwner(player.getName())) {
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

        if (superRegion.hasOwner(player.getName())) {
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
        String[] nameParts = ChatColor.stripColor(event.getInventory().getItem(0).getItemMeta().getDisplayName()).split(" ");
        if (nameParts.length > 1 && ChatColor.stripColor(event.getInventory().getName()).equals("Region")) {
            region = rm.getRegionByID(Integer.parseInt(nameParts[1]));
        } else {
            superRegion = rm.getSuperRegion(ChatColor.stripColor(event.getInventory().getItem(0).getItemMeta().getDisplayName()));
        }

        if (event.getCurrentItem().getType() == Material.TNT) {
            player.closeInventory();
            if (region != null && region.isOwner(player.getName())) {
                rm.destroyRegion(region.getLocation());
                rm.removeRegion(region.getLocation());
                return;
            }
            if (superRegion != null && superRegion.hasOwner(player.getName())) {
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
        if (event.getCurrentItem().getType() == Material.DIAMOND_SWORD) {
            player.closeInventory();
            if (superRegion != null) {
                ArrayList<SuperRegion> warringRegions = new ArrayList<SuperRegion>();
                warringRegions.addAll(rm.getWars(superRegion));
                TownGUIListener.openInventory(player, warringRegions);
                return;
            }
            return;
        }
        if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
            SkullMeta skullMeta = (SkullMeta) event.getCurrentItem().getItemMeta();
            String owner = skullMeta.getOwner();
            ArrayList<String> playerList = new ArrayList<String>();
            for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
                playerList.add(currentPlayer.getName());
            }
            if (owner == null) {
                if (ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("View Owners")) {
                    player.closeInventory();
                    if (region != null) {
                        PlayerGUIListener.openPlayerList(player, region.getOwners(), "to who {name}");
                    } else if (superRegion != null) {
                        ArrayList<String> owners = new ArrayList<String>();
                        owners.addAll(superRegion.getOwners());
                        PlayerGUIListener.openPlayerList(player, owners, "to who {name}");
                    }
                } else if (ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("View Members")) {
                    player.closeInventory();
                    if (region != null) {
                        PlayerGUIListener.openPlayerList(player, region.getMembers(), "to who {name}");
                    } else if (superRegion != null) {
                        ArrayList<String> members = new ArrayList<String>();
                        members.addAll(superRegion.getMembers().keySet());
                        PlayerGUIListener.openPlayerList(player, members, "to who {name}");
                    }
                }
            } else {
                if (owner.equals("MHF_ArrowRight")) {
                    player.closeInventory();
                    if (region != null) {
                        PlayerGUIListener.openPlayerList(player, playerList, "to addmemberid {name} " + region.getID());
                    } else if (superRegion != null) {
                        PlayerGUIListener.openPlayerList(player, playerList, "to add {name} " + superRegion.getName());
                    }
                } else if (owner.equals("MHF_ArrowUp")) {
                    player.closeInventory();
                    if (region != null) {
                        PlayerGUIListener.openPlayerList(player, region.getMembers(), "to addownerid {name} " + region.getID());
                    } else if (superRegion != null) {
                        ArrayList<String> memberList = new ArrayList<String>();
                        memberList.addAll(superRegion.getMembers().keySet());
                        PlayerGUIListener.openPlayerList(player, memberList, "to addowner {name} " + superRegion.getName());
                    }
                } else if (owner.equals("MHF_Exclamation")) {
                    player.closeInventory();
                    if (region != null) {
                        PlayerGUIListener.openPlayerList(player, region.getOwners(), "to setownerid {name} " + region.getID());
                    }
                } else if (owner.equals("MHF_ArrowDown")) {
                    player.closeInventory();
                    if (region != null && region.isOwner(player.getName())) {
                        ArrayList<String> memberList = new ArrayList<String>();
                        memberList.addAll(region.getMembers());
                        memberList.addAll(region.getOwners());
                        PlayerGUIListener.openPlayerList(player, memberList, "to removeid {name} " + region.getID());
                    } else if (superRegion != null && superRegion.hasOwner(player.getName())) {
                        ArrayList<String> memberList = new ArrayList<String>();
                        memberList.addAll(superRegion.getMembers().keySet());
                        memberList.addAll(superRegion.getOwners());
                        PlayerGUIListener.openPlayerList(player, memberList, "to remove {name} " + superRegion.getName());
                    }
                }
            }
            return;
        }

        String backState = ChatColor.stripColor(event.getInventory().getItem(8).getItemMeta().getLore().get(0));
        if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
            player.closeInventory();
            String[] parts = backState.split(" ");
            if (backState.startsWith("list")) {
                if (parts.length > 1 && rm.getRegionCategories().containsKey(parts[1])) {
                    RegionGUIListener.openCategoryInventory(player);
                    //RegionGUIListener.openListInventory(null, null, player, parts[1]);
//                    player.performCommand("to list " + parts[1]);
                } else {
                    RegionGUIListener.openCategoryInventory(player);
//                    player.performCommand("to list");
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
//        player.closeInventory();
        //player.performCommand("hs info " + ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
    }
}