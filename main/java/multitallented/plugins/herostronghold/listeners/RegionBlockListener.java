package main.java.multitallented.plugins.herostronghold.listeners;

import java.util.ArrayList;
import java.util.Iterator;
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
        for (Location currentLoc : regionManager.getRegionLocations()) {
            if (currentLoc.getBlock().equals(loc.getBlock())) {
                //TODO check for deny build
                regionManager.destroyRegion(currentLoc);
                return;
            }
            Region currentRegion = regionManager.getRegion(currentLoc);
            RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
            int radius = currentRegionType.getRadius();
            if (Math.sqrt(loc.distanceSquared(currentRegion.getLocation())) < radius) {
                //TODO check for deny build
                ArrayList<ItemStack> requirements = currentRegionType.getRequirements();
                boolean correctionNeeded = true;
                for (int x=((int) currentLoc.getX()-radius); x<Math.abs(radius + currentLoc.getX()); x++) {
                    for (int y = currentLoc.getY()- radius > 0 ? ((int) currentLoc.getY() - radius) : 0; y< Math.abs((radius) + currentLoc.getY()) && (y + currentLoc.getY() < 128); y++) {
                        for (int z = ((int) currentLoc.getZ() - radius); z<Math.abs(radius + currentLoc.getZ()); z++) {
                            Block tempBlock = currentLoc.getWorld().getBlockAt(x, y, z);
                            if (tempBlock.getTypeId() == loc.getBlock().getTypeId()) {
                                for (Iterator<ItemStack> iter = requirements.iterator(); iter.hasNext(); ) {
                                    ItemStack is = iter.next();
                                    if (tempBlock.getType().equals(is.getType())) {
                                        if (correctionNeeded) {
                                            correctionNeeded = false;
                                        } else if (is.getAmount() <= 1) {
                                            iter.remove();
                                            if (requirements.isEmpty()) {
                                                System.out.println("not a necessity");
                                                return;
                                            }
                                        } else {
                                            is.setAmount(is.getAmount() -1 );
                                        }
                                        System.out.println(is.getType().name() + "." + is.getAmount());
                                        
                                    }
                                }
                            }
                        }
                    }
                    if (x + 1 >= Math.abs(radius + currentLoc.getX())) {
                        System.out.println("pre-destroy-region");
                        regionManager.destroyRegion(currentLoc);
                        return;
                    }
                }
                System.out.println("failed-to-destroy");
            }
        }
    }
}
