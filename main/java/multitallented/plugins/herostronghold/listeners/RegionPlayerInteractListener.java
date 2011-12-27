package main.java.multitallented.plugins.herostronghold.listeners;

import main.java.multitallented.plugins.herostronghold.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Multitallented
 */
public class RegionPlayerInteractListener extends PlayerListener {
    private final RegionManager rm;
    public RegionPlayerInteractListener(RegionManager rm) {
        this.rm = rm;
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getClickedBlock().getLocation(), event.getPlayer(), 0, "denyplayerinteract"))
            return;

        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        event.setCancelled(true);
    }

    @Override
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getBed().getLocation(), event.getPlayer(), 0, "denyplayerinteract"))
            return;

        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        event.setCancelled(true);
    }

    @Override
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getBlockClicked().getLocation(), event.getPlayer(), 0, "denyplayerinteract"))
            return;

        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        event.setCancelled(true);
    }

    @Override
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled() || !rm.shouldTakeAction(event.getBlockClicked().getLocation(), event.getPlayer(), 0, "denyplayerinteract"))
            return;

        event.getPlayer().sendMessage(ChatColor.GRAY + "[HeroStronghold] This region is protected");
        event.setCancelled(true);
    }
}
