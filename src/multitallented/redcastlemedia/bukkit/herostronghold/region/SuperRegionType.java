package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Multitallented
 */
public class SuperRegionType {
    private List<String> effects;
    private final int radius;
    private Map<String, Integer> requirements;
    private Map<String, Integer> maxRegions;
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
    private final int population;
    public SuperRegionType(String name, List<String> effects, int radius, List<String> requirements, double moneyRequirement, double output,
            List<String> children, int maxPower, int dailyPower, int charter, double exp, String centralStructure,
            String description, int population) {
        this.name=name;
        this.effects = effects;
        this.radius = radius;
        this.rawRadius = (int) Math.sqrt(radius);
        this.moneyRequirement = moneyRequirement;
        this.output = output;
        this.children = children;
        this.maxPower = maxPower;
        this.dailyPower = dailyPower;
        this.charter = charter;
        this.exp = exp;
        this.centralStructure = centralStructure;
        this.description = description;
        this.population = population;
        setRequirements(requirements);
    }
    
    private void setRequirements(List<String> reqs) {
        Map<String, Integer> reqMap = new HashMap<String, Integer>();
        Map<String, Integer> maxMap = new HashMap<String, Integer>();
        for (String s : reqs) {
            String[] args = s.split("\\.");
            if (args.length < 2) {
                continue;
            } else if (args.length < 3) {
                reqMap.put(args[0], Integer.parseInt(args[1]));
            } else {
                reqMap.put(args[0], Integer.parseInt(args[1]));
                maxMap.put(args[0], Integer.parseInt(args[2]));
            }
        }
        requirements = reqMap;
        maxRegions = maxMap;
    }
    
    public int getPopulation() {
        return population;
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
