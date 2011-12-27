/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.multitallented.plugins.herostronghold;

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
        for (Location l : rm.getRegionLocations()) {
            int radius = rm.getRegionType(rm.getRegion(l).getType()).getRadius();
            try {
                if (Math.sqrt(loc.distanceSquared(l)) < radius) {
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
                    return;
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }
    }
    
}
