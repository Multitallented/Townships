package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

/**
 *
 * @author Multitallented
 * @author Phoenix_Frenzy
 */

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

//import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;

public class TownGUIListener implements Listener {
    private static RegionManager rm;
    public TownGUIListener(RegionManager rm) {
        this.rm = rm;
    }
    
    public static void openInventory(Player player) {
        openInventory(player, rm.getSortedSuperRegions());
    }

    public static void openInventory(Player player, ArrayList<SuperRegion> superRegions) {
        int size = 9;
        int actualSize = rm.getSortedSuperRegions().size();
        boolean hasSuperRegions = !rm.getSuperRegionTypes().isEmpty();
        actualSize = hasSuperRegions ? actualSize + 1 : actualSize;
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), size, ChatColor.RED + "Townships Towns");


        int i = 0;
        for (SuperRegion superRegion : rm.getSortedSuperRegions()) {

            //Determine if the player has permissions for any of these

            SuperRegionType srt = rm.getSuperRegionType(superRegion.getType());
            ItemStack is = new ItemStack(srt.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            isMeta.setDisplayName(ChatColor.RESET + superRegion.getName());
            ArrayList<String> lore = new ArrayList<String>();
            for (String ownerName : superRegion.getOwners()) {
                lore.add(ChatColor.GOLD + ownerName);
            }
            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }

        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !ChatColor.stripColor(event.getInventory().getName()).equals("Townships Towns")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR
                ||!event.getCurrentItem().hasItemMeta()){
            //player.closeInventory();
            return;
        }

        /*if (event.getCurrentItem().hasItemMeta() && ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("Back to Categories")) {
            player.closeInventory();
            player.performCommand("to list");
            return;
        }*/

        SuperRegion superRegion = rm.getSuperRegion(ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
        if (superRegion == null) {
            return;
        }
        player.closeInventory();
        player.performCommand("to who " + superRegion.getName());
    }
}