package multitallented.redcastlemedia.bukkit.herostronghold.region;

import java.util.logging.Logger;
import org.bukkit.Material;

/**
 *
 * @author Multi
 */
public class HSItem {
    private final Material mat;
    private final int id;
    private final int damage;
    private int qty;
    private final double chance;
    private final boolean wildDamage;
    
    public HSItem(Material mat, int id, int qty, int damage, int chance) {
        this.mat = mat;
        this.id = id;
        this.damage = damage;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        wildDamage = damage == -1;
    }
    
    public HSItem(Material mat, int id, int qty, int damage) {
        this.mat = mat;
        this.id = id;
        this.damage = damage;
        this.qty = qty;
        this.chance = 1;
        wildDamage = damage == -1;
    }
    public HSItem(Material mat, int id, int qty) {
        this.mat = mat;
        this.id = id;
        this.damage = -1;
        this.qty = qty;
        this.chance = 1;
        wildDamage = true;
    }
    
    public boolean damageMatches(short durability) {
        int dur = (int) durability;
        if (dur == damage) {
            return true;
        }
        if ((id == 17 || id == 162) && ((damage + 4) == dur || (damage + 8) == dur || (damage + 12) == dur)) {
            return true;
        }
        return false;
    }
    
    public Material getMat() {
        return mat;
    }
    public int getID() {
        return id;
    }
    public int getDamage() {
        return damage;
    }
    public int getQty() {
        return qty;
    }
    public boolean isWildDamage() {
        return wildDamage;
    }
    public double getChance() {
        return chance;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    
    @Override
    public HSItem clone() {
        return new HSItem(mat, id, qty, damage, (int) chance);
    }
}
