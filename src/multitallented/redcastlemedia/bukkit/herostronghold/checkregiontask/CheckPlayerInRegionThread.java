/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitallented.redcastlemedia.bukkit.herostronghold.checkregiontask;

import multitallented.redcastlemedia.bukkit.herostronghold.events.PlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Multitallented
 */
public class CheckPlayerInRegionThread implements Runnable {
    private final RegionManager rm;
    private final Location loc;
    private final Player p;
    private final PluginManager pm;
    private final CheckRegionTask crt;
    public CheckPlayerInRegionThread(CheckRegionTask crt, PluginManager pm, RegionManager rm, Player p) {
        this.crt = crt;
        this.pm = pm;
        this.rm = rm;
        this.p = p;
        this.loc = p.getLocation();
    }

    @Override
    public void run() {
        double x = loc.getX();
        for (Region r : rm.getSortedRegions()) {
            int radius = rm.getRegionType(r.getType()).getRadius();
            Location l = r.getLocation();
            if (l.getX() + radius < x) {
                return;
            }
            try {
                if (!(l.getX() - radius > x) && l.distanceSquared(loc) < radius) {
                    PlayerInRegionEvent pIREvent = new PlayerInRegionEvent(l, p);
                    pm.callEvent(pIREvent);
                    try {
                        for (Location dl : pIREvent.getRegionsToDestroy()) {
                            if (!crt.containsRegionToDestory(dl)) {
                                crt.addOrDestroyRegionToDestroy(dl);
                            }
                        }
                    } catch (NullPointerException npe) {

                    }
                    try {
                        for (Region re : pIREvent.getRegionsToCreate()) {
                            crt.addRegionToCreate(re);
                        }
                    } catch (Exception e) {
                        
                    }
                    return;
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
        
    }
    
}
