package multitallented.plugins.townships.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;

/**
 *
 * @author Multitallented
 */
public class EffectGate extends Effect {
    private final RegionManager rm;
    private final Map<Location, Set<Block>> gates = new HashMap<Location, Set<Block>>();
    private final Set<Location> openGates = new HashSet<Location>();
    public EffectGate(Townships plugin) {
        super(plugin);
        this.rm = plugin.getRegionManager();
        registerEvent(new IntruderListener(this));
        registerEvent(new CloseGateListener());
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class CloseGateListener implements Listener {
        @EventHandler
        public void onPluginDisable(PluginDisableEvent event) {
            //Close gates if the plugin is going to be disabled
            if (event.getPlugin().getDescription().getName().equals("Townships")) {
                for (Location l : openGates) {
                    for (Block b : gates.get(l)) {
                        b.setTypeId(85);
                    }
                }
            }
        }
    }
    
    public class IntruderListener implements Listener {
        private final EffectGate effect;
        public IntruderListener(EffectGate effect) {
            this.effect = effect;
        }
        
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            Block block = event.getClickedBlock();
            //Check if a sign
            try {
                if (!(block.getState() instanceof Sign)) {
                    return;
                }
            } catch (NullPointerException npe) {
                return;
            }
            //Check if its a gate sign
            Sign sign = (Sign) block.getState();
            if (!sign.getLine(0).equalsIgnoreCase("[Gate]")) {
                return;
            }
            
            Location currentLocation = block.getLocation();
            
            double x1 = currentLocation.getX();
            Location loc = null;
            for (Region r : rm.getSortedRegions()) {
                double radius = rm.getRegionType(r.getType()).getRadius();
                Location l = r.getLocation();
                if (l.getX() + radius < x1) {
                    return;
                }
                try {
                    if (!(l.getX() - radius > x1) && l.distanceSquared(currentLocation) < radius) {
                        loc = l;
                        break;
                    }
                } catch (IllegalArgumentException iae) {

                }
            }
            if (loc == null) {
                return;
            }
            
            //Check if the region has the shoot arrow effect and return arrow velocity
            double speed = effect.regionHasEffect(effect.rm.getRegionType(effect.rm.getRegion(loc).getType()).getEffects(), "gate");
            if (speed == 0) {
                return;
            }
            
            Player player = event.getPlayer();
            
            //Check if the player owns or is a member of the region
            if (!effect.isOwnerOfRegion(player, loc) && !effect.isMemberOfRegion(player, loc)) {
                return;
            }
            
            //Check to see if the Townships has enough reagents
            if (!effect.hasReagents(loc)) {
                return;
            }
            
            //Run upkeep but don't need to know if upkeep occured
            effect.forceUpkeep(loc);
            
            //Open or close the gate
            if (!gates.containsKey(loc)) {
                RegionType currentRegionType = rm.getRegionType(rm.getRegion(loc).getType());
                
                int radius = (int) Math.sqrt(currentRegionType.getRadius());

                int lowerLeftX = (int) loc.getX() - radius;
                int lowerLeftY = (int) loc.getY() - radius;
                lowerLeftY = lowerLeftY < 0 ? 0 : lowerLeftY;
                int lowerLeftZ = (int) loc.getZ() - radius;

                int upperRightX = (int) loc.getX() + radius;
                int upperRightY = (int) loc.getY() + radius;
                upperRightY = upperRightY > 255 ? 255 : upperRightY;
                int upperRightZ = (int) loc.getZ() + radius;
                
                World world = loc.getWorld();
                
                Set<Block> tempSet = new HashSet<Block>();
                
                outer: for (int x=lowerLeftX; x<upperRightX; x++) {
                    
                    for (int z=lowerLeftZ; z<upperRightZ; z++) {
                        
                        for (int y=lowerLeftY; y<upperRightY; y++) {
                            
                            Block currentBlock = world.getBlockAt(x, y, z);
                            int type = currentBlock.getTypeId();
                            if (type == 85) {
                                tempSet.add(currentBlock);
                            }
                        }
                        
                    }
                    
                }
                if (tempSet.isEmpty()) {
                    rm.destroyRegion(loc);
                    rm.removeRegion(loc);
                    return;
                }
                gates.put(loc, tempSet);
            }
            
            event.setCancelled(true);
            if (openGates.contains(loc)) {
                //Gate is open
                for (Block b : gates.get(loc)) {
                    b.setTypeId(85);
                }
                openGates.remove(loc);
            } else {
                //Gate is closed
                for (Block b : gates.get(loc)) {
                    b.setTypeId(0);
                }
                openGates.add(loc);
            }
        }
    }
    
}
