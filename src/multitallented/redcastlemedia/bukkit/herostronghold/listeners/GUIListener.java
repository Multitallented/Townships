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
import net.milkbowl.vault.item.ItemInfo;
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
        
public class GUIListener implements Listener {
    
    public static void openListInventory(ArrayList<RegionType> regions, ArrayList<SuperRegionType> superRegions, Player player) {
        int size = 9;
        int actualSize = regions.size() + superRegions.size();
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        Inventory inv = Bukkit.createInventory(null, size, "HeroStronghold Regions");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        int i = 0;
        for (RegionType r : regions) {
            ItemStack is = new ItemStack(r.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            String displayName = ChatColor.RESET + r.getName();
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "Region");
            if (r.getDescription() != null && !r.getDescription().equals("")) {
                String sendMe = new String(r.getDescription());
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
            if (r.getMoneyRequirement() > 0) {
                lore.add(ChatColor.RESET + "" + ChatColor.BLUE + "Cost: " + formatter.format(r.getMoneyRequirement()));
            }
            if (r.getRequirements().size() > 0) {
                lore.add("Requirements");
                for (ArrayList<HSItem> items : r.getRequirements()) {
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
                }
            }
            isMeta.setDisplayName(displayName);
            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        for (SuperRegionType sr : superRegions) {
            ItemStack is = new ItemStack(sr.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            String displayName = ChatColor.RESET + sr.getName();
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.RESET + "" + ChatColor.GRAY + "Super Region");
            if (sr.getDescription() != null && !sr.getDescription().equals("")) {
                lore.add(ChatColor.GOLD + sr.getDescription());
            }
            if (sr.getChildren().size() > 0) {
                lore.add(ChatColor.GREEN + "Upgrade from:");
                int lineCount = 0;
                String childString = "";
                for (String srt : sr.getChildren()) {
                    if (!childString.equals("")) {
                        childString += ", ";
                    } else {
                        childString += ChatColor.GREEN + "";
                    }
                    lineCount += srt.length();
                    if (lineCount > 50) {
                        lore.add(childString);
                        childString = new String();
                        lineCount = srt.length();
                    }
                    childString += srt;
                }
                lore.add(childString);
            }
            if (sr.getMoneyRequirement() > 0) {
                lore.add("Cost: " + formatter.format(sr.getMoneyRequirement()));
            }
            if (sr.getRequirements().size() > 0) {
                lore.add(ChatColor.BLUE + "Requirements:");
                for (String s : sr.getRequirements().keySet()) {
                    lore.add(ChatColor.BLUE + " " + sr.getRequirement(s) + " " + s);
                }
            }
            isMeta.setDisplayName(displayName);
            isMeta.setLore(lore);
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        
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