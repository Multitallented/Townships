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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
        
public class RequirementsGUIListener implements Listener {

    public void openListInventory(RegionType region, Player player) {
        int size = 9;
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + "Regions");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        
        
        player.openInventory(inv);
    
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
    }
// Method for listening to Interact Event
@EventHandler
public void interact(PlayerInteractEvent event) {
   Player p = event.getPlayer();
   if (p.getItemInHand().getType() == Material.FURNACE) { // For example purposes...
           event.setCancelled(true);
           p.openInventory(inv);
   }
}
 
@EventHandler
public void click(InventoryClickEvent event) {
if (event.getInventory().getSize() == 9) {
event.setCancelled(true);
if (event.getCurrentItem().getType() == Material.BLAZE_ROD) {
// Teleport to "spawn"
}
if (event.getCurrentItem().getType() == Material.IRON_CHESTPLATE) {
// Teleport to "pvp zone"
}
else {
return;
}
}
}
}
