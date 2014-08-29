/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package multitallented.redcastlemedia.bukkit.herostronghold.listeners.guis;

/**
 *
 * @author Autumn
 */

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.Util;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
        
public class RequirementsGUIListener implements Listener {

    public static void openRequirementsInventory(ArrayList<ArrayList<HSItem>> items, Player player, String title) {
        int index = 0;
        HashMap<Integer, HSItem> proxyInv = new HashMap<Integer, HSItem>();
        HashMap<Integer, ArrayList<HSItem>> cycleItems = new HashMap<Integer, ArrayList<HSItem>>();
        
        for (ArrayList<HSItem> subItems : items) {
            if (subItems.size() == 1) {
                HSItem item = subItems.get(0);
                int qty = item.getQty();
                
                ItemStack is = new ItemStack(item.getMat());
                int maxStack = is.getMaxStackSize();
                
                while (qty > maxStack) {
                    HSItem tempItem = item.clone();
                    tempItem.setQty(maxStack);
                    proxyInv.put(index, tempItem);
                    index++;
                    qty -= maxStack;
                }
                
                HSItem tempItem = item.clone();
                tempItem.setQty(qty);
                proxyInv.put(index, tempItem);
                index++;
            } else {
                int baseIndex = index;
                int baseIndexOffset = 0;
                
                HSItem item = subItems.get(0);
                int qty = item.getQty();

                ItemStack is = new ItemStack(item.getMat());
                int maxStack = is.getMaxStackSize();
                int orMax = 0;
                while (qty > maxStack) {
                    HSItem tempItem = item.clone();
                    tempItem.setQty(maxStack);
                    proxyInv.put(index, tempItem);
                    ArrayList<HSItem> tempListItems = new ArrayList<HSItem>();
                    tempListItems.add(tempItem);
                    cycleItems.put(baseIndex + baseIndexOffset, tempListItems);
                    index++;
                    orMax++;
                    baseIndexOffset++;
                    qty -= maxStack;
                }

                HSItem tempItem = item.clone();
                tempItem.setQty(qty);
                proxyInv.put(index, tempItem);
                index++;
                orMax++;
                
                for (HSItem currItem : subItems) {
                    if (currItem.equals(item)) {
                        continue;
                    }
                    baseIndexOffset = 0;
                    
                    ItemStack cis = new ItemStack(currItem.getMat());
                    int cqty = currItem.getQty();
                    int cMaxStack = cis.getMaxStackSize();
                    int cMax = 0;
                    while (cqty > cMaxStack) {
                        cMax++;
                        cqty -= cMaxStack;
                        
                        if (!cycleItems.containsKey(baseIndex + baseIndexOffset)) {
                            cycleItems.put(baseIndex + baseIndexOffset, new ArrayList<HSItem>());
                        }
                        HSItem clone = currItem.clone();
                        clone.setQty(cMaxStack);
                        
                        ArrayList<HSItem> subCycleList = cycleItems.get(baseIndex + baseIndexOffset);
                        while (subCycleList.size() < baseIndexOffset) {
                            subCycleList.add(new HSItem(Material.AIR, 0, 0));
                        }
                        subCycleList.add(baseIndexOffset, clone);
                        
                        baseIndexOffset++;
                    }
                    cMax++;
                    if (!cycleItems.containsKey(baseIndex + baseIndexOffset)) {
                        cycleItems.put(baseIndex + baseIndexOffset, new ArrayList<HSItem>());
                    }
                    
                    ArrayList<HSItem> subCycleList = cycleItems.get(baseIndex + baseIndexOffset);
                    while (subCycleList.size() < baseIndexOffset) {
                        subCycleList.add(new HSItem(Material.AIR, 0, 0));
                    }
                    
                    HSItem clone = currItem.clone();
                    clone.setQty(cqty);
                    subCycleList.add(baseIndexOffset, clone);
                    
                    if (cMax > orMax) {
                        index += cMax - orMax;
                        orMax = cMax;
                    }
                }
            }
        }
        
        int size = 9;
        if (index > size) {
            size = index + 9 - (index % 9);
            if (index % 9 == 0) {
                size -= 9;
            }
        }
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.GREEN + title);
        
        for (Integer pIndex : proxyInv.keySet()) {
            HSItem nextItem = proxyInv.get(pIndex);
            ItemStack is;
            if (nextItem.isWildDamage()) {
                is = new ItemStack(nextItem.getMat(), nextItem.getQty());
                ItemMeta isMeta = is.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                lore.add("Any type acceptable");
                isMeta.setLore(lore);
                is.setItemMeta(isMeta);
            } else {
                is = new ItemStack(nextItem.getMat(), nextItem.getQty(), (short) nextItem.getDamage());
            }
            inv.setItem(pIndex, is);
        }
        
        player.openInventory(inv);
        
        for (Integer cycleIndex : cycleItems.keySet()) {
            Util.cycles.addItemCycle(player, inv, cycleIndex, cycleItems.get(cycleIndex));
        }
    }
    
    @EventHandler
    public void closeInventory(InventoryCloseEvent event) {
        Bukkit.getPlayer("Multitallented").sendMessage("begin closing");
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        if (Util.cycles.containsItemCycle(player)) {
            Bukkit.getPlayer("Multitallented").sendMessage("closing");
            Util.cycles.removeCycleItem(player);
        }
    }
 
    @EventHandler
    public void click(InventoryClickEvent event) {
        String name = ChatColor.stripColor(event.getInventory().getName());
        if (!name.equals("Requirements") &&
                !name.equals("Reagents") &&
                !name.equals("Upkeep") &&
                !name.equals("Output")) {
            return;
        }
        /*if (event.getInventory().getSize() == 9) {
            event.setCancelled(true);
            if (event.getCurrentItem().getType() == Material.TRAP_DOOR) {
                // Teleport to "spawn"
                ((Player) event.getWhoClicked()).sendMessage("spawn");
            }
            if (event.getCurrentItem().getType() == Material.WOOD_DOOR) {
                // Teleport to "pvp zone"
                ((Player) event.getWhoClicked()).sendMessage("pvp");
            }
        }*/
        //Util.cycles.removeCycleItem(event.getInventory());
        //event.getWhoClicked().closeInventory();
        event.setCancelled(true);
    }
}
