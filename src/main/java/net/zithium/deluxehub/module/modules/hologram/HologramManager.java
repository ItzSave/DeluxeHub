package net.zithium.deluxehub.module.modules.hologram;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import net.zithium.deluxehub.utility.reflection.ArmorStandName;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager extends Module {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();

    private Set<Hologram> holograms;
    private com.tcoded.folialib.wrapper.task.WrappedTask updateTask;

    public HologramManager(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.HOLOGRAMS);
    }

    @Override
    public void onEnable() {
        holograms = ConcurrentHashMap.newKeySet();
        loadHolograms();
        
        FileConfiguration config = getPlugin().getConfigManager().getFile(ConfigType.SETTINGS).getConfig();
        boolean refreshEnabled = config.getBoolean("holograms.refresh.enabled", true);
        long refreshRate = config.getLong("holograms.refresh.rate", 20L);
        
        if (refreshEnabled && refreshRate > 0) {
            updateTask = scheduler.runTimer(new HologramUpdateTask(this), refreshRate, refreshRate);
        }
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        removeAllHologramEntities();
    }

    public void loadHolograms() {
        scheduler.runLater(() -> {
            FileConfiguration config = getConfig(ConfigType.DATA);

            if (config.contains("holograms")) {
                for (String key : config.getConfigurationSection("holograms").getKeys(false)) {
                    List<String> lines = config.getStringList("holograms." + key + ".lines");

                    Location loc = config.getLocation("holograms." + key + ".location");
                    if (loc == null && config.isConfigurationSection("holograms." + key + ".location")) {
                        org.bukkit.configuration.ConfigurationSection sec = config.getConfigurationSection("holograms." + key + ".location");
                        World w = org.bukkit.Bukkit.getWorld(sec.getString("world", "world"));
                        if (w != null) {
                            loc = new Location(w, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z"), (float) sec.getDouble("yaw", 0.0), (float) sec.getDouble("pitch", 0.0));
                        }
                    }

                    if (loc == null) {
                        continue;
                    }

                    deleteNearbyHolograms(loc);

                    Hologram holo = createHologram(key, loc);
                    holo.setLines(lines);
                }
            }
        }, 40L);
    }

    public void saveHolograms() {
        saveHologramsData();
        deleteAllHolograms();
    }

    public void saveHologramsData() {
        FileConfiguration config = getConfig(ConfigType.DATA);
        holograms.forEach(hologram -> {
            config.set("holograms." + hologram.getName() + ".location", hologram.getLocation());
            config.set("holograms." + hologram.getName() + ".lines", hologram.getLines());
        });

        getPlugin().getConfigManager().getFile(ConfigType.DATA).save();
    }

    public Set<Hologram> getHolograms() {
        return holograms;
    }

    public boolean hasHologram(String name) {
        return getHolograms().stream().anyMatch(hologram -> hologram.getName().equalsIgnoreCase(name));
    }

    public Hologram getHologram(String name) {
        return getHolograms().stream().filter(hologram -> hologram.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Hologram createHologram(String name, Location location) {
        Hologram holo = new Hologram(getPlugin(), name, location);
        holograms.add(holo);
        return holo;
    }

    public void deleteHologram(String name) {
        Hologram holo = getHologram(name);
        holo.remove();

        holograms.remove(holo);
        getConfig(ConfigType.DATA).set("holograms." + name, null);
        getPlugin().getConfigManager().getFile(ConfigType.DATA).save();
    }

    public void deleteAllHolograms() {
        for (Hologram hologram : holograms) {
            hologram.remove();
        }

        holograms.clear();
    }

    @org.bukkit.event.EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        // Delay spawning slightly to ensure player is fully in world
        scheduler.runAtEntityLater(player, task -> {
            for (Hologram holo : holograms) {
                holo.spawnFor(player);
            }
        }, 10L);
    }

    @org.bukkit.event.EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        for (Hologram holo : holograms) {
            holo.removeFor(player.getUniqueId());
        }
    }

    private void removeAllHologramEntities() {
        for (Hologram hologram : holograms) {
            hologram.remove();
        }
    }

    public void deleteNearbyHolograms(Location location) {
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        scheduler.runAtLocation(location, task -> world.getNearbyEntities(location, 2.0, 20.0, 2.0).stream()
                .filter(entity -> entity instanceof ArmorStand)
                .filter(entity -> !entity.getScoreboardTags().contains("deluxehub_active"))
                .forEach(entity -> scheduler.runAtEntity(entity, nextTask -> entity.remove())));
    }
}
