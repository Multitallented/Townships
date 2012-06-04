package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Multitallented
 */
public class SuperRegionType {
    private List<String> effects;
    private final int radius;
    private final Map<String, Integer> requirements;
    private final double moneyRequirement;
    private final double output;
    private final String name;
    private final List<String> children;
    private final int maxPower;
    private final int charter;
    private final int dailyPower;
    private final double exp;
    private final String centralStructure;
    private final int rawRadius;
    private final String description;
    public SuperRegionType(String name, List<String> effects, int radius, Map<String, Integer> requirements, double moneyRequirement, double output,
            List<String> children, int maxPower, int dailyPower, int charter, double exp, String centralStructure,
            String description) {
        this.name=name;
        this.effects = effects;
        this.radius = radius;
        this.rawRadius = (int) Math.sqrt(radius);
        this.requirements = requirements;
        this.moneyRequirement = moneyRequirement;
        this.output = output;
        this.children = children;
        this.maxPower = maxPower;
        this.dailyPower = dailyPower;
        this.charter = charter;
        this.exp = exp;
        this.centralStructure = centralStructure;
        this.description = description;
    }
    
    public int getRawRadius() {
        return rawRadius;
    }
    
    public String getCentralStructure() {
        return centralStructure;
    }
    
    public List<String> getEffects() {
        return effects;
    }
    
    public double getExp() {
        return exp;
    }
    
    public int getDailyPower() {
        return dailyPower;
    }
    
    public double getOutput() {
        return output;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean hasEffect(String input) {
        return effects.contains(input);
    }
    
    public boolean addEffect(String input) {
        return effects.add(input);
    }
    
    public double getMoneyRequirement() {
        return moneyRequirement;
    }
    
    public int getRadius() {
        return radius;
    }
    
    
    public Integer getRequirement(String name) {
        return requirements.get(name);
    }
    
    public Map<String, Integer> getRequirements() {
        return requirements;
    }
    
    public List<String> getChildren() {
        return children;
    }
    
    public int getMaxPower() {
        return maxPower;
    }
    
    public int getCharter() {
        return charter;
    }
    
    public String getDescription() {
        return description;
    }
}
