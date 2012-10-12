package multitallented.redcastlemedia.bukkit.herostronghold.checkregiontask;

import java.util.HashSet;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import multitallented.redcastlemedia.bukkit.herostronghold.events.TwoSecondEvent;
import multitallented.redcastlemedia.bukkit.herostronghold.region.Region;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Multitallented
 */
public class CheckRegionTask implements Runnable {
    private final transient Server server;
    private final HeroStronghold hs;
    private final Set<Location> regionsToDestroy = new HashSet<Location>();
    private final HashSet<Region> regionsToCreate = new HashSet<Region>();
    private int i= 0;
    
    public CheckRegionTask(Server server, HeroStronghold hs) {
        this.server = server;
        this.hs = hs;
    }
    
    public synchronized void addOrDestroyRegionToDestroy(Location l) {
        if (!regionsToDestroy.remove(l)) {
            regionsToDestroy.add(l);
        }
    }
    
    public synchronized void addRegionToCreate(Region r) {
        regionsToCreate.add(r);
    }
    
    public boolean containsRegionToDestory(Location l) {
        return regionsToDestroy.contains(l);
    }
    
    public HashSet<Region> getRegiosToCreate(Region r) {
        return regionsToCreate;
    }

    @Override
    public void run() {
        Player[] players = server.getOnlinePlayers();
        int chunk = players.length / 4;
        PluginManager pm = server.getPluginManager();
        for (int j=chunk * i; j<(i==3 ? players.length : chunk * (i+1)); j++) {
            try {
                CheckPlayerInRegionThread thread = new CheckPlayerInRegionThread(this, pm, hs.getRegionManager(), players[j]);
                thread.run();
            } catch (Exception e) {
                
            }
        }
        if (i == 3) {
            i=-1;
            
            
            for (Location l : hs.getRegionManager().getRegionLocations()) {
                CheckUpkeepThread thread = new CheckUpkeepThread(this, pm, hs.getRegionManager(), l);
                try {
                    thread.run();
                } catch (Exception e) {
                }
            }
            pm.callEvent(new TwoSecondEvent());
        } else {
            i++;
        }
        
        for (Location l : regionsToDestroy) {
            hs.getRegionManager().destroyRegion(l);
            hs.getRegionManager().removeRegion(l);
        }
        regionsToDestroy.clear();
        for (Region r : regionsToCreate) {
            hs.getRegionManager().addRegion(r.getLocation(), r.getType(), r.getOwners(), r.getMembers());
        }
        regionsToCreate.clear();
    }
}
