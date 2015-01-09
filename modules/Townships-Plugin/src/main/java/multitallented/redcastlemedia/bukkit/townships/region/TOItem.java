package multitallented.redcastlemedia.bukkit.townships.region;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Multi
 */
public class TOItem {
    private Material mat;
    private final int id;
    private final int damage;
    private int qty;
    private final double chance;
    private final boolean wildDamage;
    private String displayName = null;
    private ArrayList<String> lore = new ArrayList<String>();

    public TOItem(Material mat, int id, int qty, int damage, int chance, String displayName, ArrayList<String> lore) {
        this.mat = mat;
        this.id = id;
        this.damage = damage;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        wildDamage = damage == -1;
        this.displayName = displayName;
        this.lore = lore;
    }

    public TOItem(Material mat, int id, int qty, int damage, int chance) {
        this.mat = mat;
        this.id = id;
        this.damage = damage;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        wildDamage = damage == -1;
    }
    
    public TOItem(Material mat, int id, int qty, int damage) {
        this.mat = mat;
        this.id = id;
        this.damage = damage;
        this.qty = qty;
        this.chance = 1;
        wildDamage = damage == -1;
    }
    public TOItem(Material mat, int id, int qty) {
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

    public boolean equivalentItem(ItemStack iss) {
        return equivalentItem(iss, false);
    }

    public static TOItem createFromItemStack(ItemStack is) {
        if (is.hasItemMeta() && !is.getItemMeta().getDisplayName().equals("")) {
            return new TOItem(is.getType(),is.getTypeId(),is.getAmount(), is.getDurability(), 100, is.getItemMeta().getDisplayName(), (ArrayList<String>) is.getItemMeta().getLore());
        }
        if (is.getDurability() > 0) {
            return new TOItem(is.getType(),is.getTypeId(),is.getAmount(), is.getDurability());
        }
        return new TOItem(is.getType(),is.getTypeId(),is.getAmount());
    }
    
    public boolean equivalentItem(ItemStack iss, boolean useDisplayName) {
        if (useDisplayName) {
            boolean nullComparison = getDisplayName() == null;
            boolean nullName = !iss.hasItemMeta() || iss.getItemMeta().getDisplayName() == null;
            
            boolean equivalentNames = (nullComparison && nullName) || ((!nullComparison && !nullName) && iss.getItemMeta().getDisplayName().equals(getDisplayName()));
            
            return iss.getType() == getMat() &&
                    (isWildDamage() || getDamage() == (int) (iss.getDurability())) &&
                    equivalentNames;
        } else {
            return iss.getType() == getMat() &&
                (isWildDamage() || getDamage() == (int) (iss.getDurability()));
        }
    }

    public boolean equivalentTOItem(TOItem iss) {
        return equivalentTOItem(iss, false);
    }

    public boolean equivalentTOItem(TOItem iss, boolean useDisplayName) {
        if (useDisplayName) {
            return iss.getMat() == getMat() &&
                    (isWildDamage() || iss.isWildDamage() || getDamage() == iss.getDamage()) &&
                    ((getDisplayName() == null && iss.getDisplayName() == null) || getDisplayName().equals(iss.getDisplayName()));
        } else {
            return iss.getMat() == getMat() &&
                    (isWildDamage() || iss.isWildDamage() || getDamage() == iss.getDamage());
        }
    }

    public ArrayList<String> getLore() {
        return lore;
    }

    public String getDisplayName() {
            return displayName;
    }

    public void setMat(Material mat) {
        this.mat = mat;
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
    public TOItem clone() {
        return new TOItem(mat, id, qty, damage, (int) chance, displayName, lore);
    }
}
