package main.java.multitallented.plugins.herostronghold.listeners;

import java.util.Iterator;
import java.util.Set;
import main.java.multitallented.plugins.herostronghold.Region;
import main.java.multitallented.plugins.herostronghold.RegionManager;
import main.java.multitallented.plugins.herostronghold.RegionType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionBlockListener extends BlockListener {
    private final RegionManager regionManager;
    public RegionBlockListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Location loc = event.getBlock().getLocation();
        Location currentLoc = null;
        boolean delete = false;
        Set<Location> locations = regionManager.getRegionLocations();
        outer: for (Iterator<Location> iter = locations.iterator(); iter.hasNext();) {
            currentLoc = iter.next();
            if (currentLoc.getWorld().getName().equals(loc.getWorld().getName())) {
                if (currentLoc.getBlock().equals(loc.getBlock())) {
                    regionManager.destroyRegion(currentLoc);
                    break outer;
                }
                Region currentRegion = regionManager.getRegion(currentLoc);
                RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
                int radius = currentRegionType.getRadius();
                if (Math.sqrt(loc.distanceSquared(currentLoc)) < radius) {

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
            }
        }
        if (delete && currentLoc != null) {
            regionManager.removeRegion(currentLoc);
        }
    }
}
