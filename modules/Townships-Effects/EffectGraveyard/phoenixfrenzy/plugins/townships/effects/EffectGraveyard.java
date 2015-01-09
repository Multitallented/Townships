package phoenixfrenzy.plugins.townships.effects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import multitallented.redcastlemedia.bukkit.townships.Townships;
import multitallented.redcastlemedia.bukkit.townships.effect.Effect;
import multitallented.redcastlemedia.bukkit.townships.events.ToTwoSecondEffectEvent;
import multitallented.redcastlemedia.bukkit.townships.region.Region;
import multitallented.redcastlemedia.bukkit.townships.region.RegionManager;
import multitallented.redcastlemedia.bukkit.townships.region.RegionType;
import multitallented.redcastlemedia.bukkit.townships.region.SuperRegion;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import static org.bukkit.event.EventPriority.HIGH;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author Phoenix Frenzy
 * @author Multitallented
 */
public class EffectGraveyard extends Effect {
    public EffectGraveyard(Townships plugin) {
        super(plugin);
        registerEvent(new EffectGraveyard.UpkeepListener(this));
    }
    
    @Override
    public void init(Townships plugin) {
        super.init(plugin);
    }
    
    public class UpkeepListener implements Listener {
        private final EffectGraveyard effect;
        private RegionManager rm;
        
        public HashMap<String, Region> respawnLocations = new HashMap<String, Region>();
        
        public UpkeepListener(EffectGraveyard effect) {
            this.effect = effect;
            rm = effect.getPlugin().getRegionManager();
            
            for (Region graveyard : rm.getSortedRegions()) {
                RegionType rt = rm.getRegionType(graveyard.getType());
                if (rt == null) {
                    continue;
                }
                boolean hasEffect = false;
                ArrayList<String> effects = rt.getEffects();
                for (String s : effects) {
                    if (!s.startsWith("graveyard")) {
                        continue;
                    }
                    hasEffect = true;
                }
                if (!hasEffect) {
                    continue;
                }
                
                File regionFolder = new File(getPlugin().getDataFolder(), "data");
                File regionFile = new File(regionFolder, graveyard.getID() + ".yml");
                if (!regionFile.exists()) {
                    return;
                }
                
                FileConfiguration rConfig = new YamlConfiguration();
                try {
                    rConfig.load(regionFile);
                    
                    List<String> respawningPlayers = rConfig.getStringList("respawning-players");
                    for (String s : respawningPlayers) {
                        respawnLocations.put(s, graveyard);
                    }
                    
                } catch (Exception e) {
                    getPlugin().warning("[Townships] unable to read death locations in " + graveyard.getID() + ".yml");
                    return;
                }
            }
        }
        
        @EventHandler
        public void onJailTick(ToTwoSecondEffectEvent event) {
            
        }
        
        @EventHandler(priority=HIGH)
        public void onPlayerDeath(PlayerDeathEvent event) {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }
            Player player = (Player) event.getEntity();
            
            Location deathLocation = event.getEntity().getLocation();
            
            Region jail = null;
            boolean bypassJail = false;
            for (Region r : rm.getContainingBuildRegions(deathLocation)) {
                RegionType rt = rm.getRegionType(r.getType());
                if (rt == null) {
                    continue;
                }
                if (rt.getEffects().contains("jail")) {
                    bypassJail = true;
                    break;
                }
            }
            
            if (!bypassJail) {
                outer: for (SuperRegion sr : rm.getContainingSuperRegions(deathLocation)) {
                    for (Region r : rm.getContainedRegions(sr)) {
                        RegionType rt = rm.getRegionType(r.getType());
                        if (rt == null) {
                            continue;
                        }

                        if (rt.getEffects().contains("jail")) {
                            jail = r;
                            break outer;
                        }
                    }
                }
            }
            
