package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEffectEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectRandomUpkeep extends Effect {
    public EffectRandomUpkeep(Townships plugin) {
        super(plugin);
        registerEvent(new UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectRandomUpkeep effect;
        public UpkeepListener(EffectRandomUpkeep effect) {
            this.effect = effect;
        }
        
        
        @EventHandler
        public void onCustomEvent(ToTwoSecondEffectEvent event) {
            if (event.getEffect().length < 2 || !event.getEffect()[0].equals("random_upkeep")) {
                return;
            }
            Region r = event.getRegion();
            Location l = r.getLocation();
            RegionType rt = getPlugin().getRegionManager().getRegionType(r.getType());
            if (rt == null) {
                return;
            }

            //Check if the region has the shoot arrow effect and return arrow velocity
            double chance = ((double) Integer.parseInt(event.getEffect()[1])) / 100;
            double rand = Math.random();
            if (rand > chance) {
                return;
            }

            //Check to see if the region has enough reagents
            if (!effect.hasReagents(l)) {
                return;
            }
            //Run upkeep but don't need to know if upkeep occured
            effect.forceUpkeep(l);
            //effect.forceUpkeep(l);
        }
    }
    
}
