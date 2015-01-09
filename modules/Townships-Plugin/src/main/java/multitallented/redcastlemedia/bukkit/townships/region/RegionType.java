package multitallented.redcastlemedia.bukkit.townships.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multitallented
 */
public class RegionType {
    private String name;

    private final double radius;
    private final double buildRadius;
    private final double rawBuildRadius;
    private final double rawRadius;

    private final ArrayList<ArrayList<TOItem>> requirements;
    private final ArrayList<ArrayList<TOItem>> reagents;
    private final ArrayList<ArrayList<TOItem>> input;
    private final ArrayList<ArrayList<TOItem>> output;
    private final ArrayList<String> effects;

    private final double moneyRequirement;
    private final double moneyOutput;
    private final double exp;
    private final List<String> superRegions;
    private final String description;
    private final String group;
    private final int powerDrain;
    private final int housing;
    private final List<String> biome;
    private final ItemStack icon;
    private final int minY;
    private final int maxY;
    private final double unlockCost;
    private final HashMap<String, ArrayList<String>> namedItems;
    private final double salvageValue;
    
    
    public RegionType(String name, String group, ArrayList<String> effects,
            double radius, double buildRadius, ArrayList<ArrayList<TOItem>> requirements, List<String> superRegions,
            ArrayList<ArrayList<TOItem>> reagents, ArrayList<ArrayList<TOItem>> upkeep,
            ArrayList<ArrayList<TOItem>> output, double moneyRequirement, double moneyOutput, double exp,
            String description, int powerDrain,
            int housing, List<String> biome, ItemStack icon, int minY, int maxY, double unlockCost,
            double salvageValue, HashMap<String, ArrayList<String>> namedItems) {
        this.name = name;
        this.group = group;
        this.effects = effects;
        this.radius = radius;
        this.rawRadius = Math.sqrt(this.radius);
        this.buildRadius = buildRadius;
        this.rawBuildRadius = Math.sqrt(this.buildRadius);
        this.requirements = requirements;
        this.superRegions = superRegions;
        this.reagents = reagents;
        this.input = upkeep;
        this.output = output;
        this.moneyRequirement = moneyRequirement;
        this.moneyOutput = moneyOutput;
        this.exp = exp;
        this.description = description;
        this.powerDrain = powerDrain;
        this.housing = housing;
        this.biome = biome;
        this.icon = icon;
        this.minY = minY;
        this.maxY = maxY;
        this.unlockCost = unlockCost;
        this.namedItems = namedItems;
	    this.salvageValue = salvageValue;
    }

	public double getSalvage() {
		return salvageValue;
	}
    
    public HashMap<String, ArrayList<String>> getNamedItems() {
        return namedItems;
    }
    
    public double getUnlockCost() {
        return unlockCost;
    }
    public int getMinY() {
            return minY;
    }
    public int getMaxY() {
            return maxY;
    }
    public ItemStack getIcon() {
        return icon;
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
    
    public double getRawRadius() {
        return rawRadius;
    }
    
    public double getRawBuildRadius() {
        return rawBuildRadius;
    }
    
    public double getBuildRadius() {
        return buildRadius;
    }

	public double getRadius() {
		return radius;
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
    
    public ArrayList<ArrayList<TOItem>> getReagents() {
        return reagents;
    }
    
    public ArrayList<ArrayList<TOItem>> getRequirements() {
        return requirements;
    }
    
    public double getMoneyRequirement() {
        return moneyRequirement;
    }
    
    public ArrayList<String> getEffects() {
        return effects;
    }
    
    public ArrayList<ArrayList<TOItem>> getUpkeep() {
        return input;
    }
    
    public double getMoneyOutput() {
        return moneyOutput;
    }
    
    public ArrayList<ArrayList<TOItem>> getOutput() {
        return output;
    }

    public String getDescription() {
        return this.description;
    }
}
