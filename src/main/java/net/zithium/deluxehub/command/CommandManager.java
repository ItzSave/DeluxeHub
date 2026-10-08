package net.zithium.deluxehub.command;

import cl.bgmp.bukkit.util.BukkitCommandsManager;
import cl.bgmp.bukkit.util.CommandsManagerRegistration;
import cl.bgmp.minecraft.util.commands.CommandsManager;
import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import cl.bgmp.minecraft.util.commands.injection.SimpleInjector;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.command.commands.ClearchatCommand;
import net.zithium.deluxehub.command.commands.DeluxeHubCommand;
import net.zithium.deluxehub.command.commands.FlyCommand;
import net.zithium.deluxehub.command.commands.LobbyCommand;
import net.zithium.deluxehub.command.commands.LockchatCommand;
import net.zithium.deluxehub.command.commands.SetLobbyCommand;
import net.zithium.deluxehub.command.commands.VanishCommand;
import net.zithium.deluxehub.command.commands.gamemode.AdventureCommand;
import net.zithium.deluxehub.command.commands.gamemode.CreativeCommand;
import net.zithium.deluxehub.command.commands.gamemode.GamemodeCommand;
import net.zithium.deluxehub.command.commands.gamemode.SpectatorCommand;
import net.zithium.deluxehub.command.commands.gamemode.SurvivalCommand;
import net.zithium.deluxehub.config.ConfigType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {

    private final DeluxeHubPlugin plugin;
    private FileConfiguration config;

    private CommandsManager commands;
    private CommandsManagerRegistration commandRegistry;

    private final List<CustomCommand> customCommands;

    public CommandManager(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getFile(ConfigType.COMMANDS).getConfig();
        this.customCommands = new ArrayList<>();
    }

    public void reload() {
        config = plugin.getConfigManager().getFile(ConfigType.COMMANDS).getConfig();

        if (commandRegistry != null) {
            commandRegistry.unregisterCommands();
        }

        commands = new BukkitCommandsManager();
        commandRegistry = new CommandsManagerRegistration(plugin, commands);
        commands.setInjector(new SimpleInjector(plugin));

        commandRegistry.register(DeluxeHubCommand.class);

        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) {
            plugin.getLogger().warning("The 'commands' section in commands.yml is missing or invalid; built-in commands will not be registered.");
        } else {
            for (String command : commandsSection.getKeys(false)) {
                if (!config.getBoolean("commands." + command + ".enabled")) continue;

                registerCommand(command, config.getStringList("commands." + command + ".aliases").toArray(new String[0]));
            }
        }

        reloadCustomCommands();
    }

    public void execute(String cmd, String[] args, CommandSender sender) throws CommandException {
        commands.execute(cmd, args, sender, sender);
    }

    public void reloadCustomCommands() {
        if (!customCommands.isEmpty()) {
            customCommands.clear();
        }

        ConfigurationSection customCommandsSection = config.getConfigurationSection("custom_commands");
        if (customCommandsSection == null) {
            if (config.isSet("custom_commands")) {
                plugin.getLogger().warning("The 'custom_commands' section in commands.yml is invalid; custom commands will not be loaded.");
            }
            return;
        }

        for (String entry : customCommandsSection.getKeys(false)) {

            CustomCommand customCommand = new CustomCommand(entry, config.getStringList("custom_commands." + entry + ".actions"));

            if (config.contains("custom_commands." + entry + ".aliases")) {
                customCommand.addAliases(config.getStringList("custom_commands." + entry + ".aliases"));
            }

            if (config.contains("custom_commands." + entry + ".permission")) {
                customCommand.setPermission(config.getString("custom_commands." + entry + ".permission"));
            }

            customCommands.add(customCommand);
        }
    }

    private void registerCommand(String cmd, String[] aliases) {
        switch (cmd.toUpperCase()) {
            case "GAMEMODE" -> commandRegistry.register(GamemodeCommand.class, aliases);
            case "GMS" -> commandRegistry.register(SurvivalCommand.class, aliases);
            case "GMC" -> commandRegistry.register(CreativeCommand.class, aliases);
            case "GMA" -> commandRegistry.register(AdventureCommand.class, aliases);
            case "GMSP" -> commandRegistry.register(SpectatorCommand.class, aliases);
            case "CLEARCHAT" -> commandRegistry.register(ClearchatCommand.class, aliases);
            case "FLY" -> commandRegistry.register(FlyCommand.class, aliases);
            case "LOCKCHAT" -> commandRegistry.register(LockchatCommand.class, aliases);
            case "SETLOBBY" -> commandRegistry.register(SetLobbyCommand.class, aliases);
            case "LOBBY" -> commandRegistry.register(LobbyCommand.class, aliases);
            case "VANISH" -> commandRegistry.register(VanishCommand.class, aliases);
        }
    }

    public List<CustomCommand> getCustomCommands() {
        return customCommands;
    }
}
