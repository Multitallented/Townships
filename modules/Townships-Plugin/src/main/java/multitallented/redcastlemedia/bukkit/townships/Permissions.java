package multitallented.redcastlemedia.bukkit.townships;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * Created by Multi on 11/17/2015.
 */
public class Permissions {
    public static void assignPermissions(Townships plugin) {
        if (Townships.perms == null || !Townships.perms.hasGroupSupport()) {
            return;
        }
        File folder = plugin.getDataFolder();
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                return;
            }
        }
        File permissionsFile = new File(folder, "permissions.yml");
        if (!permissionsFile.exists()) {
            return;
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(permissionsFile);
            List<String> permissionsList = config.getStringList("permissions");
            if (permissionsList == null || permissionsList.isEmpty()) {
                return;
            }
            for (String perm : permissionsList) {
                for (String groupName : Townships.perms.getGroups()) {
                    if (Townships.perms.groupHas((String) null, groupName, perm)) {
                        continue;
                    }
                    Townships.perms.groupAdd((String) null, groupName, perm);
                }
            }
            permissionsFile.delete();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to read permissions.yml");
            e.printStackTrace();
            return;
        }
    }
}
