package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionType {
    private String name;
    private final ArrayList<String> friendlyClasses;
    private final ArrayList<String> enemyClasses;
    private final ArrayList<String> effects;
    private final int radius;
    private final ArrayList<ItemStack> requirements;
    private final ArrayList<ItemStack> reagents;
    private final ArrayList<ItemStack> upkeep;
    private final ArrayList<ItemStack> output;
    private final double upkeepChance;
    private final double moneyRequirement;
    private final double moneyOutput;
    private final double exp;
    private final List<String> superRegions;
    private final int buildRadius;
    private final int rawBuildRadius;
    private final int rawRadius;
    private final String description;
    private final String group;
    private final int powerDrain;
    private final int housing;
    private final List<String> biome;
    
    
    public RegionType(String name, String group, ArrayList<String> friendlyClasses,
            ArrayList<String> enemyClasses, ArrayList<String> effects,
            int radius, int buildRadius, ArrayList<ItemStack> requirements, List<String> superRegions,
            ArrayList<ItemStack> reagents, ArrayList<ItemStack> upkeep,
            ArrayList<ItemStack> output, double upkeepChance,
            double moneyRequirement, double moneyOutput, double exp,
            String description, int powerDrain,
            int housing, List<String> biome) {
        this.name = name;
        this.group = group;
        this.friendlyClasses = friendlyClasses;
        this.enemyClasses = enemyClasses;
        this.effects = effects;
        this.radius = radius;
        this.rawRadius = (int) Math.sqrt(radius);
        this.rawBuildRadius = (int) Math.sqrt(buildRadius);
        this.buildRadius = buildRadius;
        this.requirements = requirements;
        this.superRegions = superRegions;
        this.reagents = reagents;
        this.upkeep = upkeep;
        this.output = output;
        this.upkeepChance = upkeepChance;
        this.moneyRequirement = moneyRequirement;
        this.moneyOutput = moneyOutput;
        this.exp = exp;
        this.description = description;
        this.powerDrain = powerDrain;
        this.housing = housing;
        this.biome = biome;
    }
    public List<String> getBiome() {
        return biome;
    }
    public int getHousing() {
        return housing;
    }
    
    public int getPowerDrain() {
        return powerDrain;
    }
    
    public String getGroup() {
        return group;
    }
    
    public int getRawRadius() {
        return rawRadius;
    }
    
    public int getRawBuildRadius() {
        return rawBuildRadius;
    }
    
    public int getBuildRadius() {
        return buildRadius;
    }
    
    public List<String> getSuperRegions() {
        return superRegions;
    }
    
    public double getExp() {
        return exp;
    }
    
    public String getName() {
        return name;
    }
    
    public ArrayList<ItemStack> getReagents() {
        return reagents;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public ArrayList<ItemStack> getRequirements() {
        return requirements;
    }
    
    public double getMoneyRequirement() {
        return moneyRequirement;
    }
    
    public double getUpkeepChance() {
        return upkeepChance;
    }
    
    public ArrayList<String> getEffects() {
        return effects;
    }
    
    public ArrayList<ItemStack> getUpkeep() {
        return upkeep;
    }
    
    public double getMoneyOutput() {
        return moneyOutput;
    }
    
    public ArrayList<ItemStack> getOutput() {
        return output;
    }
    
    public boolean containsFriendlyClass(String name) {
        return this.friendlyClasses.contains(name);
    }
    
    public boolean containsEnemyClass(String name) {
        return this.enemyClasses.contains(name);
    }
    
    public String getDescription() {
        return this.description;
    }
}
