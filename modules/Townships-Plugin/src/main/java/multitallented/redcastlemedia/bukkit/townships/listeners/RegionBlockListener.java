package multitallented.redcastlemedia.bukkit.townships.listeners;

import java.text.NumberFormat;
import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionCondition;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import multitallented.redcastlemedia.bukkit.townships.region.TOItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionBlockListener implements Listener {
    private final RegionManager regionManager;
    private final Townships plugin;
    public RegionBlockListener(Townships plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        Location currentLoc = null;
        boolean delete = false;
        boolean activeSRDetected = false;
        for (SuperRegion sr : regionManager.getContainingSuperRegions(loc)) {
            SuperRegionType currentRegionType = regionManager.getSuperRegionType(sr.getType());
            Player player = event.getPlayer();
            boolean nullPlayer = player == null;
            boolean member = false;
            if (!nullPlayer) {
                member = (sr.hasOwner(player.getName()) || sr.hasMember(player.getName()));
            }
            boolean reqs = regionManager.hasAllRequiredRegions(sr);
            boolean hasPower = sr.getPower() > 0;
            boolean hasMoney = sr.getBalance() > 0;
            boolean hasGrace = regionManager.refreshGracePeriod(sr, hasMoney && reqs);
            if ((nullPlayer || !member)
                    && currentRegionType.hasEffect("deny_block_break") &&
                    hasPower && ((hasMoney && reqs) || hasGrace)) {
                event.setCancelled(true);
                if (player != null) {
                    player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                }
                return;
            }
            if ((nullPlayer || !member)
                    && currentRegionType.hasEffect("deny_block_break_no_reagent")) {
                event.setCancelled(true);
                if (player != null) {
                    player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                }
                return;
            }

            activeSRDetected = activeSRDetected || (hasPower && ((reqs && hasMoney) || hasGrace));
        }
        for (Region r : regionManager.getContainingBuildRegions(loc)) {
            try {
                //Check chest
                currentLoc = r.getLocation();
                if (currentLoc.getBlock().equals(loc.getBlock())) {
                    Region currentRegion = regionManager.getRegion(currentLoc);
                    RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
                    Player player = event.getPlayer();
                    Effect effect = new Effect(plugin);
                    if ((player == null || (!currentRegion.isOwner(player.getName())))
                            && effect.regionHasEffect(currentRegionType.getEffects(), "deny_block_break") != 0 && effect.hasReagents(currentLoc)) {
                        event.setCancelled(true);
                        if (player != null) {
                            player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                        }
                        return;
                    }
                    if ((player == null || !currentRegion.isOwner(player.getName()))
                            && effect.regionHasEffect(currentRegionType.getEffects(), "deny_block_break_no_reagent") != 0) {
                        event.setCancelled(true);
                        if (player != null) {
                            player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                        }
                        return;
                    }
                    if (activeSRDetected && effect.regionHasEffect(currentRegionType.getEffects(), "power_deny_block_break") != 0 &&
                            (player == null || !currentRegion.isOwner(player.getName()))) {
                        event.setCancelled(true);
                        if (player != null) {
                            player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                        }
                        return;
                    }

                    for (SuperRegion sr : regionManager.getContainingSuperRegions(currentLoc)) {
                        if (!sr.hasOwner(player.getName()) && sr.hasMember(player.getName())) {
                            player.sendMessage(ChatColor.RED + "[Townships] You can't destroy regions in towns you don't own.");
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (currentRegionType != null && player != null && (Townships.getConfigManager().getSalvage() > 0 || currentRegionType.getSalvage() != 0) && r.isPrimaryOwner(player.getName())) {
                        NumberFormat formatter = NumberFormat.getCurrencyInstance();
                        double salvageValue = Townships.getConfigManager().getSalvage() * currentRegionType.getMoneyRequirement();
                        salvageValue = currentRegionType.getSalvage() != 0 ? currentRegionType.getSalvage() : salvageValue;
                        player.sendMessage(ChatColor.GREEN + "[Townships] You salvaged region " + r.getType() + " " + r.getID() + " for " + formatter.format(salvageValue));
                        Townships.econ.depositPlayer(player, salvageValue);
                    }

                    regionManager.destroyRegion(currentLoc);
                    delete=true;
                    break;
                }

                //Check everything else
                Region currentRegion = regionManager.getRegion(currentLoc);
                RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
                Player player = event.getPlayer();
                Effect effect = new Effect(plugin);
                if ((player == null || (!currentRegion.isOwner(player.getName()) && !effect.isMemberOfRegion(player, currentLoc)))
                        && effect.regionHasEffect(currentRegionType.getEffects(), "deny_block_break") != 0 && effect.hasReagents(currentLoc)) {
                    event.setCancelled(true);
                    if (player != null) {
                        player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                    }
                    return;
                }
                if ((player == null || (!currentRegion.isOwner(player.getName()) && !effect.isMemberOfRegion(player, currentLoc)))
                        && effect.regionHasEffect(currentRegionType.getEffects(), "deny_block_break_no_reagent") != 0) {
                    event.setCancelled(true);
                    if (player != null) {
                        player.sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
                    }
                    return;
                }
                TOItem toItem = new TOItem(loc.getBlock().getType(), loc.getBlock().getTypeId(), 1, loc.getBlock().getState().getData().toItemStack().getDurability());
                ArrayList<ArrayList<TOItem>> reqMap = new ArrayList<ArrayList<TOItem>>();

                for (ArrayList<TOItem> currentStack : currentRegionType.getRequirements()) {
                    ArrayList<TOItem> tempMap = new ArrayList<TOItem>();

                    for (TOItem hsItem : currentStack) {
                        TOItem clone = hsItem.clone();
                        if (toItem != null && toItem.getMat() == clone.getMat() &&
                                (clone.isWildDamage() || clone.damageMatches((short) toItem.getDamage()))) {
                            clone.setQty(clone.getQty() + 1);
                            toItem = null;
                        }
                        tempMap.add(clone);
                    }
                    reqMap.add(tempMap);
                }

                double lx = Math.floor(currentLoc.getX()) + 0.4;
                double ly = Math.floor(currentLoc.getY()) + 0.4;
                double lz = Math.floor(currentLoc.getZ()) + 0.4;
                double buildRadius = currentRegionType.getRawBuildRadius();

                int x = (int) Math.round(lx - buildRadius);
                int y = (int) Math.round(ly - buildRadius);
                y = y < 0 ? 0 : y;
                int z = (int) Math.round(lz - buildRadius);
                int xMax = (int) Math.round(lx + buildRadius);
                int yMax = (int) Math.round(ly + buildRadius);
                yMax = yMax > currentLoc.getWorld().getMaxHeight() - 1 ? currentLoc.getWorld().getMaxHeight() - 1 : yMax;
                int zMax = (int) Math.round(lz + buildRadius);
                World world = currentLoc.getWorld();

                for (int i = x; i < xMax; i++) {
                    for (int j = y; j < yMax; j++) {
                        for (int k = z; k < zMax; k++) {
                            ItemStack is = world.getBlockAt(i,j,k).getState().getData().toItemStack();
                            
                            int p = 0;
                            boolean destroyIndex = false;
                            outer1: for (ArrayList<TOItem> tempMap : reqMap) {
                                for (TOItem item : tempMap) {
                                    if (item.getMat() == is.getType() && (item.isWildDamage() || item.damageMatches(is.getDurability()))) {
                                        if (item.getQty() < 2) {
                                            destroyIndex = true;
                                        } else {
                                            item.setQty(item.getQty() - 1);
                                        }
                                        break outer1;
                                    }
                                }
                                p++;
                            }
                            if (destroyIndex) {
                                reqMap.remove(p);

                                if (reqMap.isEmpty()) {
                                    delete = false;
                                }
                            }
                        }
                    }
                }

                delete = !reqMap.isEmpty();
                if (delete && (effect.isMemberOfRegion(player, r.getLocation()) || r.isOwner(player.getName()))) {
                    player.sendMessage(ChatColor.GRAY + "[Townships] Breaking this, would destroy your " + r.getType());
                    player.sendMessage(ChatColor.RED + "Missing requirements:");
                    for (String s : Util.hasCreationRequirements(r.getLocation(), currentRegionType, regionManager)) {
                        player.sendMessage(ChatColor.RED + s);
                    }
                    event.setCancelled(true);
                    return;
                }
                if (delete) {
                    regionManager.destroyRegion(currentLoc);
                }

            } catch (NullPointerException npe) {
                plugin.warning("Region " + r.getID() + " corrupted.");
            }
        }
        
        if (delete && currentLoc != null) {
            regionManager.removeRegion(currentLoc);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
        conditions.add(new RegionCondition("deny_block_build", true, 0));
        conditions.add(new RegionCondition("deny_block_build_no_reagent", false, 0));
        if (event.isCancelled() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), conditions)) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
    }
    
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.isCancelled() || !event.getBlock().getType().equals(Material.CAKE_BLOCK))
            return;
        
        ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
        conditions.add(new RegionCondition("deny_block_break", true, 0));
        conditions.add(new RegionCondition("deny_block_break_no_reagent", false, 0));
        if (regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), conditions)) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
            event.setCancelled(true);
            return;
        }
    }
        
    
    
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.isCancelled() || event.getBlock().getTypeId() == 0 ||
                regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyliquid", false)) {
            return;
        }
        
        if (regionManager.shouldTakeAction(event.getToBlock().getLocation(), null, 0, "denyliquid", false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
        conditions.add(new RegionCondition("deny_fire", true, 1));
        conditions.add(new RegionCondition("deny_fire_no_reagent", false, 1));
        IgniteCause cause = event.getCause();
        if (((cause == IgniteCause.LIGHTNING || cause == IgniteCause.LAVA || cause == IgniteCause.SPREAD) &&
                regionManager.shouldTakeAction(event.getBlock().getLocation(), null, conditions)) ||
                (cause == IgniteCause.FLINT_AND_STEEL && regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), conditions))) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        ArrayList<RegionCondition> conditions = new ArrayList<RegionCondition>();
        conditions.add(new RegionCondition("deny_fire", true, 1));
        conditions.add(new RegionCondition("deny_fire_no_reagent", false, 1));
        if (event.isCancelled() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), null, conditions)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if ((event.isCancelled() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), 0, "deny_block_break", true)) &&
                (event.isCancelled() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), 0, "deny_block_break_no_reagent", false))) {
            return;
        }
        event.setCancelled(true);
        if (event.getPlayer() != null) {
            event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] This region is protected");
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "deny_block_build", true) ||
                regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "deny_block_build_no_reagent", false)) {
            return;
        }
        for (Block b : event.getBlocks()) {
            if ((regionManager.shouldTakeAction(b.getLocation(), null, 0, "deny_block_build", true)) ||
                    (regionManager.shouldTakeAction(b.getLocation(), null, 0, "deny_block_build_no_reagent", false))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /*@EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled() || !event.isSticky() ||
                regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak", true) ||
                regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreaknoreagent", false)) {
            return;
        }
        if (regionManager.shouldTakeAction(event.getRetractLocation(), null, 0, "denyblockbreak", true) ||
                regionManager.shouldTakeAction(event.getRetractLocation(), null, 0, "denyblockbreaknoreagent", false)) {
            event.setCancelled(true);
        }
    }*/
}
