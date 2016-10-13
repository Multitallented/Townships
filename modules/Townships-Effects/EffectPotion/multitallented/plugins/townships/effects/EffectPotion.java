package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToPlayerInRegionEvent;
import multitallented.redcastlemedia.bukkit.townships.listeners.guis.TownGUIListener;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Multitallented
 * @editor Louis Blumiere
 *
 */
public class EffectPotion extends Effect {
    public final Townships aPlugin;
    public EffectPotion(Townships plugin) {
        super(plugin);
        this.aPlugin = plugin;
        registerEvent(new IntruderListener(this));
    }

    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }

    public class IntruderListener implements Listener {
        private final EffectPotion effect;
        public IntruderListener(EffectPotion effect) {
            this.effect = effect;
        }

        @EventHandler
        public void onCustomEvent(ToPlayerInRegionEvent event) {
            Player player = event.getPlayer();

            Location l = event.getLocation();
            RegionManager rm = effect.getPlugin().getRegionManager();
            Region r = rm.getRegion(l);
            RegionType rt = rm.getRegionType(r.getType());

            //Sets the Townships value flag.
            String addSpeed = "SPEED";
            int duration = 1000;
            for (String currentEffect : rt.getEffects()) {
                String[] splitString = currentEffect.split("_");
                if (splitString.length > 1 && splitString[0].equals("potion")) {
                    addSpeed = splitString[1];
                    break;
                }
                try {
                    duration = Integer.parseInt(splitString[1].split("\\.")[0]);
                } catch (Exception e) {
                    System.out.println("[Townships] invalid config for " + r.getID() + ".yml at " + currentEffect);
                }
            }

            //Check if the players is an owner or a member of the region.
            if (!effect.isOwnerOfRegion(player, l) && !effect.isMemberOfRegion(player, l)) {
                return;
            }

            //Check to see if the Townships has enough reagents.
            if (!effect.hasReagents(l)) {
                return;
            }

            effect.forceUpkeep(event);

            //Feel free to change this line to another potion effect.
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(addSpeed), (int) (duration / 50), 1));
        }
    }

}