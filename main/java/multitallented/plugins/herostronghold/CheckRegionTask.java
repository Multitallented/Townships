package main.java.multitallented.plugins.herostronghold;

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
        for (Location l : liveRegions.keySet()) {
            Region currentRegion = liveRegions.get(l);
            RegionType currentRegionType = regionManager.getRegionType(currentRegion.getType());
            int radius = currentRegionType.getRadius();
            
            //Check for players in regions
            for (Player p : server.getOnlinePlayers()) {
                Location loc = p.getLocation();
                if (Math.sqrt(loc.distanceSquared(l)) < radius) {
                    server.getPluginManager().callEvent(new PlayerInRegionEvent(currentRegion.getID()));
                }
            }
            
            //Check for upkeep
            if (Math.random() < currentRegionType.getUpkeepChance()) {
                server.getPluginManager().callEvent(new UpkeepEvent(currentRegion.getID()));
            }
        }
    }
}
