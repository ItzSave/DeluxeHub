package fun.lewisdev.deluxehub.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("fly")
public class FlyCommand extends BaseCommand {

    @Default
    public void command(Player player, String[] args) {
        if (args.length == 0) {
            if (!player.hasPermission(Permissions.COMMAND_FLIGHT.getPermission())) {
                Messages.NO_PERMISSION.send(player);
                return;
            }

            if (player.getAllowFlight()) {
                Messages.FLIGHT_DISABLE.send(player);
                toggleFlight(player, false);
            } else {
                Messages.FLIGHT_ENABLE.send(player);
                toggleFlight(player, true);
            }
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                Messages.INVALID_PLAYER.send(player);
                return;
            }

            if (target.getAllowFlight()) {
                Messages.FLIGHT_DISABLE.send(player);
                Messages.FLIGHT_DISABLE_OTHER.send(player, "%player%", target.getName());
                toggleFlight(target, false);
            } else {
                Messages.FLIGHT_ENABLE.send(player);
                Messages.FLIGHT_ENABLE_OTHER.send(player, "%player%", target.getName());
                toggleFlight(target, true);
            }
        }
    }

    private void toggleFlight(Player player, boolean value) {
        player.setAllowFlight(value);
        player.setFlying(value);
    }
}
