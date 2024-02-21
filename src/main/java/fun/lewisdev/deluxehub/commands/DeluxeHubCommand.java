package fun.lewisdev.deluxehub.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.config.Messages;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

@CommandAlias("deluxehub|dhub")
@Description("View plugin information")
public class DeluxeHubCommand extends BaseCommand {

    private final DeluxeHubPlugin plugin;

    public DeluxeHubCommand(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    @SuppressWarnings("deprecation") // plugin.getDescription is deprecated via Paper
    public void command(Player sender, String[] args) {


        PluginDescriptionFile pDFile = plugin.getDescription();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission(Permissions.COMMAND_DELUXEHUB_HELP.getPermission())) {
                sender.sendMessage("Server is running DeluxeHub v" + pDFile.getVersion() + "By: " + pDFile.getAuthors());
                return;
            }

            sender.sendMessage("");
            sender.sendMessage(ColorUtil.color("&d&lDeluxeHub " + "&fv" + plugin.getDescription().getVersion()));
            sender.sendMessage(ColorUtil.color("&7Author: &fItsLewizzz"));
            sender.sendMessage("");
            sender.sendMessage(ColorUtil.color(" &d/deluxehub info &8- &7&oDisplays information about the current config"));
            sender.sendMessage(ColorUtil.color(" &d/deluxehub scoreboard &8- &7&oToggle the scoreboard"));
            sender.sendMessage(ColorUtil.color(" &d/deluxehub open <menu> &8- &7&oOpen a custom menu"));
            sender.sendMessage(ColorUtil.color(" &d/deluxehub hologram &8- &7&oView the hologram help"));
            sender.sendMessage("");
            sender.sendMessage(ColorUtil.color("  &d/vanish &8- &7&oToggle vanish mode"));
            sender.sendMessage(ColorUtil.color("  &d/fly &8- &7&oToggle flight mode"));
            sender.sendMessage(ColorUtil.color("  &d/setlobby &8- &7&oSet the spawn location"));
            sender.sendMessage(ColorUtil.color("  &d/lobby &8- &7&oTeleport to the spawn location"));
            sender.sendMessage(ColorUtil.color("  &d/gamemode <gamemode> &8- &7&oSet your gamemode"));
            sender.sendMessage(ColorUtil.color("  &d/gmc &8- &7&oGo into creative mode"));
            sender.sendMessage(ColorUtil.color("  &d/gms &8- &7&oGo into survival mode"));
            sender.sendMessage(ColorUtil.color("  &d/gma &8- &7&oGo into adventure mode"));
            sender.sendMessage(ColorUtil.color("  &d/gmsp &8- &7&oGo into spectator mode"));
            sender.sendMessage(ColorUtil.color("  &d/clearchat &8- &7&oClear global chat"));
            sender.sendMessage(ColorUtil.color("  &d/lockchat &8- &7&oLock/unlock global chat"));
            sender.sendMessage("");
        }
    }

    @Subcommand("reload")
    public void reloadCommand(CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_DELUXEHUB_RELOAD.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }

        long start = System.currentTimeMillis();
        plugin.reload();
        Messages.CONFIG_RELOAD.send(sender, "%time%", String.valueOf(System.currentTimeMillis() - start));
        int inventories = plugin.getInventoryManager().getInventories().size();
        if (inventories > 0) {
            sender.sendMessage(ColorUtil.color("&8- &7Loaded &a" + inventories + "&7 custom menus."));
        }
    }
}
