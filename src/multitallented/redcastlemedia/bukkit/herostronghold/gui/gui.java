/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package multitallented.redcastlemedia.bukkit.herostronghold.gui;

/**
 *
 * @author Autumn
 */
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
        
public class gui extends JavaPlugin implements Listener {
    
    public void onEnable() {
        getServer().getPluginManager().registerEvent(this, this);
    }
    
    private void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_RED 
                + "HeroStronghold Regions");
        
        ItemStack house = new ItemStack(Material.WOODEN_DOOR);
        ItemMeta houseMeta = house.getItemMeta();
        ItemStack cobblequery = new ItemStack(Material.COBBLESTONE);
        ItemMeta cobblequeryMeta = cobblequery.getItemMeta();
        
        houseMeta.setDisplayName(ChatColor.GOLD + "House");
        house.setItemMeta(houseMeta);
        
        cobblequeryMeta.setDisplayName(ChatColor.GOLD + "Cobblestone Query");
        cobblequery.setItemMeta(cobblequeryMeta);
        
        // 012345678   2+6
        inv.setItem(2, house);
        inv.setItem(5, cobblequery);
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName())
                .equalsIgnoreCase("HeroStronghold Regions"))
            return;
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR
                ||!event.getCurrentItem().hasItemMeta()){
            player.closeInventory();
            return;  
        }
        switch(event.getCurrentItem().getType()) {
            case WOODEN_DOOR:
                player.closeInventory();
                player.sendMessage(String.format("%sHeroStronghold Regions: %sHouses", 
                        ChatColor.DARK_RED, ChatColor.GOLD));
                break;
            case COBBLESTONE:
                player.closeInventory();
                player.sendMessage(String.format("%sHeroStronghold Regions: %sCobbleQuery", 
                        ChatColor.DARK_RED, ChatColor.GOLD));
                break; 
            default:
                player.closeInventory();
                break;
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory()
                .addItem(new ItemStack(Material.COMPASS));
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action a = event.getAction();
        ItemStack is = event.getItem();
        
        if(a == Action.PHYSICAL || is == null || is.getType()==Material.AIR)
            return;
        
        if(is.getType() == Material.COMPASS)
            openGUI(event.getPlayer());
    }
            
}
