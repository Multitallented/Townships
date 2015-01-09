package multitallented.redcastlemedia.bukkit.townships.checkregiontask;

import java.util.HashMap;
import java.util.Iterator;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEffectEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondSREffectEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Multitallented
 */
public class TriggerEffectTask {
    private final RegionManager rm;
    private final PluginManager pm;
    private final CheckRegionTask crt;
    private final HashMap<String, HashMap<String, String>> regionTypes = new HashMap<String, HashMap<String, String>>();
    private final HashMap<String, HashMap<String, String>> superRegionTypes = new HashMap<String, HashMap<String, String>>();
    
    public TriggerEffectTask(RegionManager rm, PluginManager pm, CheckRegionTask crt) {
        this.rm = rm;
        this.pm = pm;
        this.crt = crt;
        
        for (String s : rm.getRegionTypes()) {
            RegionType rt = rm.getRegionType(s);
            for (String effect : rt.getEffects()) {
                if (effect.startsWith("deny")) {
                    continue;
                }
                if (regionTypes.containsKey(s)) {
                    regionTypes.get(s).put(effect.split("\\.")[0], effect);
                } else {
                    HashMap<String, String> tempMap = new HashMap<String, String>();
                    tempMap.put(effect.split("\\.")[0], effect);
                    regionTypes.put(s, tempMap);
                }
            }
        }
        for (String s : rm.getSuperRegionTypes()) {
            SuperRegionType srt = rm.getSuperRegionType(s);
            for (String effect : srt.getEffects()) {
                if (effect.startsWith("deny") || effect.startsWith("control") || effect.startsWith("housing")) {
                    continue;
                }
                if (superRegionTypes.containsKey(s)) {
                    superRegionTypes.get(s).put(effect.split("\\.")[0], effect);
                } else {
                    HashMap<String, String> tempMap = new HashMap<String, String>();
                    tempMap.put(effect.split("\\.")[0], effect);
                    superRegionTypes.put(s, tempMap);
                }
            }
        }
    }
    
    public void go() {
        for (Region re : rm.getSortedRegions()) {
            HashMap<String, String> effects = regionTypes.get(re.getType());
            if (effects == null || effects.isEmpty()) {
                continue;
            }
            for (String effect : effects.keySet()) {
                ToTwoSecondEffectEvent twoSecondEffectEvent = new ToTwoSecondEffectEvent(re, effects.get(effect).split("\\."));
                pm.callEvent(twoSecondEffectEvent);
                try {
                    for (Location dl : twoSecondEffectEvent.getRegionsToDestroy()) {
                        if (!crt.containsRegionToDestory(dl)) {
                            crt.addOrDestroyRegionToDestroy(dl);
                        }
                    }
                } catch (NullPointerException npe) {

                }
                try {
                    for (Region reg : twoSecondEffectEvent.getRegionsToCreate()) {
                        crt.addRegionToCreate(reg);
                    }
                } catch (Exception e) {

                }
            }
        }
        for (Iterator<SuperRegion> it = rm.getSortedSuperRegions().iterator(); it.hasNext();) {
            SuperRegion sr = it.next();
            HashMap<String, String> effects = superRegionTypes.get(sr.getType());
            if (effects == null || effects.isEmpty()) {
                continue;
            }
            for (String effect : effects.keySet()) {
                ToTwoSecondSREffectEvent twoSecondEffectEvent = new ToTwoSecondSREffectEvent(sr, effects.get(effect).split("\\."));
                pm.callEvent(twoSecondEffectEvent);
            }
        }
    }
}
