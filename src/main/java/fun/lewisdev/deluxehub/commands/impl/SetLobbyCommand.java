package fun.lewisdev.deluxehub.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.world.LobbySpawn;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;

@CommandAlias("setlobby|setspawn")
public class SetLobbyCommand extends BaseCommand {
    private final DeluxeHubPlugin plugin;


    public SetLobbyCommand(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void command(Player player) {
        if (!player.hasPermission(Permissions.COMMAND_SET_LOBBY.getPermission())) {
            Messages.NO_PERMISSION.send(player);
            return;
        }

        if (plugin.getModuleManager().getDisabledWorlds().contains(player.getWorld().getName())) {
            player.sendMessage(ColorUtil.color("&cYou cannot set the lobby location in a disabled world."));
            return;
        }

        LobbySpawn lobbyModule = ((LobbySpawn) plugin.getModuleManager().getModule(ModuleType.LOBBY));
        lobbyModule.setLocation(player.getLocation());
        Messages.SET_LOBBY.send(player);
    }
}
