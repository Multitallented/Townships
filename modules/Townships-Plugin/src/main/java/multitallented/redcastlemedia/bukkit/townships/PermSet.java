package multitallented.redcastlemedia.bukkit.townships;

import java.io.File;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Multitallented
 */
public class PermSet {
    private int priority = 0;
    private HashMap<String, Integer> perms = new HashMap<String, Integer>();
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int input) {
        this.priority = input;
    }
    
    public HashMap<String,Integer> getPerms() {
        return perms;
    }
    
    public void setPerms(HashMap<String, Integer> input) {
        this.perms = input;
    }
    
    public static HashMap<String, PermSet> loadPermSets(Townships plugin) {
        HashMap<String, PermSet> tempMap = new HashMap<String, PermSet>();
        try {
            File groupFile = new File(plugin.getDataFolder(), "groups.yml");
            if (!groupFile.exists()) {
                groupFile.createNewFile();
            }
            
            FileConfiguration groupConfig = new YamlConfiguration();
            groupConfig.load(groupFile);
            for (String key : groupConfig.getKeys(false)) {
                PermSet ps = new PermSet();
                ConfigurationSection cs = groupConfig.getConfigurationSection(key);
                HashMap<String, Integer> currentMap = new HashMap<String, Integer>();
                ps.setPriority(groupConfig.getInt(key + ".priority", 0));
                ConfigurationSection cs1 = cs.getConfigurationSection("limits");
                if (cs1 != null) {
                    for (String s : cs1.getKeys(false)) {
                        currentMap.put(s, cs1.getInt(s));
                    }
                }
                ps.setPerms(currentMap);
                tempMap.put(key, ps);
            }
            
        } catch (Exception e) {
            System.out.println("[Townships] failed to read groups.yml");
        }
        
        return tempMap;
    }
}
