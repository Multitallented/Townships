package multitallented.plugins.townships.effects;

import java.util.HashMap;
import java.util.HashSet;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerEnterSRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerExitSRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author Multitallented
 */
public class EffectMessage extends Effect {
    private final Townships plugin;
    public EffectMessage(Townships plugin) {
        super(plugin);
        this.plugin = plugin;
        registerEvent(new IntruderListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class IntruderListener implements Listener {
        private final EffectMessage effect;
        public IntruderListener(EffectMessage effect) {
            this.effect = effect;
        }
        
        @EventHandler
        public void onSRegionEnter(ToPlayerEnterSRegionEvent event) {
            RegionManager rm = getPlugin().getRegionManager();
            SuperRegion sr = rm.getSuperRegion(event.getName());
            if (sr == null) {
                return;
            }
            SuperRegionType srt = rm.getSuperRegionType(sr.getType());
            if (srt == null) {
                return;
            }
            if (!hasMessage(srt)) {
                return;
            }
            
            event.getPlayer().sendMessage(ChatColor.WHITE + "[Townships] You have entered " + ChatColor.RED + sr.getName());
        }
        
        @EventHandler
        public void onSRegionExit(ToPlayerExitSRegionEvent event) {
            if (event.getLoggingOut()) {
                return;
            }
            RegionManager rm = getPlugin().getRegionManager();
            SuperRegion sr = rm.getSuperRegion(event.getName());
            if (sr == null) {
                return;
            }
            SuperRegionType srt = rm.getSuperRegionType(sr.getType());
            if (srt == null) {
                return;
            }
            if (!hasMessage(srt)) {
                return;
            }
            
            event.getPlayer().sendMessage(ChatColor.WHITE + "[Townships] You have exited " + ChatColor.RED + sr.getName());
        }
        
        private boolean hasMessage(SuperRegionType srt) {
            for (String s : srt.getEffects()) {
                if (s.startsWith("message")) {
                    return true;
                }
            }
            return false;
        }
    }
}
