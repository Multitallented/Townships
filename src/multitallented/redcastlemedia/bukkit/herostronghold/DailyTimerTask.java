package multitallented.redcastlemedia.bukkit.herostronghold;

import java.util.HashSet;
import java.util.Set;
import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;

/**
 *
 * @author Multitallented
 */
public class DailyTimerTask implements Runnable {
    private final RegionManager rm;
    private final HeroStronghold plugin;
    public DailyTimerTask(HeroStronghold plugin) {
        this.plugin = plugin;
        this.rm = plugin.getRegionManager();
    }

    @Override
    public void run() {
        //TODO throw a new day event?
        Set<SuperRegion> destroyThese = new HashSet<SuperRegion>();
        Economy econ = HeroStronghold.econ;
        for (SuperRegion sr : rm.getSortedSuperRegions()) {
            if (econ != null) {
                double total = 0;
                double tax = sr.getTaxes();
                if (tax != 0) {
                    for (String member : sr.getMembers().keySet()) {
                        double balance = econ.getBalance(member);
                        if (!sr.getMember(member).contains("notax") && balance > 0) {
                            if (balance - tax < 0) {
                                econ.withdrawPlayer(member, balance);
                                total += balance;
                            } else {
                                econ.withdrawPlayer(member, tax);
                                total += tax;
                            }
                        }
                    }
                    rm.addTaxRevenue(sr, total);
                    double output = rm.getSuperRegionType(sr.getType()).getOutput();
                    total += output;
                    double newBalance = total + sr.getBalance();
                    if (newBalance < 0) {
                        destroyThese.add(sr);
                        final String st = sr.getName();
                        new Runnable() {
                              @Override
                              public void run() {
                                    plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " ran out of money!");
                              }
                        }.run();
                    } else
                        rm.addBalance(sr, total);
                    
                }
            }
            int power = sr.getPower();
            int maxPower = rm.getSuperRegionType(sr.getType()).getMaxPower();
            int dailyPower = rm.getSuperRegionType(sr.getType()).getDailyPower();
            if (power <= 0) {
                destroyThese.add(sr);
                final String st = sr.getName();
                new Runnable() {
                      @Override
                      public void run() {
                            plugin.getServer().broadcastMessage(ChatColor.RED + "[HeroStronghold] " + st + " has no power!");
                      }
                }.run();
            } else if (power >= maxPower) {
                //Dont need to do anything here apparently
            } else if (power + dailyPower > maxPower) {
                rm.setPower(sr, maxPower);
            } else {
                rm.setPower(sr, power + dailyPower);
            }
        }
        for (SuperRegion dsr : destroyThese) {
            rm.destroySuperRegion(dsr.getName(), true);
        }
    }
    
}
