/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multitallented.redcastlemedia.bukkit.herostronghold;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Multitallented
 */
public class PermSet {
    public HashMap<String, HashMap<String, Integer>> loadPermSets(HeroStronghold plugin) {
        HashMap<String, HashMap<String, Integer>> tempMap = new HashMap<String, HashMap<String, Integer>>();
        try {
            File groupFile = new File(plugin.getDataFolder(), "groups.yml");
            if (!groupFile.exists()) {
                groupFile.createNewFile();
            }
            
            FileConfiguration groupConfig = new YamlConfiguration();
            groupConfig.load(groupFile);
            for (String key : groupConfig.getKeys(false)) {
                ConfigurationSection cs = groupConfig.getConfigurationSection(key);
                HashMap<String, Integer> currentMap = new HashMap<String, Integer>();
                for (String s : cs.getKeys(false)) {
                    currentMap.put(s, cs.getInt(s));
                }
                tempMap.put(key, currentMap);
            }
            
        } catch (Exception e) {
            System.out.println("[HeroStronghold] failed to read groups.yml");
        }
        
        return tempMap;
    }
}
