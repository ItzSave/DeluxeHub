package net.zithium.deluxehub.module.modules.world;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import net.zithium.deluxehub.utility.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LobbySpawn extends Module {

    private boolean spawnJoin;
    private Location location = null;
    private final DeluxeHubPlugin plugin;
    private final PlatformScheduler scheduler;

    public LobbySpawn(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.LOBBY);

        this.plugin = plugin;
        this.scheduler = DeluxeHubPlugin.scheduler();
    }

    @Override
    public void onEnable() {
        scheduler.runNextTick(task -> {
            FileConfiguration config = getConfig(ConfigType.DATA);
            if (config.contains("spawn")) {
                location = (Location) config.get("spawn");
            }
        });

        spawnJoin = getConfig(ConfigType.SETTINGS).getBoolean("join_settings.spawn_join", false);
    }

    @Override
    public void onDisable() {
        // Saving on disable is not needed and overwrites manual config edits.
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (spawnJoin && location != null) {
            TeleportUtil.teleportCompat(player, location);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (location != null && !inDisabledWorld(player.getLocation())) {
            event.setRespawnLocation(location);
        }
    }
}
