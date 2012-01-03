package multitallented.redcastlemedia.bukkit.herostronghold.listeners;

import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Multitallented
 */
public class SendMessageThread implements Runnable {
    private final Map<Player, String> channels;
    private final Player player;
    private final String channel;
    private final String message;
    private final String title;

    public SendMessageThread(String channel, Map<Player, String> channels, String title, Player player, String message) {
        this.channels = channels;
        this.player = player;
        this.channel = channel;
        this.message = message;
        if (title == null || title.equals("")) {
            this.title = "";
        } else {
            this.title = title + ", ";
        }
    }

    @Override
    public void run() {
        int i=0;
        for (Player p : channels.keySet()) {
            if (channels.get(p).equals(channel)) {
                p.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + channel + ChatColor.GRAY + "]" + title + player.getDisplayName()
                        + ": " + message);
                i++;
            }
        }
        if (i<=1)
            player.sendMessage(ChatColor.GOLD + "[" + channel + "] No hears you. You are alone in this channel.");
    }
    
}
