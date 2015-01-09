package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectPeriodicFarm extends Effect {
    private final RegionManager rm;
    public EffectPeriodicFarm(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new IntruderListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class IntruderListener implements Listener {
        private final EffectPeriodicFarm effect;

        public IntruderListener(EffectPeriodicFarm effect) {
            this.effect = effect;
        }
        
        @EventHandler
        public void onCustomEvent(ToPlayerInRegionEvent event) {
            
        }
    }
}
