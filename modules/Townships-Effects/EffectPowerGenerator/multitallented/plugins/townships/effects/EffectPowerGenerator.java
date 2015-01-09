package multitallented.plugins.townships.effects;

import java.util.Date;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectPowerGenerator extends Effect {
    private HashMap<Location, Long> lastUpkeep = new HashMap<Location, Long>();
    
    public EffectPowerGenerator(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(plugin, this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectPowerGenerator effect;
        private final Townships plugin;
        public UpkeepListener(Townships plugin, EffectPowerGenerator effect) {
            this.effect = effect;
            this.plugin = plugin;
        }
        
        
        @EventHandler
        public void onCustomEvent(ToTwoSecondEvent event) {
            for (Region r : plugin.getRegionManager().getSortedRegions()) {
                Location l = r.getLocation();
                RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType()); 

                if (rt == null) {
                    return;
                }
                
                //Check if the region has the shoot arrow effect and return arrow velocity
                long period = effect.regionHasEffect(rt.getEffects(), "power_generator");
                if (period == 0) {
                    return;
                }

                period *= 1000;            

                if (lastUpkeep.get(l) != null && period + lastUpkeep.get(l) > new Date().getTime()) {
                    return;
                }

                //Check to see if the Townships has enough reagents
                if (!effect.hasReagents(l)) {
                    return;
                }
                //Run upkeep but don't need to know if upkeep occured
                lastUpkeep.put(l, new Date().getTime());

                for (SuperRegion sr : plugin.getRegionManager().getContainingSuperRegions(l)) {
                    if (sr.getPower() < plugin.getRegionManager().getSuperRegionType(sr.getType()).getMaxPower()) {
                        sr.setPower(sr.getPower() + 1);
                        effect.forceUpkeep(l);
                    }
                }
            }
        }
    }
    
}
