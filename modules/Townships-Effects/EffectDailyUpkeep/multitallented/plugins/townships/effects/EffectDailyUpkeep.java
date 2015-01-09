package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToDayEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectDailyUpkeep extends Effect {
    
    public EffectDailyUpkeep(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectDailyUpkeep effect;
        public UpkeepListener(EffectDailyUpkeep effect) {
            this.effect = effect;
        }
        
        
        @EventHandler
        public void onCustomEvent(ToDayEvent event) {
            for (Region r : getPlugin().getRegionManager().getSortedRegions()) {
                if (r == null) {
                    return;
                }
                Location l = r.getLocation();
                RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType()); 

                //Check if the region has the shoot arrow effect and return arrow velocity
                boolean hasEffect = false;
                for (String s : rt.getEffects()) {
                    if (!s.startsWith("daily_upkeep")) {
                        hasEffect = true;
                        break;
                    }
                }
                if (!hasEffect) {
                    return;
                }         

                //Run upkeep but don't need to know if upkeep occured
                effect.upkeep(l);
            }
        }
    }   
}