package multitallented.redcastlemedia.bukkit.herostronghold.region;

/**
 *
 * @author Multitallented
 */
public class RegionCondition {
    public final String NAME;
    public final boolean USE_REAGENTS;
    public final int MODIFIER;
    public RegionCondition(String name, boolean useReagents, int modifier) {
        NAME = name;
        USE_REAGENTS = useReagents;
        MODIFIER = modifier;
    }
}
