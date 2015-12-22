package multitallented.plugins.townships.effects;

/**
 *
 * @author Sevaron
 * @author Multitallented
 */
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionCondition;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EffectRepair extends Effect
{
  public final Townships aPlugin;

  public EffectRepair(Townships plugin)
  {
    super(plugin);
    this.aPlugin = plugin;
    registerEvent(new RepairListener(this));
  }

    @Override
  public void init(Townships plugin)
  {
    super.init(plugin);
  }
  public class RepairListener implements Listener {
    private final EffectRepair effect;

    public RepairListener(EffectRepair effect) {
      this.effect = effect;
    }

    private Material getRequiredReagent(Material material) {
      switch (material) {
          case WOOD_HOE:
          case WOOD_PICKAXE:
          case WOOD_PLATE:
          case WOOD_SPADE:
          case WOOD_STAIRS:
          case WOOD_SWORD:
        return Material.WOOD;
          case LEATHER_CHESTPLATE:
          case LEATHER_HELMET:
          case LEATHER_LEGGINGS:
          case LEATHER_BOOTS:
        return Material.LEATHER;
          case STONE_AXE:
          case STONE_HOE:
          case STONE_PICKAXE:
          case STONE_SPADE:
          case STONE_SWORD:
        return Material.COBBLESTONE;
          case IRON_PICKAXE:
          case IRON_SPADE:
          case IRON_SWORD:
          case IRON_AXE:
          case IRON_HOE:
          case IRON_CHESTPLATE:
          case IRON_HELMET:
          case IRON_LEGGINGS:
          case IRON_BOOTS:
          case SHEARS:
        return Material.IRON_INGOT;
          case GOLD_PICKAXE:
          case GOLD_SPADE:
          case GOLD_SWORD:
          case GOLD_AXE:
          case GOLD_HOE:
          case GOLD_CHESTPLATE:
          case GOLD_HELMET:
          case GOLD_LEGGINGS:
          case GOLD_BOOTS:
        return Material.GOLD_INGOT;
          case DIAMOND_PICKAXE:
          case DIAMOND_SPADE:
          case DIAMOND_SWORD:
          case DIAMOND_AXE:
          case DIAMOND_HOE:
          case DIAMOND_CHESTPLATE:
          case DIAMOND_HELMET:
          case DIAMOND_LEGGINGS:
          case DIAMOND_BOOTS:
        return Material.DIAMOND;
          case BOW:
          case FISHING_ROD:
        return Material.STRING;
           } return null;
    }

    private int getRepairCost(ItemStack is)
    {
      Material mat = is.getType();
      int amt = 1;
        System.out.println("Durability: " + mat.getMaxDurability() + ":" + is.getDurability());
      switch (mat) {
          case WOOD_HOE:
          case WOOD_PICKAXE:
          case WOOD_PLATE:
          case WOOD_SPADE:
          case WOOD_STAIRS:
          case WOOD_SWORD:
          case BOW:
          case FISHING_ROD:
        amt = (int)(is.getDurability() / mat.getMaxDurability() * 1.0D);
        return amt < 1 ? 1 : amt;
          case SHEARS:
          case GOLD_PICKAXE:
          case GOLD_SPADE:
          case GOLD_SWORD:
          case GOLD_AXE:
          case GOLD_HOE:
          case GOLD_CHESTPLATE:
          case GOLD_HELMET:
          case GOLD_LEGGINGS:
          case GOLD_BOOTS:
        amt = (int)(is.getDurability() / mat.getMaxDurability() * 3.0D);
        return amt < 1 ? 1 : amt;
          case IRON_PICKAXE:
          case IRON_SPADE:
          case IRON_SWORD:
          case IRON_AXE:
          case IRON_HOE:
          case IRON_CHESTPLATE:
          case IRON_HELMET:
          case IRON_LEGGINGS:
          case IRON_BOOTS:
        amt = (int)(is.getDurability() / mat.getMaxDurability() * 4.0D);
        return amt < 1 ? 1 : amt;
          case DIAMOND_PICKAXE:
          case DIAMOND_SPADE:
          case DIAMOND_SWORD:
          case DIAMOND_AXE:
          case DIAMOND_HOE:
          case DIAMOND_CHESTPLATE:
          case DIAMOND_HELMET:
          case DIAMOND_LEGGINGS:
          case DIAMOND_BOOTS:
        amt = (int)(is.getDurability() / mat.getMaxDurability() * 7.0D);
        return amt < 1 ? 1 : amt;
          case STONE_AXE:
          case STONE_HOE:
          case STONE_PICKAXE:
          case STONE_SPADE:
          case STONE_SWORD:
          case LEATHER_CHESTPLATE:
          case LEATHER_HELMET:
          case LEATHER_LEGGINGS:
          case LEATHER_BOOTS:
        amt = (int)(is.getDurability() / mat.getMaxDurability() * 2.0D);
        return amt < 1 ? 1 : amt;
                    } return 0;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.isCancelled()) || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || (!event.getClickedBlock().getType().equals(Material.IRON_BLOCK))) {
            return;
        }

        Region r = null;
        List<Region> potentialRegions = getPlugin().getRegionManager().getContainingBuildRegions(event.getClickedBlock().getLocation());
        if (!potentialRegions.isEmpty()) {
            r = potentialRegions.get(0);
        }

        if (r == null) {
            return;
        }
//
//      if (this.effect.regionHasEffect(this.effect.aPlugin.getRegionManager().getRegionType(r.getType()).getEffects(), "repair") == 0) {
//        return;
//      }

        Player player = event.getPlayer();

        RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType());
        boolean hasEffect = false;
        for (String s : rt.getEffects()) {
            if (s.equals("repair.1")) {
                hasEffect = true;
                break;
            }
        }
        if (!hasEffect) {
            return;
        }

//      if (getPlugin().getRegionManager().shouldTakeAction(event.getClickedBlock().getLocation(), player, new RegionCondition("repair",true, 0))) {
//          return;
//      }

//      if ((!r.isMember(player.getName())) && (!r.isOwner(player.getName()))) {
//        return;
//      }

        ItemStack is = player.getItemInHand();
        if (is.getType() == Material.AIR) {
            player.sendMessage(ChatColor.GRAY + "[Townships] You must hold the item you wish to repair.");
            return;
        }
      Material reagent = getRequiredReagent(is.getType());
      int repairCost = getRepairCost(is);
      if (repairCost == 0) {
        player.sendMessage(ChatColor.GRAY + "[Townships] That item isn't something you can repair here.");
        event.setCancelled(true);
        return;
      }
      if (reagent != null) {

          System.out.println("Cost: " + reagent.name() + ":" + repairCost);
          ItemStack cost = new ItemStack(reagent, repairCost);
          if (!hasReagentCost(player, cost)) {
              player.sendMessage(ChatColor.RED + "[Townships] You don't have enough " + reagent.name().toLowerCase().replace("_", " "));
              return;
          }
          player.getInventory().removeItem(cost);
      }
      if (!effect.upkeep(r.getLocation())) {
          player.sendMessage(ChatColor.RED + "[Townships] Not enough reagents/input materials.");
          return;
      }
      is.setDurability((short) 0);
    }

    protected boolean hasReagentCost(Player player, ItemStack itemStack) {
      int amount = 0;
      for (ItemStack stack : player.getInventory().all(itemStack.getType()).values()) {
        amount += stack.getAmount();
        if (amount >= itemStack.getAmount()) {
          return true;
        }
      }
      return false;
    }
  }
}