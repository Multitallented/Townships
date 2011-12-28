package main.java.multitallented.plugins.herostronghold.listeners;

import main.java.multitallented.plugins.herostronghold.region.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EndermanPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

/**
 *
 * @author Multitallented
 */
public class RegionEntityListener extends EntityListener {
    private final RegionManager rm;
    public RegionEntityListener(RegionManager rm) {
        this.rm = rm;
    }
    
    @Override
        public void onPaintingPlace(PaintingPlaceEvent event) {
            if (event.isCancelled() || !rm.shouldTakeAction(event.getPainting().getLocation(), event.getPlayer(), 0, "denyblockbuild"))
                return;
            
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        }
        
        @Override
        public void onEndermanPlace(EndermanPlaceEvent event) {
            if (event.isCancelled() || !rm.shouldTakeAction(event.getLocation(), null, 0, "denyblockbuild"))
                return;
            event.setCancelled(true);
        }
        
        @Override
        public void onPaintingBreak(PaintingBreakEvent event) {
            if (event.isCancelled() || !(event instanceof PaintingBreakByEntityEvent))
                return;
            PaintingBreakByEntityEvent pEvent = (PaintingBreakByEntityEvent) event;
            if (!(pEvent.getRemover() instanceof Player))
                return;
            Player player = (Player) pEvent.getRemover();
            if (!rm.shouldTakeAction(event.getPainting().getLocation(), player, 0, "denyblockbreak"))
                return;
            
            event.setCancelled(true);
            player.sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        }
        
        
        @Override
        public void onEntityExplode(EntityExplodeEvent event) {
            if (event.isCancelled()) {
                return;
            }
            Location l = event.getLocation();
            Entity ent = event.getEntity();
            if ((!(ent instanceof Creeper) && !(ent instanceof EnderDragon) && !(ent instanceof TNTPrimed) && !(ent instanceof Fireball)
                    && !rm.shouldTakeAction(l, null, 4, "denyblockbreak")))
                event.setCancelled(true);
        }
        
        @Override
        public void onExplosionPrime(ExplosionPrimeEvent event) {
            if (event.isCancelled() || !rm.shouldTakeAction(event.getEntity().getLocation(), null, 0, "denyblockbreak"))
                return;
            event.setCancelled(true);
        }
        
        @Override
        public void onEndermanPickup(EndermanPickupEvent event) {
            if (event.isCancelled() || !rm.shouldTakeAction(event.getBlock().getLocation(), null, 0, "denyblockbreak"))
                return;
            event.setCancelled(true);
        }
}
