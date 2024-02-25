package fun.lewisdev.deluxehub.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.player.PlayerVanish;
import org.bukkit.entity.Player;

@CommandAlias("vanish|v")
public class VanishCommand extends BaseCommand {

    private final DeluxeHubPlugin plugin;


    public VanishCommand(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void command(Player sender) {

        if (!sender.hasPermission(Permissions.COMMAND_VANISH.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }

        PlayerVanish vanishModule = ((PlayerVanish) plugin.getModuleManager().getModule(ModuleType.VANISH));
        vanishModule.toggleVanish(sender);
    }
}
