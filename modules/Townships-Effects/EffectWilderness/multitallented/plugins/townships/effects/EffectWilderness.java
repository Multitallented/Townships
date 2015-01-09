package multitallented.plugins.townships.effects;

import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
/**
 *
 * @author Multitallented
 */
public class EffectWilderness extends Effect {
    private final RegionManager rm;
    public EffectWilderness(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new IntruderListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class IntruderListener implements Listener {
        private final EffectWilderness effect;
        private final RegionManager rm;
        public IntruderListener(EffectWilderness effect) {
            this.effect = effect;
            this.rm = getPlugin().getRegionManager();
        }
        
        @EventHandler
        public void onBlockBreakEvent(BlockBreakEvent event) {
            if (event.isCancelled()) {
                return;
            }
            if (rm.getContainingRegions(event.getBlock().getLocation()).isEmpty() &&
                    rm.getContainingSuperRegions(event.getBlock().getLocation()).isEmpty()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] Make a region first. See /to list");
            }
        }
        @EventHandler
        public void onBlockBuildEvent(BlockPlaceEvent event) {
            if (event.isCancelled()) {
                return;
            }
            if (rm.getContainingRegions(event.getBlock().getLocation()).isEmpty() &&
                    rm.getContainingSuperRegions(event.getBlock().getLocation()).isEmpty()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] Make a region first. See /to list");
            }
        }
        @EventHandler
        public void onBucketEvent(PlayerBucketEmptyEvent event) {
            if (event.isCancelled()) {
                return;
            }
            if (rm.getContainingRegions(event.getBlockClicked().getLocation()).isEmpty() &&
                    rm.getContainingSuperRegions(event.getBlockClicked().getLocation()).isEmpty()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] Make a region first. See /to list");
            }
        }
        @EventHandler
        public void onBucketEvent(PlayerBucketFillEvent event) {
            if (event.isCancelled()) {
                return;
            }
            if (rm.getContainingRegions(event.getBlockClicked().getLocation()).isEmpty() &&
                    rm.getContainingSuperRegions(event.getBlockClicked().getLocation()).isEmpty()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "[Townships] Make a region first. See /to list");
            }
        }
    }
}