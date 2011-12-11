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
    private final EffectManager effectManager;
    public CheckRegionTask(Server server, EffectManager effectManager, RegionManager regionManager) {
        this.server = server;
        this.regionManager = regionManager;
        this.effectManager = effectManager;
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
                    //TODO execute effects
                }
            }
            
            //TODO check for upkeep
        }
    }
}
