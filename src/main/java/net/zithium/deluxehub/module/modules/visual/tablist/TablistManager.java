package net.zithium.deluxehub.module.modules.visual.tablist;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import net.zithium.deluxehub.utility.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class TablistManager extends Module {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();
    private List<UUID> players;
    private WrappedTask tablistTask;

    private String header, footer;
    private boolean playerNameFormatEnabled;
    private String playerNameFormat;

    public TablistManager(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.TABLIST);
    }

    @Override
    public void onEnable() {
        players = new CopyOnWriteArrayList<>();

        FileConfiguration config = getConfig(ConfigType.SETTINGS);

        header = String.join("\n", config.getStringList("tablist.header"));
        footer = String.join("\n", config.getStringList("tablist.footer"));

        playerNameFormatEnabled = config.getBoolean("tablist.player_name_format.enabled", false);
        playerNameFormat = config.getString("tablist.player_name_format.format", "%player%");

        if (config.getBoolean("tablist.refresh.enabled")) {
            tablistTask = scheduler.runTimer(new TablistUpdateTask(this), 1L, config.getLong("tablist.refresh.rate"));
        }

        scheduler.runLater(() -> 
                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> !inDisabledWorld(player.getLocation()))
                        .forEach(this::createTablist), 20L);
    }

    @Override
    public void onDisable() {
        if (tablistTask != null) {
            tablistTask.cancel();
        }

        Bukkit.getOnlinePlayers().forEach(this::removeTablist);
    }

    public void createTablist(Player player) {
        UUID uuid = player.getUniqueId();
        players.add(uuid);
        updateTablist(uuid);
    }

    public boolean updateTablist(UUID uuid) {
        if (!players.contains(uuid)) {
            return false;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return false;
        }

        TablistHelper.sendTabList(player, PlaceholderUtil.setPlaceholders(header, player), PlaceholderUtil.setPlaceholders(footer, player));

        // Update player name in tablist if enabled
        if (playerNameFormatEnabled) {
            TablistHelper.setPlayerListName(player, PlaceholderUtil.setPlaceholders(playerNameFormat, player));
        }

        return true;
    }

    public void removeTablist(Player player) {
        if (players.contains(player.getUniqueId())) {
            players.remove(player.getUniqueId());
            TablistHelper.sendTabList(player, null, null);

            // Reset player name in tablist if it was customized
            if (playerNameFormatEnabled) {
                TablistHelper.setPlayerListName(player, null);
            }
        }
    }

    public List<UUID> getPlayers() {
        return players;
    }

    /**
     * Updates all online players' tablists
     * Useful for reloading configuration
     */
    public void updateAllPlayers() {
        players.forEach(this::updateTablist);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!inDisabledWorld(player.getLocation())) {
            createTablist(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeTablist(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName())) {
            return;
        }

        if (inDisabledWorld(event.getTo().getWorld()) && players.contains(player.getUniqueId())) {
            removeTablist(player);
        } else {
            createTablist(player);
        }
    }
}
