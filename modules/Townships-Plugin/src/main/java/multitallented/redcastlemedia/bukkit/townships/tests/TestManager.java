package multitallented.redcastlemedia.bukkit.townships.tests;

import com.mojang.authlib.GameProfile;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.Util;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegionType;
import multitallented.redcastlemedia.bukkit.townships.region.TOItem;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PlayerInteractManager;
import net.minecraft.server.v1_11_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by multitallented on 3/7/17.
 */
public class TestManager {

    public TestManager(Townships townships, Logger log) {
        this.townships = townships;
        this.log = log;

        testUtil();
        testRegionManager();
    }

    private final Townships townships;
    private final Logger log;

    private void assertEqual(Object obj1, Object obj2, String failMessage) {
        if (!obj1.equals(obj2)) {
            log.warning("[Townships] Unit Test " + failMessage);
        }
    }
    private void assertLess(double obj1, double obj2, String failMessage) {
        if (!(obj1 < obj2)) {
            log.warning("[Townships] Unit Test " + failMessage);
        }
    }
    private void assertGreater(double obj1, double obj2, String failMessage) {
        if (!(obj1 > obj2)) {
            log.warning("[Townships] Unit Test " + failMessage);
        }
    }

    private void testUtil() {
        ArrayList<TOItem> tempList = new ArrayList<>();
        tempList.add(new TOItem(Material.COBBLESTONE, 4, 5));
        ArrayList<ArrayList<TOItem>> req = new ArrayList<>();
        req.add(tempList);
        Inventory inv = Bukkit.createInventory(null, 9);
        inv.addItem(new ItemStack(Material.COBBLESTONE, 5));
        assertEqual(Util.containsItems(req, inv), true, "Failed Util.containItems cobblestone");

        inv = Bukkit.createInventory(null, 9);
        inv.addItem(new ItemStack(Material.LOG, 5));
        assertEqual(Util.containsItems(req, inv), false, "Failed Util.containItems !cobblestone");
    }

    private CraftPlayer createFakePlayer(String name) {
        CraftServer cserver = (CraftServer) Bukkit.getServer();
        MinecraftServer mserver = cserver.getServer();
        WorldServer wserver = mserver.getWorldServer(0);
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        PlayerInteractManager pim = new PlayerInteractManager(wserver);
        return new CraftPlayer(cserver, new EntityPlayer(mserver, wserver, gameProfile, pim));
    }

    private void testRegionManager() {
        CraftPlayer player = createFakePlayer("tTownship1");
        CraftPlayer player2 = createFakePlayer("tTownship2");
        Bukkit.getWorlds().get(0).getBlockAt(0, 239, 0).setType(Material.COBBLESTONE);
        player.teleport(new Location(Bukkit.getWorlds().get(0), 0, 240, 0));
        player2.teleport(new Location(Bukkit.getWorlds().get(0), 0, 240, 0));
        player.setOp(true);
        player2.setOp(true);

        RegionManager rm = townships.getRegionManager();

        {
            ArrayList<ArrayList<TOItem>> tempList = new ArrayList<>();
            ArrayList<TOItem> temptempList = new ArrayList<>();
            temptempList.add(new TOItem(Material.COBBLESTONE, 4, 1));
            tempList.add(temptempList);
            rm.addRegionType(new RegionType("testregion",
                    "admin",
                    new ArrayList<String>(), //groups
                    5, //radius
                    5, //build radius
                    tempList, //requirements
                    new ArrayList<String>(), //super regions
                    new ArrayList<ArrayList<TOItem>>(), //reagents
                    new ArrayList<ArrayList<TOItem>>(), //upkeep
                    new ArrayList<ArrayList<TOItem>>(), //output
                    0.0, //moneyRequirement
                    0.0, //moneyOutput
                    0.0, //exp
                    "Description here", //description
                    0, //powerDrain
                    0, //housing
                    new ArrayList<String>(), //biome
                    new ItemStack(Material.COBBLESTONE, 1), //icon
                    0, //minY
                    999, //maxY
                    0, //unlockCost
                    0, //salvageValue
                    new HashMap<String, ArrayList<String>>() //namedItems
            ));
        }


        player.performCommand("to create testregion");
        assertEqual(rm.getRegionByID(0).getType(), "testregion", "Failed create region");

        player.performCommand("to add tTownship2");
        assertEqual(rm.getRegionByID(0).isMember("tTownship2"), true, "Failed add player");

        player.performCommand("to addowner tTownship2");
        assertEqual(rm.getRegionByID(0).isOwner("tTownship2"), true, "Failed addowner player");

        player.performCommand("to setowner tTownship2");
        assertEqual(rm.getRegionByID(0).isPrimaryOwner("tTownship2"), true, "Failed setowner player2");
        assertEqual(rm.getRegionByID(0).isMember("tTownship2"), false, "Failed setowner member player2");
        assertEqual(rm.getRegionByID(0).isOwner("tTownship1"), true, "Failed setowner player1");
        assertEqual(rm.getRegionByID(0).isMember("tTownship1"), false, "Failed setowner member player1");

        {
            ArrayList<String> tempReqs = new ArrayList<>();
            tempReqs.add("testregion");

            rm.addSuperRegionType(new SuperRegionType("testsuper",
                    new ArrayList<String>(), //effects
                    10, //radius
                    tempReqs, //requirements
                    0.0, //moneyRequirement
                    0.0, //output
                    new ArrayList<String>(), //children
                    10, //maxPower
                    10, //dailyPower
                    0, //charter
                    0, //exp
                    "testregion", //centralStructure
                    "Description test super", //description
                    0, //population
                    new ItemStack(Material.COBBLESTONE, 2), //icon
                    new ArrayList<String>(), //limits
                    1.0 //unlockCost
            ));
        }

        player2.performCommand("to create testsuper TestSuper");
        assertEqual(rm.getSuperRegion("TestSuper").getType(), "testsuper", "Failed create super");

        player2.performCommand("to add tTownship1 TestSuper");
        player.performCommand("to accept TestSuper");
        assertEqual(rm.getSuperRegion("TestSuper").hasMember("tTownship1"), true, "Failed add super");

        //TODO add more tests

        player2.performCommand("to destroy TestSuper");
        player2.performCommand("to destroy");
        rm.removeRegionType("testregion");
        rm.removeSuperRegionType("testsuper");

        Bukkit.getWorlds().get(0).getBlockAt(0, 239, 0).setType(Material.AIR);
        player.setOp(false);
        player2.setOp(false);
        player.remove();
        player2.remove();
    }
}
