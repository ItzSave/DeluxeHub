package fun.lewisdev.deluxehub.module.modules.chat;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.commands.CustomCommand;
import fun.lewisdev.deluxehub.config.Messages;
import fun.lewisdev.deluxehub.module.Module;
import fun.lewisdev.deluxehub.module.ModuleType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CustomCommands extends Module {

    private List<CustomCommand> commands;

    public CustomCommands(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.CUSTOM_COMMANDS);
    }

    @Override
    public void onEnable() {
        commands = new ArrayList<>();

        FileConfiguration config = getPlugin().getConfig();
        if (config.isConfigurationSection("custom_commands")) {
            ConfigurationSection commandsSection = config.getConfigurationSection("custom_commands");
            for (String commandKey : commandsSection.getKeys(false)) {
                ConfigurationSection commandConfig = commandsSection.getConfigurationSection(commandKey);
                String permission = commandConfig.getString("permission");
                List<String> aliases = commandConfig.getStringList("aliases");
                List<String> actions = commandConfig.getStringList("actions");
                CustomCommand customCommand = new CustomCommand(aliases.get(0), actions);
                customCommand.setPermission(permission);
                customCommand.addAliases(aliases.subList(1, aliases.size()));
                commands.add(customCommand);
            }
        }
    }

    @Override
    public void onDisable() {
        // Any cleanup tasks if needed
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (inDisabledWorld(player.getLocation())) return;

        String command = event.getMessage().toLowerCase().replace("/", "");

        for (CustomCommand customCommand : commands) {
            if (customCommand.getAliases().contains(command)) {
                String permission = customCommand.getPermission();
                if (permission != null && !player.hasPermission(permission)) {
                    Messages.CUSTOM_COMMAND_NO_PERMISSION.send(player);
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                getPlugin().getActionManager().executeActions(player, customCommand.getActions());
            }
        }
    }
}
