package fun.lewisdev.deluxehub.commands.impl;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.Permissions;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.ModuleType;
import fun.lewisdev.deluxehub.module.modules.chat.ChatLock;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("lockchat")
public class LockchatCommand extends BaseCommand {

    private final DeluxeHubPlugin plugin;

    public LockchatCommand(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    public void command(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_LOCKCHAT.getPermission())) {
            Messages.NO_PERMISSION.send(sender);
            return;
        }

        ChatLock chatLockModule = (ChatLock) plugin.getModuleManager().getModule(ModuleType.CHAT_LOCK);

        if (chatLockModule.isChatLocked()) {
            Bukkit.getOnlinePlayers().forEach(player -> Messages.CHAT_UNLOCKED_BROADCAST.send(player, "%player%", sender.getName()));
            chatLockModule.setChatLocked(false);
        } else {
            Bukkit.getOnlinePlayers().forEach(player -> Messages.CHAT_LOCKED_BROADCAST.send(player, "%player%", sender.getName()));
            chatLockModule.setChatLocked(true);
        }
    }
}
