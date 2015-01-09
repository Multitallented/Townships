/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multitallented.redcastlemedia.bukkit.townships.events;

import java.util.ArrayList;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import org.bukkit.Location;
import org.bukkit.event.Event;

/**
 *
 * @author Multitallented
 */
public interface ToEvent {
    public void setRegionsToCreate(ArrayList<Region> regions);
    public ArrayList<Region> getRegionsToCreate();
    public void setRegionsToDestroy(ArrayList<Location> regions);
    public ArrayList<Location> getRegionsToDestroy();
    public Location getLocation();
}
