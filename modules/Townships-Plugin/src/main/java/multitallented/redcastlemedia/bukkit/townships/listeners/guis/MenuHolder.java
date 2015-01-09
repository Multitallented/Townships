package multitallented.redcastlemedia.bukkit.townships.listeners.guis;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author Multitallented
 */
public class MenuHolder implements InventoryHolder {
    private final Inventory inventory;
    
    public MenuHolder(Inventory inventory) {
        this.inventory = inventory;
}
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
}
