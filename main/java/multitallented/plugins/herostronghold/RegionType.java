package main.java.multitallented.plugins.herostronghold;

import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionType {
    private String name;
    ArrayList<String> friendlyClasses = null;
    ArrayList<String> enemyClasses = null;
    ArrayList<String> effects = null;
    int radius = 0;
    ArrayList<ItemStack> requirements = null;
    ArrayList<ItemStack> reagents = null;
    ArrayList<ItemStack> upkeep = null;
    ArrayList<ItemStack> output = null;
    double upkeepChance = 0;
    double moneyRequirement = 0;
    double moneyOutput = 0;
    
    
    public RegionType(String name, ArrayList<String> friendlyClasses,
            ArrayList<String> enemyClasses, ArrayList<String> effects,
            int radius, ArrayList<ItemStack> requirements,
            ArrayList<ItemStack> reagents, ArrayList<ItemStack> upkeep,
            ArrayList<ItemStack> output, double upkeepChance,
            double moneyRequirement, double moneyOutput) {
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
    
    //TODO write more methods here for retrieving info
}
