package net.zithium.deluxehub.module.modules.player;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.Permissions;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.config.Messages;
import net.zithium.deluxehub.cooldown.CooldownType;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.List;
import java.util.UUID;

public class DoubleJump extends Module {

    private long cooldownDelay;
    private double launch;
    private double launchY;
    private List<String> actions;

    public DoubleJump(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.DOUBLE_JUMP);
    }

    @Override
    public void onEnable() {
        FileConfiguration config = getConfig(ConfigType.SETTINGS);
        cooldownDelay = config.getLong("double_jump.cooldown", 0);
        launch = config.getDouble("double_jump.launch_power", 1.3);
        launchY = config.getDouble("double_jump.launch_power_y", 1.2);
        actions = config.getStringList("double_jump.actions");

        if (launch > 4.0) {
            launch = 4.0;
        }

        if (launchY > 4.0) {
            launchY = 4.0;
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Basic checks
        if (player.hasPermission(Permissions.DOUBLE_JUMP_BYPASS.getPermission())) {
            return;
        }

        if (inDisabledWorld(player.getLocation())) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (!event.isFlying()) {
            return;
        }

        // Cancel if the player is standing on solid ground - not a legitimate double jump
        if (player.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        if (!tryCooldown(uuid, CooldownType.DOUBLE_JUMP, cooldownDelay)) {
            Messages.DOUBLE_JUMP_COOLDOWN.send(player, "%time%", getCooldown(uuid, CooldownType.DOUBLE_JUMP));
            return;
        }

        player.setVelocity(player.getLocation().getDirection().multiply(launch).setY(launchY));
        executeActions(player, actions);

        // Disable flight until they land again - restored in onPlayerMove
        player.setAllowFlight(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        Player player = event.getPlayer();

        // Already has flight available, nothing to restore
        if (player.getAllowFlight()) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (player.hasPermission(Permissions.DOUBLE_JUMP_BYPASS.getPermission())) {
            return;
        }

        if (inDisabledWorld(player.getLocation())) {
            return;
        }

        // Player has landed allow them to jump again
        if (player.getLocation().subtract(0, 1, 0).getBlock().getType().isSolid()) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR && !inDisabledWorld(player.getLocation())) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR && !inDisabledWorld(player.getLocation())) {
            player.setAllowFlight(true);
        }
    }
}