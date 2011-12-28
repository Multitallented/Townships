package main.java.multitallented.plugins.herostronghold.region;

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
    public SuperRegionType(String name, List<String> effects, int radius, Map<String, Integer> requirements, double moneyRequirement, double output, List<String> children, int maxPower) {
        this.name=name;
        this.effects = effects;
        this.radius = radius;
        this.requirements = requirements;
        this.moneyRequirement = moneyRequirement;
        this.output = output;
        this.children = children;
        this.maxPower = maxPower;
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
    
    //TODO finish adding SuperRegionType get and set
}
