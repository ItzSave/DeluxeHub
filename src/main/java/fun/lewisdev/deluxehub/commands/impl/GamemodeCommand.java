package fun.lewisdev.deluxehub.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.config.Messages;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@CommandAlias("gamemode")
@SuppressWarnings("unused") // Gamemode methods return unused even though they are used.
public class GamemodeCommand extends BaseCommand {

    public void gamemodeCommand(Player player) {

    }

    @Subcommand("creative|c")
    @CommandAlias("gmc")
    public void creative(Player sender) {
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }
        sender.setGameMode(GameMode.CREATIVE);
        Messages.GAMEMODE_CHANGE.send(sender, "%gamemode%", "CREATIVE");
    }

    @Subcommand("survival|s")
    @CommandAlias("gms")
    public void survival(Player sender) {
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }
        sender.setGameMode(GameMode.SURVIVAL);
        Messages.GAMEMODE_CHANGE.send(sender, "%gamemode%", "SURVIVAL");
    }

    @Subcommand("adventure|a")
    @CommandAlias("gma")
    public void adventure(Player sender) {
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }
        sender.setGameMode(GameMode.ADVENTURE);
        Messages.GAMEMODE_CHANGE.send(sender, "%gamemode%", "ADVENTURE");
    }

    @Subcommand("spectator|sp")
    @CommandAlias("gmsp")
    public void spectator(Player sender) {
        if (!sender.hasPermission(Permissions.COMMAND_GAMEMODE.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }
        sender.setGameMode(GameMode.SPECTATOR);
        Messages.GAMEMODE_CHANGE.send(sender, "%gamemode%", "SPECTATOR");
    }
}
