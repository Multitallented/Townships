package multitallented.plugins.townships.effects;

import java.util.Date;
import java.util.HashMap;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEffectEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectScheduledUpkeep extends Effect {
    private HashMap<Location, Long> lastUpkeep = new HashMap<Location, Long>();
    
    public EffectScheduledUpkeep(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectScheduledUpkeep effect;
        public UpkeepListener(EffectScheduledUpkeep effect) {
            this.effect = effect;
        }
        
        
        @EventHandler
        public void onCustomEvent(ToTwoSecondEffectEvent event) {
            if (!event.getEffect()[0].equals("scheduled_upkeep")) {
                return;
            }
            
            Region r = event.getRegion();
            Location l = r.getLocation();
            if (event.getEffect().length < 2) {
                return;
            }
            long period = Long.parseLong(event.getEffect()[1]) * 1000;


            if (lastUpkeep.get(l) == null) {
                //Check to see if the Townships has enough reagents
                if (!effect.hasReagents(l)) {
                    return;
                }
                //Run upkeep but don't need to know if upkeep occured
                effect.forceUpkeep(l);
                //effect.forceUpkeep(l);
                lastUpkeep.put(l, new Date().getTime());
                return;
            } else if (period + lastUpkeep.get(l) > new Date().getTime()) {
                return;
            }

            //Check to see if the Townships has enough reagents
            if (!effect.hasReagents(l)) {
                return;
            }
            //Run upkeep but don't need to know if upkeep occured
            effect.forceUpkeep(l);
            //effect.forceUpkeep(l);
            lastUpkeep.put(l, new Date().getTime());
        }
    }   
}