            if (jail != null) {
                File regionFolder = new File(getPlugin().getDataFolder(), "data");
                File regionFile = new File(regionFolder, jail.getID() + ".yml");
                if (!regionFile.exists()) {
                    System.out.println("[Townships] graveyard data file " + jail.getID() + ".yml does not exist");
                    return;
                }

                FileConfiguration rConfig = new YamlConfiguration();
                try {
                    rConfig.load(regionFile);

                    List<String> respawningPlayers = rConfig.getStringList("respawning-players");
                    respawningPlayers.add(player.getName());
                    rConfig.set("respawning-players", respawningPlayers);
                    rConfig.save(regionFile);

                } catch (Exception e) {
                    getPlugin().warning("[Townships] unable to save death location in " + jail.getID() + ".yml");
                    return;
                }
                respawnLocations.put(player.getName(), jail);
                return;
            }
            
            Region graveyard = rm.getClosestRegionWithEffectAndTownMember(deathLocation, "graveyard", player);
            Region publicGraveyard = rm.getClosestRegionWithEffect(deathLocation, "graveyard_public");
            Region respawnRegion = null;
            if (graveyard == null && publicGraveyard != null) {
                respawnRegion = publicGraveyard;
            } else if (graveyard != null && publicGraveyard == null) {
                respawnRegion = graveyard;
            } else if (graveyard != null && publicGraveyard != null) {
                if (deathLocation.distanceSquared(graveyard.getLocation()) > deathLocation.distanceSquared(publicGraveyard.getLocation())) {
                    respawnRegion = publicGraveyard;
                } else {
                    respawnRegion = graveyard;
                }
            } else {
                System.out.println("[Townships] unable to find graveyard for " + player.getName());
                return;
            }
            
                
            File regionFolder = new File(getPlugin().getDataFolder(), "data");
            File regionFile = new File(regionFolder, respawnRegion.getID() + ".yml");
            if (!regionFile.exists()) {
                System.out.println("[Townships] graveyard data file " + respawnRegion.getID() + ".yml does not exist");
                return;
            }

            FileConfiguration rConfig = new YamlConfiguration();
            try {
                rConfig.load(regionFile);

                List<String> respawningPlayers = rConfig.getStringList("respawning-players");
                respawningPlayers.add(player.getName());
                rConfig.set("respawning-players", respawningPlayers);
                rConfig.save(regionFile);

            } catch (Exception e) {
                getPlugin().warning("[Townships] unable to save death location in " + respawnRegion.getID() + ".yml");
                return;
            }
            respawnLocations.put(player.getName(), respawnRegion);
        }
        
        @EventHandler(priority=HIGH)
        public void onPlayerRespawn(PlayerRespawnEvent event) {
            Player player = event.getPlayer();
            Region graveyard = respawnLocations.get(player.getName());
            if (graveyard == null) {
                System.out.println("[Townships] No graveyard found to respawn " + player.getName());
                return;
            }
            Location respawnHere = graveyard.getLocation().getBlock().getRelative(BlockFace.UP).getLocation();
            respawnHere.setX(Math.floor(respawnHere.getX()) + 0.5);
            respawnHere.setY(Math.floor(respawnHere.getY()) + 0.5);
            respawnHere.setZ(Math.floor(respawnHere.getZ()) + 0.5);
            
            event.setRespawnLocation(respawnHere);
            respawnLocations.remove(player.getName());
            
            
            File regionFolder = new File(getPlugin().getDataFolder(), "data");
            File regionFile = new File(regionFolder, graveyard.getID() + ".yml");
            if (!regionFile.exists()) {
                getPlugin().warning("[Townships] Unable to find " + graveyard.getID() + ".yml to remove death location");
                return;
            }

            FileConfiguration rConfig = new YamlConfiguration();
            try {
                rConfig.load(regionFile);

                List<String> respawningPlayers = rConfig.getStringList("respawning-players");
                respawningPlayers.remove(player.getName());
                rConfig.set("respawning-players", respawningPlayers);
                rConfig.save(regionFile);

            } catch (IOException | InvalidConfigurationException e) {
                getPlugin().warning("[Townships] unable to save removed death location in " + graveyard.getID() + ".yml");
            }
        }
    }
}