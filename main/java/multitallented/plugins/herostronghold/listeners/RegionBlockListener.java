package main.java.multitallented.plugins.herostronghold.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import main.java.multitallented.plugins.herostronghold.HeroStronghold;
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
    private final HeroStronghold plugin;
    private final RegionManager regionManager;
    public RegionBlockListener(HeroStronghold plugin, RegionManager regionManager) {
        this.plugin = plugin;
        this.regionManager = regionManager;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Location loc = event.getBlock().getLocation();
        Set<Location> locations = regionManager.getRegionLocations();
        for (Iterator<Location> iter = locations.iterator(); iter.hasNext();) {
            Location currentLoc = iter.next();
            if (currentLoc.getBlock().equals(loc.getBlock())) {
                regionManager.destroyRegion(currentLoc);
                return;
            }
            Region currentRegion = regionManager.getRegion(currentLoc);
            RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
            int radius = currentRegionType.getRadius();
            if (Math.sqrt(loc.distanceSquared(currentLoc)) < radius) {
                
                int amountRequired = 0;
                System.out.println(currentRegionType.getRequirements().size());
                for (ItemStack currentStack : currentRegionType.getRequirements()) {
                    System.out.println(currentStack.getType().name() + "." + currentStack.getAmount());
                    if (currentStack.getTypeId() == event.getBlock().getTypeId()) {
                        amountRequired = currentStack.getAmount();
                        break;
                    }
                }
                System.out.println(amountRequired);
                if (amountRequired == 0)
                    return;
                
                amountRequired++;
                for (int x= (int) (currentLoc.getX()-radius); x<radius + currentLoc.getX(); x++) {
                    for (int y = currentLoc.getY()- radius > 1 ? (int) (currentLoc.getY() - radius) : 1; y< radius + currentLoc.getY() && y < 128; y++) {
                        for (int z = (int) (currentLoc.getZ() - radius); z<radius + currentLoc.getZ(); z++) {
                            Block tempBlock = currentLoc.getWorld().getBlockAt(x, y, z);
                            if (tempBlock.getTypeId() == event.getBlock().getTypeId()) {
                                if (amountRequired <= 1) {
                                    return;
                                } else {
                                    amountRequired--;
                                }
                                System.out.println(amountRequired);
                            }
                        }
                    }
                }
                regionManager.destroyRegion(currentLoc);
            }
        }
    }
}
