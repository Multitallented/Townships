/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Multitallented
 */
public class DefaultRegions {
    public static void createDefaultRegionFiles(HeroStronghold plugin) {
        File folder = new File(plugin.getDataFolder(), "RegionConfig");
        try {
            folder.mkdirs();
            File shackFile = new File(folder, "shack.yml");
            shackFile.mkdirs();
            shackFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.load(shackFile);
            ArrayList<String> tempList = new ArrayList<String>();
            tempList.add("denyblockbuildnoreagent.1");
            tempList.add("denyexplosion.1");
            tempList.add("denyblockbreak.1");
            tempList.add("denyplayerinteract.1");
            tempList.add("scheduledupkeep.7200");
            tempList.add("denybucketusenoreagent.1");
            tempList.add("denyfirenoreagent.1");
            tempList.add("denydamage.1");
            config.set("effects", tempList);
            tempList.clear();
            config.set("radius", 4);
            tempList.add("WOODEN_DOOR.1");
            tempList.add("THIN_GLASS.3");
            tempList.add("BOOKSHELF.1");
            tempList.add("BED_BLOCK.1");
            tempList.add("FURNACE.1");
            tempList.add("WORKBENCH.1");
            tempList.add("WALL_SIGN.1");
            tempList.add("WOOD.24");
            tempList.add("COBBLESTONE.12");
            tempList.add("WOOL.1");
            tempList.add("CHEST.2");
            config.set("requirements", tempList);
            tempList.clear();
            tempList.add("BREAD.2");
            tempList.add("IRON_INGOT.1");
            tempList.add("DIAMOND.1");
            config.set("reagents", tempList);
            tempList.clear();
            tempList.add("BREAD.2");
            config.set("upkeep", tempList);
            tempList.clear();
            tempList.add("DIRT.1");
            config.set("output", tempList);
            config.set("upkeep-chance", 1.0);
            tempList.clear();
            tempList.add("ward");
            tempList.add("village");
            config.set("super-regions", tempList);
            config.set("money-requirement", 500.0);
            config.set("upkeep-money-output", 50.0);
            config.save(shackFile);
        } catch (Exception e) {
            
        }
        try {
            File shackFile = new File(folder, "mancannon.yml");
            shackFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.load(shackFile);
            ArrayList<String> tempList = new ArrayList<String>();
            tempList.add("mancannon.3");
            config.set("effects",tempList);
            tempList.clear();
            config.set("radius", 2);
            tempList.add("POWERED_RAIL.3");
            tempList.add("PISTON_BASE.1");
            config.set("requirements", tempList);
            tempList.clear();
            tempList.add("REDSTONE.16");
            config.set("reagents",tempList);
            config.set("upkeep-chance", 0.0);
            config.set("money-requirement", 200.0);
            config.save(shackFile);
        } catch (Exception e) {
            
        }
        try {
            File shackFile = new File(folder, "arrowturret.yml");
            shackFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.load(shackFile);
            ArrayList<String> tempList = new ArrayList<String>();
            tempList.add("shootarrow.2");
            tempList.add("denyblockbuild.1");
            config.set("effects",tempList);
            tempList.clear();
            config.set("radius", 20);
            config.set("build-radius", 3);
            tempList.add("DISPENSER.1");
            tempList.add("FENCE.5");
            tempList.add("REDSTONE_WIRE.4");
            config.set("requirements", tempList);
            tempList.clear();
            tempList.add("REDSTONE.4");
            tempList.add("ARROW.1");
            config.set("reagents",tempList);
            tempList.clear();
            tempList.add("ARROW.1");
            config.set("upkeep", tempList);
            config.set("upkeep-chance", 0.0);
            config.set("money-requirement", 1500.0);
            config.set("exp", 200);
            config.save(shackFile);
        } catch (Exception e) {
            
        }
    }
    
    public static void createDefaultSuperRegionFiles(HeroStronghold plugin) {
        File folder = new File(plugin.getDataFolder(), "SuperRegionConfig");
        try {
            folder.mkdirs();
            File shackFile = new File(folder, "ward.yml");
            shackFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.load(shackFile);
            ArrayList<String> tempList = new ArrayList<String>();
            tempList.add("denyblockbuild");
            tempList.add("denyexplosion");
            tempList.add("denyblockbreak");
            tempList.add("denybucketuse");
            tempList.add("denyfirenoreagent");
            tempList.add("denyfriendlyfire");
            config.set("effects", tempList);
            tempList.clear();
            config.set("radius", 50);
            tempList.add("arrowturret.2");
            config.set("requirements", tempList);
            config.set("money-requirement", 5000.0);
            config.set("money-output-daily", -100.0);
            config.set("max-power", 40);
            config.set("daily-power-increase", 4);
            config.set("exp", 500);
            config.save(shackFile);
        } catch (Exception e) {
            
        }
        try {
            File shackFile = new File(folder, "village.yml");
            shackFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.load(shackFile);
            ArrayList<String> tempList = new ArrayList<String>();
            tempList.add("denyblockbuild");
            tempList.add("denyexplosion");
            tempList.add("denyblockbreak");
            tempList.add("denybucketuse");
            tempList.add("denyfirenoreagent");
            tempList.add("denypvp");
            config.set("effects", tempList);
            tempList.clear();
            config.set("radius", 50);
            tempList.add("arrowturret.2");
            tempList.add("shack.1");
            config.set("requirements", tempList);
            config.set("central-structure", "shack");
            config.set("money-requirement", 10000.0);
            config.set("money-output-daily", -1000.0);
            config.set("max-power", 400);
            config.set("daily-power-increase", 40);
            config.set("charter", 4);
            tempList.clear();
            tempList.add("ward");
            config.set("children", tempList);
            config.set("exp", 5000);
            config.save(shackFile);
        } catch (Exception e) {
            
        }
    }

    public static void migrateRegions(File regionFile, HeroStronghold plugin) {
        File folder = new File(plugin.getDataFolder(), "RegionConfig");
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(regionFile);
            Set<String> keys = config.getKeys(false);
            folder.mkdirs();
            for (String s : keys) {
                FileConfiguration currentConfig = new YamlConfiguration();
                ConfigurationSection cs = config.getConfigurationSection(s);
                File file = new File(folder, s + ".yml");
                file.createNewFile();
                currentConfig.load(file);
                for (String s0 : cs.getKeys(false)) {
                    currentConfig.set(s0, cs.get(s0));
                }
                currentConfig.save(file);
            }
        } catch (Exception e) {
            System.out.println("[HeroStronghold] failed to migrate regions.yml");
        }
    }
    
    public static void migrateSuperRegions(File sRegionFile, HeroStronghold plugin) {
        File folder = new File(plugin.getDataFolder(), "SuperRegionConfig");
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sRegionFile);
            Set<String> keys = config.getKeys(false);
            folder.mkdirs();
            for (String s : keys) {
                FileConfiguration currentConfig = new YamlConfiguration();
                ConfigurationSection cs = config.getConfigurationSection(s);
                File file = new File(folder, s + ".yml");
                file.createNewFile();
                currentConfig.load(file);
                for (String s0 : cs.getKeys(false)) {
                    currentConfig.set(s0, cs.get(s0));
                }
                currentConfig.save(file);
            }
        } catch (Exception e) {
            System.out.println("[HeroStronghold] failed to migrate super-regions.yml");
        }
    }
}
