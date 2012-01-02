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
    /*private final Set<Material> matBreakBlackList;
    private final Set<Material> matPlaceBlackList;
    private final Set<Material> matNotifyBreakBlackList;
    private final Set<Material> matNotifyPlaceBlackList;
    private final boolean denyTNTBlockDamage;
    private final boolean denyTNTDamage;
    private final boolean denyFlintSteelUse;
    private final boolean denyFireSpread;
    private final boolean disableLavaSpread;
    private final boolean disableWaterSpread;
    private final boolean denyDrowning;
    private final boolean teleportVoid;
    private final boolean denyVoidDamage;
    private final boolean denyFireDamage;
    private final boolean denyLavaDamage;
    private final boolean denyMobDamage;
    private final boolean denyFallDamage;
    private final boolean denyExplosionDamage;
    private final boolean denySuffocationDamage;
    private final boolean denyContactDamage;
    private final boolean denyLightningDamage;
    private final boolean denyCreeperBlockDamage;
    private final boolean denyCreeperSpawn;
    private final boolean denyGhastBlockDestruction;
    private final boolean denyGhastSpawn;
    private final boolean denyEndermanTakePlace;
    private final boolean denyEndermanSpawning;
    private final boolean denyZombieSpawn;
    private final boolean denySkeletonSpawn;
    private final boolean denySpiderSpawn;
    private final boolean denyCaveSpiderSpawn;
    private final boolean denySlimeSpawn;
    private final boolean denyMagmaCubeSpawn;
    private final boolean denyBlazeSpawn;
    private final boolean denyZombiePigmenSpawn;
    private final boolean denySnowmanSpawn;
    private final boolean denyMooshroomSpawn;
    private final boolean denySheepSpawn;
    private final boolean denyCowSpawn;
    private final boolean denyChickenSpawn;
    private final boolean denySquidSpawn;
    private final boolean denyPigSpawn;
    private final boolean denySilverfishSpawn;
    private final boolean denyWolfSpawn;
    private final boolean denyWolfTame;
    private final boolean denyEnderDragonSpawn;
    private final boolean denyFriendlyFire;
    private final boolean denyPvP;*/
    
    public ConfigManager(FileConfiguration config, HeroStronghold plugin) {
        this.config = config;
        this.plugin = plugin;
        
        //Parse region config data
        explode = config.getBoolean("explode-on-destroy", false);
        /*matBreakBlackList = processMatSet(config.getStringList("block-break-blacklist"));
        matPlaceBlackList = processMatSet(config.getStringList("block-place-blacklist"));
        matNotifyBreakBlackList = processMatSet(config.getStringList("block-notify-on-break"));
        matNotifyPlaceBlackList = processMatSet(config.getStringList("block-notify-on-place"));
        denyTNTBlockDamage = config.getBoolean("deny-tnt-block-damage", true);
        denyTNTDamage = config.getBoolean("deny-tnt-damage", false);
        denyFlintSteelUse = config.getBoolean("deny-flint-steel-use", true);
        denyFireSpread = config.getBoolean("deny-firespread", true);
        disableLavaSpread = config.getBoolean("deny-lavaspread", false);
        disableWaterSpread = config.getBoolean("disable-waterspread", false);
        
        teleportVoid = config.getBoolean("player-damage.teleport-to-sky-on-void-falling", false);
        denyDrowning = config.getBoolean("player-damage.deny-drowning-damage", false);
        denyVoidDamage = config.getBoolean("player-damage.deny-void-damage", false);
        denyFireDamage = config.getBoolean("player-damage.deny-fire-damage", false);
        denyLavaDamage = config.getBoolean("player-damage.deny-lava-damage", false);
        denyMobDamage = config.getBoolean("player-damage.deny-mob-damage", false);
        denyFallDamage = config.getBoolean("player-damage.deny-fall-damage", false);
        denyExplosionDamage = config.getBoolean("player-damage.deny-explosion-damage", false);
        denySuffocationDamage = config.getBoolean("player-damage.deny-suffocation-damage", false);
        denyContactDamage = config.getBoolean("player-damage.deny-suffocation-damage", false);
        denyLightningDamage = config.getBoolean("player-damage.deny-lightning-damage", false);
        denyFriendlyFire = config.getBoolean("player-damage.deny-friendly-fire", false);
        denyPvP = config.getBoolean("player-damage.deny-pvp", false);
        
        denyCreeperBlockDamage = config.getBoolean("creatures.creepers.deny-block-destruction", true);
        denyCreeperSpawn = config.getBoolean("creatures.creepers.deny-spawn", false);
        denyGhastBlockDestruction = config.getBoolean("creatures.ghast.deny-block-damage", false);
        denyGhastSpawn = config.getBoolean("creatures.ghast.deny-spawn", false);
        denyEndermanTakePlace = config.getBoolean("creatures.enderman.deny-block-take-and-place", true);
        denyEndermanSpawning = config.getBoolean("creatures.enderman.deny-spawn", false);
        denyZombieSpawn = config.getBoolean("creatures.zombie.deny-spawn", false);
        denySkeletonSpawn = config.getBoolean("creatures.skeleton.deny-spawn", false);
        denySpiderSpawn = config.getBoolean("creatures.spider.deny-spawn", false);
        denyCaveSpiderSpawn = config.getBoolean("creatures.cave-spider.deny-spawn", false);
        denySlimeSpawn = config.getBoolean("creatures.slime.deny-spawn", false);
        denyMagmaCubeSpawn = config.getBoolean("creatures.magma-cube.deny-spawn", false);
        denyBlazeSpawn = config.getBoolean("creatures.blaze.deny-spawn", false);
        denyZombiePigmenSpawn = config.getBoolean("creatures.zombie-pigmen.deny-spawn", false);
        denySnowmanSpawn = config.getBoolean("creatures.snowman.deny-spawn", false);
        denyMooshroomSpawn = config.getBoolean("creatures.mooshroom.deny-spawn", false);
        denySheepSpawn = config.getBoolean("creatures.sheep.deny-spawn", false);
        denyCowSpawn = config.getBoolean("creatures.cow.deny-spawn", false);
        denyChickenSpawn = config.getBoolean("creatures.chicken.deny-spawn", false);
        denySquidSpawn = config.getBoolean("creatures.squid.deny-spawn", false);
        denyPigSpawn = config.getBoolean("creatures.pig.deny-spawn", false);
        denySilverfishSpawn = config.getBoolean("creatures.silverfish.deny-spawn", false);
        denyWolfSpawn = config.getBoolean("creatures.wolf.deny-spawn", false);
        denyWolfTame = config.getBoolean("creatures.wolf.deny-tame", false);
        denyEnderDragonSpawn = config.getBoolean("creatures.enderdragon.deny-spawn", false);*/
        
        loadCharters();
    }
    
    /*private Set<Material> processMatSet(List<String> input) {
        Set<Material> tempSet = new HashSet<Material>();
        try {
            for (String s : input) {
                Material in = Material.getMaterial(s);
                if (in != null)
                    tempSet.add(in);
            }
        } catch (NullPointerException npe) {
            
        }
        return tempSet;
    }*/
    //TODO fix this loading charters
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
            String key = charterFile.getName().replace(".yml", "");
            charters.put(key, charterConfig.getStringList(key));
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
