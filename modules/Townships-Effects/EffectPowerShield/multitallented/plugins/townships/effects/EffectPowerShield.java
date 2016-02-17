package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.region.RegionCondition;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author Multitallented
 */
public class EffectPowerShield extends Effect {
    public EffectPowerShield(Townships plugin) {
        super(plugin);
        registerEvent(new EffectPowerShield.UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
        
    }
    
    public class UpkeepListener implements Listener {
        private final EffectPowerShield effect;
        private RegionManager rm;
        public UpkeepListener(EffectPowerShield effect) {
            this.effect = effect;
            rm = effect.getPlugin().getRegionManager();
        }
        
        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
        public void onEntityExplode(EntityExplodeEvent event) {
            Entity e = event.getEntity();
            
            if (!(e instanceof TNTPrimed)) {
                return;
            }

            //TODO offset this by 5
            superRegionLoop: for (SuperRegion sr : rm.getContainingSuperRegions(event.getLocation())) {
                int powerDamage = 1;
                for (String effectString : rm.getSuperRegionType(sr.getType()).getEffects()) {
                    if (effectString.startsWith("power_shield")) {
                        String[] effectParts = effectString.split("\\.");
                        if (effectParts.length > 1) {
                            try {
                                powerDamage = Integer.parseInt(effectParts[1]);
                            } catch (Exception e) {

                            }
                        }
                        if (sr.getPower() > 0) {
                            event.setCancelled(true);
                            sr.setPower(Math.max(0, sr.getPower() - powerDamage));
                            //TODO check for destroy region
                            break superRegionLoop;
                        }
                    }
                }
            }


            /*if (rm.shouldTakeAction(event.getLocation(), null, new RegionCondition("power_shield", true, 0))) {
                boolean powerReduced = false;
                for (SuperRegion sr : rm.getContainingSuperRegions(event.getLocation())) {
                    if (sr.getPower() > 0 && rm.getSuperRegionType(sr.getType()).getEffects().contains("power_shield")) {
                        
                        powerReduced = true;
                        rm.reduceRegion(sr);
                    }
                }
                if (powerReduced) {
                    event.setCancelled(true);
                }
            }*/
        }
    }
}