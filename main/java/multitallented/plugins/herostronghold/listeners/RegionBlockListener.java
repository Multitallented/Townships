package main.java.multitallented.plugins.herostronghold.listeners;

import java.util.Iterator;
import java.util.Set;
import main.java.multitallented.plugins.herostronghold.effect.Effect;
import main.java.multitallented.plugins.herostronghold.HeroStronghold;
import main.java.multitallented.plugins.herostronghold.region.Region;
import main.java.multitallented.plugins.herostronghold.region.RegionManager;
import main.java.multitallented.plugins.herostronghold.region.RegionType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionBlockListener extends BlockListener {
    private final RegionManager regionManager;
    private final HeroStronghold plugin;
    public RegionBlockListener(HeroStronghold plugin) {
        this.plugin = plugin;
        this.regionManager = plugin.getRegionManager();
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Location loc = event.getBlock().getLocation();
        Location currentLoc = null;
        boolean delete = false;
        
        double x1 = loc.getX();
        for (Region r : regionManager.getSortedRegions()) {
            if (currentLoc.getBlock().equals(loc.getBlock())) {
                regionManager.destroyRegion(currentLoc);
                break;
            }
            
            int radius = regionManager.getRegionType(r.getType()).getRadius();
            Location l = r.getLocation();
            if (l.getX() + radius < x1) {
                return;
            }
            try {
                if (l.getX() - radius > x1 && l.distanceSquared(loc) < radius) {
                    Region currentRegion = regionManager.getRegion(currentLoc);
                    RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
                    Player player = event.getPlayer();
                    Effect effect = new Effect(plugin);
                    if ((player == null || (!currentRegion.isOwner(player.getName()) && !currentRegion.isMember(player.getName())))
                            && effect.regionHasEffect(currentRegionType.getEffects(), "denyblockbreak") != 0 && effect.hasReagents(currentLoc)) {
                        event.setCancelled(true);
                        if (player != null)
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
                    }
                    
                    int amountRequired = 0;
                    int i = 0;
                    for (ItemStack currentStack : currentRegionType.getRequirements()) {
                        if (currentStack.getTypeId() == event.getBlock().getTypeId()) {
                            amountRequired = currentStack.getAmount();
                            break;
                        }
                    }
                    if (amountRequired == 0)
                        return;

                    for (int x= (int) (currentLoc.getX()-radius); x<radius + currentLoc.getX(); x++) {
                        for (int y = currentLoc.getY()- radius > 1 ? (int) (currentLoc.getY() - radius) : 1; y< radius + currentLoc.getY() && y < 128; y++) {
                            for (int z = (int) (currentLoc.getZ() - radius); z<radius + currentLoc.getZ(); z++) {
                                Block tempBlock = currentLoc.getWorld().getBlockAt(x, y, z);
                                if (tempBlock.getTypeId() == event.getBlock().getTypeId()) {
                                    if (i >= amountRequired) {
                                        return;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                        }
                    }
                    regionManager.destroyRegion(currentLoc);
                    delete = true;
                    break;
                }
            } catch (IllegalArgumentException iae) {

            }
        }
        
        /*Set<Location> locations = regionManager.getRegionLocations();
        outer: for (Iterator<Location> iter = locations.iterator(); iter.hasNext();) {
            currentLoc = iter.next();
            try {
                if (currentLoc.getBlock().equals(loc.getBlock())) {
                    regionManager.destroyRegion(currentLoc);
                    break outer;
                }
                Region currentRegion = regionManager.getRegion(currentLoc);
                RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
                int radius = currentRegionType.getRadius();
                if (Math.sqrt(loc.distanceSquared(currentLoc)) < radius) {
                    Player player = event.getPlayer();
                    Effect effect = new Effect(plugin);
                    if ((player == null || (!currentRegion.isOwner(player.getName()) && !currentRegion.isMember(player.getName())))
                            && effect.regionHasEffect(currentRegionType.getEffects(), "denyblockbreak") != 0 && effect.hasReagents(currentLoc)) {
                        event.setCancelled(true);
                        if (player != null)
                            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
                    }
                    
                    int amountRequired = 0;
                    int i = 0;
                    for (ItemStack currentStack : currentRegionType.getRequirements()) {
                        if (currentStack.getTypeId() == event.getBlock().getTypeId()) {
                            amountRequired = currentStack.getAmount();
                            break;
                        }
                    }
                    if (amountRequired == 0)
                        return;

                    for (int x= (int) (currentLoc.getX()-radius); x<radius + currentLoc.getX(); x++) {
                        for (int y = currentLoc.getY()- radius > 1 ? (int) (currentLoc.getY() - radius) : 1; y< radius + currentLoc.getY() && y < 128; y++) {
                            for (int z = (int) (currentLoc.getZ() - radius); z<radius + currentLoc.getZ(); z++) {
                                Block tempBlock = currentLoc.getWorld().getBlockAt(x, y, z);
                                if (tempBlock.getTypeId() == event.getBlock().getTypeId()) {
                                    if (i >= amountRequired) {
                                        return;
                                    } else {
                                        i++;
                                    }
                                }
                            }
                        }
                    }
                    regionManager.destroyRegion(currentLoc);
                    iter.remove();
                    delete = true;
                    break outer;
                }
            } catch (IllegalArgumentException iae) {
            
            }
        }*/
        if (delete && currentLoc != null) {
            regionManager.removeRegion(currentLoc);
        } 
    }
    
    @Override
        public void onBlockPlace(BlockPlaceEvent event) {
            if (event.isCancelled() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), 0, "denyblockplace"))
                return;
            
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        }
    
    @Override
        public void onBlockDamage(BlockDamageEvent event) {
            if (event.isCancelled() || !event.getBlock().getType().equals(Material.CAKE_BLOCK))
                return;
            if (regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), 0, "denyblockbreak")) {
                event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
                event.setCancelled(true);
                return;
            }
        }
        
        @Override
        public void onBlockFromTo(BlockFromToEvent event) {
            if (event.isCancelled() || !regionManager.shouldTakeAction(event.getToBlock().getLocation(), null, 0, "denyblockbreak"))
                return;
            
            Block blockFrom = event.getBlock();

            // Check the fluid block (from) whether it is air.
           if (blockFrom.getTypeId() == 0 || blockFrom.getTypeId() == 8 || blockFrom.getTypeId() == 9) {
                event.setCancelled(true);
                return;
            }
            if (blockFrom.getTypeId() == 10 || blockFrom.getTypeId() == 11) {
                event.setCancelled(true);
                return;
            }
        }
        
        @Override
        public void onBlockIgnite(BlockIgniteEvent event) {
            if (event.isCancelled()) {
                return;
            }

            IgniteCause cause = event.getCause();

            boolean isFireSpread = cause == IgniteCause.SPREAD;
            
            if (cause == IgniteCause.LIGHTNING && regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak")) {
                event.setCancelled(true);
                return;
            }

            if (cause == IgniteCause.LAVA && regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak")) {
                event.setCancelled(true);
                return;
            }

            if (isFireSpread && regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak")) {
                event.setCancelled(true);
                return;
            }

            if (cause == IgniteCause.FLINT_AND_STEEL && regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), 1, "denyblockbreak")) {
                event.setCancelled(true);
                if (event.getPlayer() != null)
                    event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
                return;
            }

        }
        
        @Override
        public void onBlockBurn(BlockBurnEvent event) {
            if (event.isCancelled() && !regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak")) {
                return;
            }
            event.setCancelled(true);
        }
        
        @Override
        public void onSignChange(SignChangeEvent event) {
            if (event.isCancelled() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), event.getPlayer(), 0, "denyblockbreak"))
                return;
            event.setCancelled(true);
            if (event.getPlayer() != null)
                event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        }
        
        @Override
        public void onBlockPistonExtend(BlockPistonExtendEvent event) {
            if (event.isCancelled()) {
                return;
            }
            
            for (Block b : event.getBlocks()) {
                if (regionManager.shouldTakeAction(b.getLocation(), null, 0, "denyblockbreak")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @Override
        public void onBlockPistonRetract(BlockPistonRetractEvent event) {
            if (event.isCancelled() || !event.isSticky() || !regionManager.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak"))
                return;
            
            event.setCancelled(true);
        }
}
