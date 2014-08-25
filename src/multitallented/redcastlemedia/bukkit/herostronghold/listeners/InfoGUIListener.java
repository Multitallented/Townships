package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

/**
 *
 * @author Autumn
 */
import java.text.NumberFormat;
import java.util.ArrayList;
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
        
public class InfoGUIListener implements Listener {
    
    public static void openInfoInventory(RegionType region, Player player) {
        int size = 18;
        Inventory inv = Bukkit.createInventory(null, size, "Region Info");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        
        ItemStack iconStack = new ItemStack(region.getIcon());
        ItemMeta iconMeta = iconStack.getItemMeta();
        iconMeta.setDisplayName(region.getName());
        ArrayList<String> lore = new ArrayList<String>();
        if (region.getDescription() != null && !region.getDescription().equals("")) {
            String sendMe = new String(region.getDescription());
            String[] sends = sendMe.split(" ");
            String outString = "";
            for (String s : sends) {
                if (outString.length() > 40) {
                    lore.add(outString);
                    outString = "";
                }
                if (!outString.equals("")) {
                    outString += ChatColor.RESET + "" + ChatColor.GOLD + " ";
                } else {
                    outString += ChatColor.RESET + "" + ChatColor.GOLD;
                }
                outString += s;
            }
            lore.add(outString);
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
        if (region.getMoneyOutput() > 0) {
            lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Per Use: +" + formatter.format(region.getMoneyOutput()));
        } else if (region.getMoneyOutput() < 0) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Per Use: " + formatter.format(region.getMoneyOutput()));
        }
        costMeta.setLore(lore);
        costStack.setItemMeta(costMeta);
        inv.setItem(9, costStack);
        
        ItemStack requireStack = new ItemStack(Material.CHEST);
        inv.setItem(10, requireStack);
        
        ItemStack reagentStack = new ItemStack(Material.HOPPER);
        inv.setItem(11, reagentStack);
        
        ItemStack outputStack = new ItemStack(Material.DISPENSER);
        inv.setItem(12, outputStack);
        
        ItemStack effectsStack = new ItemStack(Material.POTION, 1, (short) 1);
        inv.setItem(13, effectsStack);
        
        ItemStack backStack = new ItemStack(2259);
        inv.setItem(17, backStack);
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName())
                .equalsIgnoreCase("HeroStronghold Regions")) {
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
        player.closeInventory();
        player.performCommand("hs info " + ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
    }
}