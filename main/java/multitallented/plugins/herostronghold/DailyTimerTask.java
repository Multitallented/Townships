package main.java.multitallented.plugins.herostronghold;

import main.java.multitallented.plugins.herostronghold.region.RegionManager;
import main.java.multitallented.plugins.herostronghold.region.SuperRegion;
import net.milkbowl.vault.economy.Economy;

/**
 *
 * @author Multitallented
 */
public class DailyTimerTask implements Runnable {
    private final RegionManager rm;
    public DailyTimerTask(RegionManager rm) {
        this.rm = rm;
    }

    @Override
    public void run() {
        if (HeroStronghold.econ != null) {
            Economy econ = HeroStronghold.econ;
            for (SuperRegion sr : rm.getSortedSuperRegions()) {
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
                    //econ.
                }
            }
        }
        
        //TODO setup taxes
        //TODO setup power gain
        //TODO setup daily-output
    }
    
}
