package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

/**
 *
 * @author Autumn
 */
import java.text.NumberFormat;
import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.region.HSItem;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegionType;
import net.milkbowl.vault.item.Items;
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
        ItemMeta requireMeta = requireStack.getItemMeta();
        requireMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Requirements:");
        
        if (region.getRequirements().size() > 0) {
                lore.add("Requirements");
                for (ArrayList<HSItem> items : region.getRequirements()) {
                    String requirements = "";
                    for (HSItem item : items) {
                        if (!requirements.equals("")) {
                            requirements += " or ";
                        }
                        String itemName = "";
                        if (item.isWildDamage()) {
                            itemName = item.getMat().name().replace("_", " ").toLowerCase();
                        } else {
                            ItemStack ist = new ItemStack(item.getMat(), 1, (short) item.getDamage());
                            itemName = Items.itemByStack(ist).getName();
                        }
                        requirements += item.getQty() + " " + itemName;
                    }
                    lore.add(requirements);
        
        inv.setItem(10, requireStack);
        
        ItemStack reagentStack = new ItemStack(Material.HOPPER);
        
        if (region.getReagents().size() > 0) {
                lore.add("Reagents");
                for (ArrayList<HSItem> items : region.getReagents()) {
                    String reagents = "";
                    for (HSItem item : items) {
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
                    lore.add(reagents);
        
        inv.setItem(11, reagentStack);
        
        ItemStack outputStack = new ItemStack(Material.DISPENSER);
        ItemMeta outputMeta = outputStack.getItemMeta();
        outputMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Output:");
        inv.setItem(12, outputStack);
        
        ItemStack effectsStack = new ItemStack(Material.POTION, 1, (short) 1);
        ItemMeta effectMeta = effectsStack.getItemMeta();
        effectMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Effects:");
        inv.setItem(13, effectsStack);
        
        ItemStack backStack = new ItemStack(2259);
        ItemMeta backMeta = backStack.getItemMeta();
        backMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Press to go BACK");
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