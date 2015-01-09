package multitallented.plugins.townships.effects;

import com.herocraftonline.heroes.api.events.HeroRegainHealthEvent;
import com.herocraftonline.heroes.characters.Hero;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

/**
 *
 * @author Multitallented
 */
public class EffectGainHealth extends Effect {
    public final Townships aPlugin;
    public EffectGainHealth(Townships plugin) {
        super(plugin);
        this.aPlugin = plugin;
        registerEvent(new IntruderListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class IntruderListener implements Listener {
        private final EffectGainHealth effect;
        public IntruderListener(EffectGainHealth effect) {
            this.effect = effect;
        }
        
        @EventHandler
        public void onCustomEvent(ToPlayerInRegionEvent event) {
            Player player = event.getPlayer();

            if (!(player.getHealth() < player.getMaxHealth())) {
                return;
            }
            
            
            Location l = event.getLocation();
            RegionManager rm = effect.getPlugin().getRegionManager();
            Region r = rm.getRegion(l);
            RegionType rt = rm.getRegionType(r.getType());
            
            //Check if the region has the shoot arrow effect and return arrow velocity
            int addHealth = effect.regionHasEffect(rt.getEffects(), "gain_health");
            if (addHealth == 0) {
                return;
            }
            
            //Check if the player owns or is a member of the region
            if (!effect.isOwnerOfRegion(player, l) && !effect.isMemberOfRegion(player, l)) {
                return;
            }
            
            //Check to see if the Townships has enough reagents
            if (!effect.hasReagents(l)) {
                return;
            }
            
            //Run upkeep but don't need to know if upkeep occured
            effect.forceUpkeep(event);
            
            //grant the player hp
            if (Townships.heroes == null) {
                EntityRegainHealthEvent e = new EntityRegainHealthEvent(player, addHealth, RegainReason.CUSTOM);
                effect.aPlugin.getServer().getPluginManager().callEvent(e);
            } else {
                Hero hero = Townships.heroes.getCharacterManager().getHero(player);
                hero.heal(addHealth);
            }
            /*if (player.getHealth() + addHealth < player.getMaxHealth()) {
                player.setHealth(player.getHealth() + addHealth);
            } else {
                player.setHealth(player.getMaxHealth());
            }*/
        }
    }
    
}
