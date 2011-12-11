package main.java.multitallented.plugins.herostronghold;

/**
 *
 * @author Multitallented
 */
public class Effect {
    private final HeroStronghold plugin;
    private final String name;
    
    public Effect(HeroStronghold plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
