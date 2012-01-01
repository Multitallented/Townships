package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import java.util.HashMap;
import java.util.Map;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Multitallented
 */
public class RegionPlayerInteractListener extends PlayerListener {
    private final RegionManager rm;
    private final Map<Player, String> channels = new HashMap<Player, String>();
    public RegionPlayerInteractListener(RegionManager rm) {
        this.rm = rm;
    }
    
    @Override
    public void onPlayerChat(PlayerChatEvent event) {
        //TODO add custom titles
        Player player = event.getPlayer();
        String channel = channels.get(player);
        if (channel == null || channel.equals(""))
            return;
        event.setCancelled(true);
        SendMessageThread smt = new SendMessageThread(channel, channels, player, event.getMessage());
        try {
            smt.run();
        } catch (Exception e) {
            
        }
    }
    
    public void setPlayerChannel(Player p, String s) {
        if (s.equals("")) {
            String prevChannel = channels.get(p);
            channels.remove(p);
            if (prevChannel != null && !prevChannel.endsWith(s)) {
                SendMessageThread smt = new SendMessageThread(prevChannel, channels, p, p.getDisplayName() + " has left channel " + s);
                try {
                    smt.run();
                } catch(Exception e) {

                }
            }
            return;
        }
        channels.put(p, s);
        SendMessageThread smt = new SendMessageThread(s, channels, p, p.getDisplayName() + " has joined channel " + s);
        try {
            smt.run();
        } catch (Exception e) {
            
        }
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
