package main.java.multitallented.plugins.herostronghold;

import java.util.Iterator;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class CheckRegionTask implements Runnable {
    private final transient Server server;
    private final RegionManager regionManager;
    
    public CheckRegionTask(Server server, RegionManager regionManager) {
        this.server = server;
        this.regionManager = regionManager;
    }

    @Override
    public void run() {
        Map<Location, Region> liveRegions = regionManager.getRegions();
        for (Iterator<Location> locations = liveRegions.keySet().iterator(); locations.hasNext();) {
            Location l = locations.next();
            Region currentRegion = liveRegions.get(l);
            RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
            int radius = currentRegionType.getRadius();
            
            //Check for players in regions
            for (Player p : server.getOnlinePlayers()) {
                Location loc = p.getLocation();
                if (Math.sqrt(loc.distanceSquared(l)) < radius) {
                    server.getPluginManager().callEvent(new PlayerInRegionEvent(currentRegion.getLocation(), p, currentRegionType.getEffects()));
                }
            }
            
            //Check for upkeep
            if (Math.random() < currentRegionType.getUpkeepChance()) {
                //Dispatch event to be caught by effects of the region
                server.getPluginManager().callEvent(new UpkeepEvent(l));
            }
        }
    }
}
