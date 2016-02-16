package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

/**
 *
 * @author Multitallented
 */

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import net.minecraft.util.org.apache.commons.lang3.text.WordUtils;

public class PlayerGUIListener implements Listener {
    private static RegionManager rm;
    public PlayerGUIListener(RegionManager rm) {
        this.rm = rm;
    }
    
    public static void openPlayerList(Player player, ArrayList<String> players, String command) {
        int size = 9;
        int actualSize = players.size();
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), size, ChatColor.RED + "Townships Players");
        
        
        int i = 0;
        for (String currentPlayer : players) {
            ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta isMeta = (SkullMeta) is.getItemMeta();
            isMeta.setDisplayName(currentPlayer);
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(command);
            isMeta.setLore(lore);
            isMeta.setOwner(currentPlayer);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase("Townships Players")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR
                ||!event.getCurrentItem().hasItemMeta()){
            return;
        }

        List<String> lore = event.getCurrentItem().getItemMeta().getLore();
        if (lore.isEmpty() || lore.get(0).equals("")) {
            return;
        }
        String command = lore.get(0);
        player.closeInventory();
        player.performCommand(command.replace("{name}", event.getCurrentItem().getItemMeta().getDisplayName()));
    }
}