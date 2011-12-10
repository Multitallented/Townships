package main.java.multitallented.plugins.herostronghold.listeners;

import main.java.multitallented.plugins.herostronghold.HeroStronghold;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

/**
 *
 * @author Multitallented
 */
public class RegionBlockListener extends BlockListener {
    private final HeroStronghold plugin;
    public RegionBlockListener(HeroStronghold plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        //TODO check for critical block if in region
    }
}
