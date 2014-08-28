package multitallented.redcastlemedia.bukkit.herostronghold.listeners.guis;

/**
 *
 * @author Autumn
 */
import java.text.NumberFormat;
import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.Util;
import multitallented.redcastlemedia.bukkit.herostronghold.listeners.guis.RequirementsGUIListener;
import multitallented.redcastlemedia.bukkit.herostronghold.region.HSItem;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
        
public class InfoGUIListener implements Listener {
    private final RegionManager rm;
    public InfoGUIListener(RegionManager rm) {
        this.rm = rm;
    }
    
    public static void openInfoInventory(RegionType region, Player player, String back) {
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
        
        ItemStack requireStack = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta requireMeta = requireStack.getItemMeta();
        requireMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Requirements:");
        lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Build a structure with these blocks.");
        requireMeta.setLore(lore);
        requireStack.setItemMeta(requireMeta);
        inv.setItem(10, Util.removeAttributes(requireStack));
        
        ItemStack reagentStack = new ItemStack(Material.CHEST);
        ItemMeta reagentMeta = reagentStack.getItemMeta();
        reagentMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Reagents:");
        lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Items required to run the region.");
        reagentMeta.setLore(lore);
        reagentStack.setItemMeta(reagentMeta);
        inv.setItem(11, reagentStack);
        
        ItemStack upkeepStack = new ItemStack(Material.HOPPER);
        ItemMeta upkeepMeta = upkeepStack.getItemMeta();
        upkeepMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Upkeep:");
        lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Items consumed by the region.");
        upkeepMeta.setLore(lore);
        upkeepStack.setItemMeta(upkeepMeta);
        inv.setItem(12, upkeepStack);
        
        ItemStack outputStack = new ItemStack(Material.DISPENSER);
        ItemMeta outputMeta = outputStack.getItemMeta();
        outputMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Output:");
        lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "Items produced by the region.");
        outputMeta.setLore(lore);
        outputStack.setItemMeta(outputMeta);
        inv.setItem(13, outputStack);
        
        ItemStack effectsStack = new ItemStack(Material.POTION, 1, (short) 1);
        ItemMeta effectMeta = effectsStack.getItemMeta();
        effectMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Effects:");
        effectMeta.setLore(region.getEffects());
        effectsStack.setItemMeta(effectMeta);
        inv.setItem(14, Util.removeAttributes(effectsStack));
        
        ItemStack backStack = new ItemStack(Material.RECORD_4);
        ItemMeta backMeta = backStack.getItemMeta();
        backMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.GOLD + "Press to go BACK");
        lore = new ArrayList<String>();
        if (back == null) {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + "Exit");
        } else {
            lore.add(ChatColor.RESET + "" + ChatColor.RED + back);
        }
        backStack.setItemMeta(backMeta);
        inv.setItem(8, backStack);
        
        player.openInventory(inv);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName())
                .equalsIgnoreCase("Region Info")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR){
            //player.closeInventory();
            return;
        }
        if (event.getCurrentItem().getType() == Material.RECORD_4) {
            player.closeInventory();
            String backState = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getLore().get(0));
            if (backState.equals("hs list")) {
                player.performCommand("hs list");
            } else if (backState.split(" ").length > 1 && backState.split(" ")[0].equals("who")) {
                player.performCommand("hs who " + backState.split(" ")[1]);
            }
            return;
        }
        
        RegionType rt = rm.getRegionType(player.getOpenInventory().getItem(0).getItemMeta().getDisplayName());
        
        if (event.getCurrentItem().getType() == Material.IRON_PICKAXE) {
            player.closeInventory();
            RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<HSItem>>(rt.getRequirements()), player, "Building Requirements");
            return;
        }
        player.closeInventory();
        //player.performCommand("hs info " + ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()));
    }
}