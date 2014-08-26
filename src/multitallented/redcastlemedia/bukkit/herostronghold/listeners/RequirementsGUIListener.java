/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

/**
 *
 * @author Autumn
 */

import java.text.NumberFormat;
import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.region.HSItem;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
        
public class RequirementsGUIListener implements Listener {

    public static void openRequirementsInventory(RegionType region, Player player) {
        int size = 9;
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "Regions");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
    
        // 012345678
 
        // Shack item
        ItemStack shackIcon = new ItemStack(Material.TRAP_DOOR, 1);
        ItemMeta shackIconMeta = shackIcon.getItemMeta();

        shackIconMeta.setDisplayName(ChatColor.BLUE + "Shack");
        shackIcon.setItemMeta(shackIconMeta);

        // House item
        ItemStack houseIcon = new ItemStack(Material.WOOD_DOOR, 1);
        ItemMeta houseIconMeta = shackIcon.getItemMeta();
        houseIconMeta.setDisplayName(ChatColor.BLUE + "House");
        houseIcon.setItemMeta(houseIconMeta);

        inv.setItem(2, shackIcon);
        inv.setItem(6, houseIcon);
        
        player.openInventory(inv);
    }
 
    @EventHandler
    public void click(InventoryClickEvent event) {
        if (event.getInventory().getSize() == 9) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.TRAP_DOOR) {
                // Teleport to "spawn"
                ((Player) event.getWhoClicked()).sendMessage("spawn");
            }
            if (event.getCurrentItem().getType() == Material.WOOD_DOOR) {
                // Teleport to "pvp zone"
                ((Player) event.getWhoClicked()).sendMessage("pvp");
            }
        }
        event.getWhoClicked().closeInventory();
    }
}
