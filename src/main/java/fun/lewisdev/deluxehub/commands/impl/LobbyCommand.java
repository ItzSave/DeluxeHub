package fun.lewisdev.deluxehub.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.world.LobbySpawn;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("lobby|spawn")
public class LobbyCommand extends BaseCommand {

    private final DeluxeHubPlugin plugin;

    public LobbyCommand(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void command(Player player) {
        Location location = ((LobbySpawn) plugin.getModuleManager().getModule(ModuleType.LOBBY)).getLocation();
        if (location == null) {
            player.sendMessage(ColorUtil.color("&cThe spawn location has not been set. &7Use /setlobby"));
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(location), 3L);
    }
}
