package multitallented.plugins.townships.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.*;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Multitallented
 */
public class EffectFood extends Effect {
    protected HashSet<SuperRegion> unfedRegions = new HashSet<SuperRegion>();
    protected final RegionManager rm;
    private final String EFFECT_NAME = "food";
    private final int EFFECT_DURATION = 600; //ticks
    private final double EFFECT_CHANCE = 0.005;
    
    public EffectFood(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new UpkeepListener(this));
    }
    
    public class UpkeepListener implements Listener {
        private final EffectFood effect;
        public UpkeepListener(EffectFood effect) {
            this.effect = effect;
            loadSuperRegions();
        }
        
        
        @EventHandler
        public void onCustomEvent(ToTwoSecondEvent event) {
            RegionManager rm = effect.rm;
            for (SuperRegion sr : unfedRegions) {
                for (String s : sr.getOwners()) {
                    Player p = Bukkit.getPlayer(s);
                    if (p == null || Math.random() > EFFECT_CHANCE || !rm.getContainingSuperRegions(p.getLocation()).contains(sr)) {
                        continue;
                    }
                    forceHunger(p);
                }
                for (String s : sr.getMembers().keySet()) {
                    Player p = Bukkit.getPlayer(s);
                    if (Math.random() > EFFECT_CHANCE || p == null || !sr.getMember(s).contains("member") ||
                            !rm.getContainingSuperRegions(p.getLocation()).contains(sr)) {
                        continue;
                    }
                    forceHunger(p);
                }
            }
        }
        
        private void forceHunger(Player p) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 1));
            p.sendMessage(ChatColor.GRAY + "[Townships] There is a shortage of food in this area.");
            p.sendMessage(ChatColor.GRAY + "[Townships] Build a food supply.");
        }
        
        @EventHandler
        public void onSuperRegionCreated(ToSuperRegionCreatedEvent event) {
            SuperRegion sr = rm.getSuperRegion(event.getName());
            SuperRegionType srt = rm.getSuperRegionType(sr.getType());
            if (!srt.hasEffect(EFFECT_NAME)) {
                return;
            }
            for (Region r : rm.getContainedRegions(sr)) {
                RegionType rt = rm.getRegionType(r.getType());
                boolean hasEffect = false;
                for (String effectName : rt.getEffects()) {
                    if (effectName.equals(EFFECT_NAME) {
                        hasEffect = true;
                        break;
                    }
                }
                if (!hasEffect) {
                    continue;
                }
                return;
            }
            unfedRegions.add(sr);
        }
        
        @EventHandler
        public void onSuperRegionDestroyed(ToSuperRegionDestroyedEvent event) {
            unfedRegions.remove(event.getSuperRegion);
        }
        
        @EventHandler
        public void onRegionCreated(ToRegionCreatedEvent event) {
            RegionManager rm = getPlugin().getRegionManager();
            Region r = event.getRegion();
            if (r == null || rm.getRegionType(r.getType()) == null) {
                return;
            }
            RegionType rt = rm.getRegionType(r.getType());
            boolean hasEffect = false;
            for (String effectName : rt.getEffects()) {
                if (effectName.equals(EFFECT_NAME) {
                    hasEffect = true;
                    break;
                }
            }
            if (!hasEffect) {
                continue;
            }
            outer: for (SuperRegion sr : rm.getContainingSuperRegions(r.getLocation())) {
                if (unfedRegions.contains(sr)) {
                    unfedRegions.remove(sr);
                }
            }
        }
        
        @EventHandler
        public void onRegionDestroyed(ToRegionDestroyedEvent event) {
            loadSuperRegions();
            /*Region r = event.getRegion();
            if (effect.regionHasEffect(r, EFFECT_NAME) == 0) {
                return;
            }
            outer: for (SuperRegion sr : rm.getContainingSuperRegions(r.getLocation())) {
                SuperRegionType srt = rm.getSuperRegionType(sr.getType());
                if (!srt.hasEffect(EFFECT_NAME)) {
                    continue;
                }
                if (fedRegions.containsKey(sr)) {
                    ArrayList<Region> re = fedRegions.get(sr);
                    if (re.contains(r)) {
                        re.remove(r);
                        if (re.isEmpty()) {
                            fedRegions.remove(sr);
                            unfedRegions.add(sr);
                        }
                    }
                } else if (!unfedRegions.contains(sr)) {
                    unfedRegions.add(sr);
                }
            }*/
        }
        
        private void loadSuperRegions() {
            outer: for (SuperRegion sr : rm.getSortedSuperRegions()) {
                SuperRegionType srt = rm.getSuperRegionType(sr.getType());
                if (!srt.hasEffect(EFFECT_NAME)) {
                    continue;
                }
                boolean fed = false;
                regionLoop: for (Region r : rm.getContainedRegions(sr)) {
                    RegionType rt = rm.getRegionType(r.getType());
                    for (String effectName : rt.getEffects()) {
                        if (effectName.equals(EFFECT_NAME) {
                            fed = true;
                            break regionLoop;
                        }
                    }
                }
                if (!fed) {
                    unfedRegions.add(sr);
                }
            }
        }
    }
    
}
