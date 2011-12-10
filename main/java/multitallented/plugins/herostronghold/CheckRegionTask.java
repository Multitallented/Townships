package main.java.multitallented.plugins.herostronghold;

import org.bukkit.Server;

/**
 *
 * @author Multitallented
 */
public class CheckRegionTask implements Runnable {
    private final transient Server server;
    private final boolean exploding;
    public CheckRegionTask(Server server, boolean exploding) {
        this.server = server;
        this.exploding = exploding;
    }

    @Override
    public void run() {
        //TODO check for destroyed regions
        
        //TODO check for players in regions
    }
}
