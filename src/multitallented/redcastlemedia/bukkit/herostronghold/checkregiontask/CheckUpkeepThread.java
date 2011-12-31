package multitallented.redcastlemedia.bukkit.herostronghold.checkregiontask;

import multitallented.redcastlemedia.bukkit.herostronghold.events.UpkeepEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Multitallented
 */
public class CheckUpkeepThread implements Runnable {
    private final Location l;
    private final RegionManager rm;
    private final PluginManager pm;
    private final CheckRegionTask crt;
    public CheckUpkeepThread(CheckRegionTask crt, PluginManager pm, RegionManager rm, Location l) {
        this.crt = crt;
        this.pm = pm;
        this.rm = rm;
        this.l = l;
        
    }

    @Override
    public void run() {
        double upkeepChance = rm.getRegionType(rm.getRegion(l).getType()).getUpkeepChance();
        if (Math.random() < upkeepChance) {
                //Dispatch event to be caught by effects of the region
                UpkeepEvent uEvent = new UpkeepEvent(l);
                pm.callEvent(uEvent);
                
                //Add any regions that need to be destroyed to the list
                try {
                    for (Location dl : uEvent.getRegionsToDestroy()) {
                        if (!crt.containsRegionToDestory(dl)) {
                            crt.addOrDestroyRegionToDestroy(dl);
                        }
                    }
                } catch (NullPointerException npe) {

                }
            }
    }
    
}
