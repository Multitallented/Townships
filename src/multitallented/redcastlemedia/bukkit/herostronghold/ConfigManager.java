package multitallented.redcastlemedia.bukkit.herostronghold;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Multitallented
 */
public class ConfigManager {
    
    private boolean explode;
    private final FileConfiguration config;
    private final HeroStronghold plugin;
    private final double maxTax;
    private final boolean destroyNoMoney;
    private final boolean destroyNoPower;
    private final boolean usePower;
    private final boolean useWar;
    private final double declareWarBase;
    private final double declareWarPer;
    private final double makePeaceBase;
    private final double makePeacePer;
    
    public ConfigManager(FileConfiguration config, HeroStronghold plugin) {
        this.config = config;
        this.plugin = plugin;
        
        //Parse region config data
        explode = config.getBoolean("explode-on-destroy", false);
        maxTax = config.getDouble("max-tax", 0.0); 
        destroyNoMoney = config.getBoolean("destroy-if-no-money");
        destroyNoPower = config.getBoolean("destory-if-no-power");
        usePower = config.getBoolean("use-power", true);
        useWar = config.getBoolean("war.use-war", false);
        declareWarBase = config.getDouble("war.declare-war-base-cost", 2000.0); 
        declareWarPer = config.getDouble("war.declare-war-cost-per-member", 500.0);
        makePeaceBase = config.getDouble("war.make-peace-base-cost", 1000.0);
        makePeacePer = config.getDouble("war.make-peace-cost-per-member", 500.0);
        loadCharters();
    }
    
    private void loadCharters() {
        Map<String, List<String>> charters = new HashMap<String, List<String>>();
        File charterFolder = new File(plugin.getDataFolder(), "charters");
        charterFolder.mkdirs();
        for (File charterFile : charterFolder.listFiles()) {
            FileConfiguration charterConfig = new YamlConfiguration();
            try {
                charterConfig.load(charterFile);
            } catch (Exception e) {
                plugin.warning("Failed to load charter " + charterFile.getName());
            }
            for (String key : charterConfig.getKeys(false)) {
                charters.put(key, charterConfig.getStringList(key));
                break;
            }
        }
        //send loaded charters for live use
        plugin.setCharters(charters);
    }
    
    public synchronized void writeToCharter(String name, List<String> data) {
        File charterFolder = new File(plugin.getDataFolder(), "charters");
        charterFolder.mkdirs();//Create the folder if it doesn't exist
        
        File charterData = new File(plugin.getDataFolder() + "/charters", name + ".yml");
        if (!charterData.exists()) {
            try {
                charterData.createNewFile();
            } catch (Exception e) {
                plugin.warning("Could not create new file " + name + ".yml");
                return;
            }
        }
        
        //Create the FileConfiguration to handle the new Charter
        FileConfiguration charterConfig = new YamlConfiguration();
        try {
            charterConfig.load(charterData);
        } catch (Exception e) {
            plugin.warning("Could not load charters.yml");
            return;
        }
        charterConfig.set(name, data);
    }
    
    public boolean getExplode() {
        return explode;
    }
    
    public double getMaxTax() {
        return maxTax;
    }
    
    public boolean getDestroyNoMoney() {
        return destroyNoMoney;
    }
    
    public boolean getDestroyNoPower() {
        return destroyNoPower;
    }
    
    public boolean getUsePower() {
        return usePower;
    }
    
    public boolean getUseWar() {
        return useWar;
    }
    
    public double getDeclareWarBase() {
        return declareWarBase;
    }
    
    public double getDeclareWarPer() {
        return declareWarPer;
    }
    
    public double getMakePeaceBase() {
        return makePeaceBase;
    }
    
    public double getMakePeacePer() {
        return makePeacePer;
    }
    
    public synchronized void removeCharter(String name) {
        File charter = new File(plugin.getDataFolder() + "/charters", name + ".yml");
        if (!charter.exists()) {
            plugin.warning("Unable to delete non-existent charter " + name + ".yml");
            return;
        }
        if (!charter.delete()) {
            plugin.warning("Unable to delete charter " + name + ".yml");
        }
    }
}
