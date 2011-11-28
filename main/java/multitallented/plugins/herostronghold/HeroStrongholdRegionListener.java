package main.java.multitallented.plugins.herostronghold;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class HeroStrongholdRegionListener {
    private final WorldGuardPlugin worldGuard;
    private final HeroStronghold plugin;
    
    public HeroStrongholdRegionListener(HeroStronghold plugin) {
        this.plugin = plugin;
        worldGuard = plugin.getWorldGuard();
        //worldGuard.getRegionManager(null).getApplicableRegions(null);
    }
    
    private void enterExitRegionListener() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            for (ProtectedRegion region : worldGuard.getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation())) {
                //TODO detect enter region and exit region
                region.getId();
            }
        }
    }
    
}
