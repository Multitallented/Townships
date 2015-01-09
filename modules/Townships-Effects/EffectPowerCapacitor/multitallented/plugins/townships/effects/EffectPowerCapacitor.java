package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToRegionDestroyedEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToSuperRegionCreatedEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectPowerCapacitor extends Effect {
    public EffectPowerCapacitor(Townships plugin) {
        super(plugin);
        registerEvent(new EffectPowerCapacitor.UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectPowerCapacitor effect;
        public UpkeepListener(EffectPowerCapacitor effect) {
            this.effect = effect;
        }
        
        @EventHandler
        public void onRegionCreated(ToRegionCreatedEvent event) {
            Region r = event.getRegion();
            RegionManager rm = effect.getPlugin().getRegionManager();
            int amount = effect.regionHasEffect(rm.getRegionType(r.getType()).getEffects(), "power_capacitor");
            if (amount < 1) {
                return;
            }
            
            for (SuperRegion sr : rm.getContainingSuperRegions(r.getLocation())) {
                rm.setMaxPower(sr, sr.getMaxPower() + amount);
            }
        }
        
        @EventHandler
        public void onSuperRegionCreated(ToSuperRegionCreatedEvent event) {
            RegionManager rm = effect.getPlugin().getRegionManager();
            SuperRegion sr = rm.getSuperRegion(event.getName());
            
            int powerIncrease = 0;
            outer: for (Region r : rm.getContainedRegions(sr)) {
                RegionType rt = rm.getRegionType(r.getType());
                if (rt == null) {
                    continue;
                }
                boolean hasEffect = false;
                for (String effectString : rt.getEffects()) {
                    String[] effectParts = effectString.split("\\.");
                    if (effectParts[0].equals("power_capacitor") && effectParts.length > 1) {
                        try {
                            powerIncrease += Integer.parseInt(effectParts[1]);
                        } catch (Exception e) {
                            continue outer;
                        }
                    }
                }
            }
            
            if (powerIncrease < 1) {
                return;
            }
            rm.setMaxPower(sr, powerIncrease + sr.getMaxPower());
        }
        
        @EventHandler
        public void onRegionDestroyed(ToRegionDestroyedEvent event) {
            Region r = event.getRegion();
            RegionManager rm = effect.getPlugin().getRegionManager();
            int amount = effect.regionHasEffect(rm.getRegionType(r.getType()).getEffects(), "power_capacitor");
            if (amount < 1) {
                return;
            }
            
            for (SuperRegion sr : rm.getContainingSuperRegions(r.getLocation())) {
                if (sr.getMaxPower() - amount < 0) {
                    rm.setMaxPower(sr, 1);
                    continue;
                }
                rm.setMaxPower(sr, sr.getMaxPower() - amount);
            }
        }
    }
}