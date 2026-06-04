package net.zithium.deluxehub.module.modules.visual.scoreboard;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager extends Module {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();
    private WrappedTask scoreTask;
    private final Map<UUID, ScoreHelper> players = new ConcurrentHashMap<>();
    private final Set<UUID> toggledPlayers = ConcurrentHashMap.newKeySet();

    private long joinDelay;
    private long worldDelay;
    private String title;
    private List<String> lines;

    public ScoreboardManager(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.SCOREBOARD);
    }

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig(ConfigType.SETTINGS);

        title = config.getString("scoreboard.title");
        lines = config.getStringList("scoreboard.lines");

        joinDelay = config.getLong("scoreboard.display_delay.server_enter", 1L);
        worldDelay = config.getLong("scoreboard.display_delay.world_change", 1L);

        if (config.getBoolean("scoreboard.refresh.enabled")) {
            scoreTask = scheduler.runTimer(new ScoreUpdateTask(this), 1L, config.getLong("scoreboard.refresh.rate"));
        }

        scheduler.runLater(() -> Bukkit.getOnlinePlayers().stream()
                .filter(player -> !inDisabledWorld(player.getLocation()))
                .forEach(this::createScoreboard), 20L);
    }

    @Override
    public void onDisable() {
        if (scoreTask != null) {
            scoreTask.cancel();
        }

        for (UUID uuid : new HashSet<>(players.keySet())) {
            ScoreHelper helper = players.remove(uuid);
            if (helper != null) {
                helper.remove();
            }
        }

        players.clear();
        toggledPlayers.clear();
    }

    public void createScoreboard(Player player) {
        if (toggledPlayers.contains(player.getUniqueId())) return;

        scheduler.runAtEntity(player, task -> {
            try {
                ScoreHelper helper = updateScoreboard(player.getUniqueId());
                if (helper != null) {
                    players.put(player.getUniqueId(), helper);
                }
            } catch (Exception e) {
                getPlugin().getLogger().warning("Failed to create scoreboard for " + player.getName());
            }
        });
    }

    public ScoreHelper updateScoreboard(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        if (toggledPlayers.contains(uuid)) return players.get(uuid);

        ScoreHelper helper = players.get(uuid);
        if (helper == null) {
            helper = new ScoreHelper(player);
        }

        helper.setTitle(title);

        int slot = lines.size();
        for (String text : lines) {
            helper.setSlot(slot, text);
            slot--;
        }

        return helper;
    }

    public void removeScoreboard(Player player) {
        ScoreHelper helper = players.remove(player.getUniqueId());
        if (helper != null) {
            scheduler.runAtEntity(player, task -> helper.remove());
        }
    }

    public boolean hasScore(UUID uuid) {
        return players.containsKey(uuid);
    }

    public Collection<UUID> getPlayers() {
        return players.keySet();
    }

    public void toggleScoreboard(Player player) {
        UUID uuid = player.getUniqueId();

        if (toggledPlayers.contains(uuid)) {
            toggledPlayers.remove(uuid);
            createScoreboard(player);
        } else {
            toggledPlayers.add(uuid);
            removeScoreboard(player);
        }
    }

    public boolean isToggled(UUID uuid) {
        return !toggledPlayers.contains(uuid);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!inDisabledWorld(player.getLocation()) && !hasScore(player.getUniqueId())) {
            scheduler.runAtEntityLater(player, task -> createScoreboard(player), joinDelay);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeScoreboard(player);
        toggledPlayers.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (event.getTo().getWorld() == null) return;
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) return;

        if (inDisabledWorld(event.getTo()) && players.containsKey(player.getUniqueId())) {
            removeScoreboard(player);
        } else if (!inDisabledWorld(event.getTo()) && !players.containsKey(player.getUniqueId())) {
            scheduler.runAtEntityLater(player, task -> createScoreboard(player), worldDelay);
        }
    }
}