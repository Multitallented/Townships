package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.util.ArrayList;
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
    
    
    public RegionType(String name, ArrayList<String> friendlyClasses,
            ArrayList<String> enemyClasses, ArrayList<String> effects,
            int radius, ArrayList<ItemStack> requirements,
            ArrayList<ItemStack> reagents, ArrayList<ItemStack> upkeep,
            ArrayList<ItemStack> output, double upkeepChance,
            double moneyRequirement, double moneyOutput, double exp) {
        this.name = name;
        this.friendlyClasses = friendlyClasses;
        this.enemyClasses = enemyClasses;
        this.effects = effects;
        this.radius = radius;
        this.requirements = requirements;
        this.reagents = reagents;
        this.upkeep = upkeep;
        this.output = output;
        this.upkeepChance = upkeepChance;
        this.moneyRequirement = moneyRequirement;
        this.moneyOutput = moneyOutput;
        this.exp = exp;
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
}
