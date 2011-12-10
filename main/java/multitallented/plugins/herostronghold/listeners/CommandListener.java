package main.java.multitallented.plugins.herostronghold.listeners;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;



/**
 *
 * @author Multitallented
 */
public class CommandListener {
    public boolean dispatch(CommandSender cs, String label, String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.GRAY + "[HeroStronghold]" + ChatColor.WHITE + " doesn't recognize non-player commands.");
            return true;
        }
        Player player = (Player) cs;
        System.out.println("[HeroStronghold] " + player.getDisplayName() + ": " + label);
        if (args.length > 1 && args[0].equalsIgnoreCase("create")) {
            String regionName = args[1];
            
            //Permission Check
            if (!player.hasPermission("herostronghold." + regionName)) {
                player.sendMessage(ChatColor.GRAY + "[HeroStronghold] you dont have permission to create a " + regionName);
                return true;
            }
            
            //Check if valid region
            
            //TODO handle this command
        }
        
        return false;
    }
}
