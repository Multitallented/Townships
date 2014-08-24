package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

/**
 *
 * @author Autumn
 */
import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionType;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegionType;
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
        
public class GUIListener implements Listener {
    private final HeroStronghold hs;
    
    public GUIListener(HeroStronghold hs) {
        this.hs=hs;
    }
    
    public void openListInventory(ArrayList<RegionType> regions, ArrayList<SuperRegionType> superRegions, Player player) {
        int size = 9;
        int actualSize = regions.size() + superRegions.size();
        if (actualSize > size) {
            size = actualSize + 9 - (actualSize % 9);
            if (actualSize % 9 == 0) {
                size -= 9;
            }
        }
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_RED 
                + "HeroStronghold Regions");
        
        int i = 0;
        for (RegionType r : regions) {
            ItemStack is = new ItemStack(r.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            isMeta.setDisplayName(r.getName());
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        for (SuperRegionType sr : superRegions) {
            ItemStack is = new ItemStack(sr.getIcon());
            ItemMeta isMeta = is.getItemMeta();
            isMeta.setDisplayName(sr.getName());
            is.setItemMeta(isMeta);
            inv.setItem(i, is);
            i++;
        }
        
        player.openInventory(inv);
    }
    
    private void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_RED 
                + "HeroStronghold Regions");
        
        ItemStack house = new ItemStack(Material.WOOD_DOOR);
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
                .equalsIgnoreCase("HeroStronghold Regions")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR
                ||!event.getCurrentItem().hasItemMeta()){
            player.closeInventory();
            return;  
        }
        switch(event.getCurrentItem().getType()) {
            case WOOD_DOOR:
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
        
        if(a == Action.PHYSICAL || is == null || is.getType()==Material.AIR) {
            return;
        }
        
        if(is.getType() == Material.COMPASS) {
            openGUI(event.getPlayer());
        }
    } 
}
