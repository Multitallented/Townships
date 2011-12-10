package main.java.multitallented.plugins.herostronghold;

import org.bukkit.Server;

/**
 *
 * @author Multitallented
 */
public class CheckRegionTask implements Runnable {
    private final transient Server server;
    public CheckRegionTask(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        //TODO check for destroyed regions
        
        //TODO check for players in regions
    }
}
