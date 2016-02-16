package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

/**
 *
 * @author Phoenix_Frenzy
 * @author Multitallented
 */
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import multitallented.redcastlemedia.bukkit.townships.region.TOItem;
import net.milkbowl.vault.item.Items;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class MainMenuGUIListener implements Listener {
    private final RegionManager rm;
    public MainMenuGUIListener(RegionManager rm) {
        this.rm = rm;
    }
    
    public static void openMainMenu(Player player) {
        int size = 18;
        //Inventory inv = Bukkit.createInventory(null, size, ChatColor.RED + "Townships Menu");
        Inventory inv = Bukkit.createInventory(new MenuHolder(Bukkit.createInventory(null, size)), size, ChatColor.RED + "To Menu");
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);

        //Buy Blueprints
        {
            ItemStack itemStack = new ItemStack(Material.MAP, 1);
            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.BLUE + "Buy Blueprints");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "You must first purchase blueprints");
            lore.add(ChatColor.WHITE + "before you can build regions.");
            im.setLore(lore);
            itemStack.setItemMeta(im);
            inv.setItem(9, itemStack);
        }


        //Build Region
        {
            ItemStack itemStack = new ItemStack(Material.IRON_AXE, 1);
            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + "Build Region");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "Build any region that you have");
            lore.add(ChatColor.WHITE + "the blueprints for.");
            im.setLore(lore);
            itemStack.setItemMeta(im);
            inv.setItem(10, itemStack);
        }

        //Inventory
        {
            ItemStack itemStack = new ItemStack(Material.CHEST, 1);
            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.GREEN + "Inventory");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "See all regions that you");
            lore.add(ChatColor.WHITE + "currently have built.");
            im.setLore(lore);
            itemStack.setItemMeta(im);
            inv.setItem(11, itemStack);
        }

        //Profile
        {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta im = (SkullMeta) itemStack.getItemMeta();
            im.setDisplayName(ChatColor.LIGHT_PURPLE + "Profile");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "See your current status.");

            if (Townships.econ != null) {
                double balance = Townships.econ.bankBalance(player.getName()).balance;
                if (balance >= 0) {
                    lore.add(ChatColor.RESET + "" + ChatColor.GREEN + "Money: " + formatter.format(balance));
                } else if (balance < 0) {
                    lore.add(ChatColor.RESET + "" + ChatColor.RED + "Money: " + formatter.format(balance));
                }
            }
            lore.add(ChatColor.LIGHT_PURPLE + "Location: " + Math.round(player.getLocation().getX()) + "x " + Math.round(player.getLocation().getY()) + "y " + Math.round(player.getLocation().getZ()) + "z");

            im.setLore(lore);
            im.setOwner(player.getName().toLowerCase());
            itemStack.setItemMeta(im);
            inv.setItem(0, itemStack);
        }

        //Lookup Player
        {
            ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.RED + "Players");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "See the status of a player.");
            im.setLore(lore);
            itemStack.setItemMeta(im);
            inv.setItem(12, itemStack);
        }

        //Lookup Town
        {
            ItemStack itemStack = new ItemStack(Material.GLOWSTONE, 1);
            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(ChatColor.RED + "Players");
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.WHITE + "See the status of a town.");
            im.setLore(lore);
            itemStack.setItemMeta(im);
            inv.setItem(13, itemStack);
        }

        ///////////////////////////////////////////
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getInventory().getName())
                .equalsIgnoreCase("To Menu")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        
        if(event.getCurrentItem()==null 
                || event.getCurrentItem().getType()==Material.AIR){
            //player.closeInventory();
            return;
        }
        
        /*String backState = ChatColor.stripColor(event.getInventory().getItem(8).getItemMeta().getLore().get(0));
        if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
            player.closeInventory();
            String[] parts = backState.split(" ");
            if (backState.startsWith("list")) {
                if (parts.length > 1 && rm.getRegionCategories().containsKey(parts[1])) {
                    player.performCommand("to list " + parts[1]);
                } else {
                    player.performCommand("to list");
                }
            } else if (parts.length > 1 && parts[0].equals("who")) {
                player.performCommand("to who " + parts[1]);
            }
            return;
        }*/

        
        if (event.getCurrentItem().getType() == Material.IRON_AXE) {
            player.closeInventory();
            GUIListener.openCategoryInventory(player);
            //RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<TOItem>>(rt.getRequirements()), player, rt.getName()+ " requirements", backState + " " + regionTypeName);
            return;
        }
        if (event.getCurrentItem().getType() == Material.MAP) {
            player.closeInventory();
            ShopGUIListener.openCategoryShop(player, false);
            //player.performCommand("to create " + rt.getName());
            return;
        }
        if (event.getCurrentItem().getType() == Material.CHEST) {
            player.closeInventory();
            RegionGUIListener.openCategoryInventory(player);
            //RequirementsGUIListener.openRequirementsInventory(new ArrayList<ArrayList<TOItem>>(rt.getReagents()), player, rt.getName() + " reagents", backState + " " + regionTypeName);
            return;
        }
        if (event.getCurrentItem().getType() == Material.SKULL_ITEM) {
            if (ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).equals("Players")) {
                player.closeInventory();
                ArrayList<String> playerList = new ArrayList<String>();
                for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
                    playerList.add(currentPlayer.getName());
                }
                PlayerGUIListener.openPlayerList(player, playerList, "to who {name}");
                return;
            } else {
                player.closeInventory();
                player.performCommand("to who " + player.getName());
                //TODO open profile menu instead
                return;
            }
        }
        if (event.getCurrentItem().getType() == Material.GLOWSTONE) {
            player.closeInventory();
            TownGUIListener.openInventory(player);
            return;
        }
        player.closeInventory();
    }
}