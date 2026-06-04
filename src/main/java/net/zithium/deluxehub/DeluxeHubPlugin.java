package net.zithium.deluxehub;

import cl.bgmp.minecraft.util.commands.exceptions.CommandException;
import cl.bgmp.minecraft.util.commands.exceptions.CommandPermissionsException;
import cl.bgmp.minecraft.util.commands.exceptions.CommandUsageException;
import cl.bgmp.minecraft.util.commands.exceptions.MissingNestedCommandException;
import cl.bgmp.minecraft.util.commands.exceptions.WrappedCommandException;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import net.zithium.deluxehub.action.ActionManager;
import net.zithium.deluxehub.command.CommandManager;
import net.zithium.deluxehub.config.ConfigManager;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.config.Messages;
import net.zithium.deluxehub.cooldown.CooldownManager;
import net.zithium.deluxehub.hook.HooksManager;
import net.zithium.deluxehub.inventory.InventoryManager;
import net.zithium.deluxehub.module.ModuleManager;
import net.zithium.deluxehub.module.ModuleType;
import net.zithium.deluxehub.module.modules.hologram.HologramManager;
import net.zithium.deluxehub.utility.UpdateChecker;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DeluxeHubPlugin extends JavaPlugin {

    private static PlatformScheduler scheduler;

    private static final int BSTATS_ID = 26336;

    public static PlatformScheduler scheduler() {
        return scheduler;
    }

    private ConfigManager configManager;
    private ActionManager actionManager;
    private HooksManager hooksManager;
    private CommandManager commandManager;
    private CooldownManager cooldownManager;
    private ModuleManager moduleManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        getLogger().info(" _   _            _          _    _ ");
        getLogger().info("| \\ |_ |  | | \\/ |_ |_| | | |_)   _)");
        getLogger().info("|_/ |_ |_ |_| /\\ |_ | | |_| |_)   _)");
        getLogger().info("");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Author: ItzSave & ItsLewizzz");
        getLogger().info("");

        // Ensure we're running on Spigot
        if (!isSpigotEnvironment()) {
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getLogger().severe("DeluxeHub requires Spigot to run.");
            getLogger().severe("Download it here: https://www.spigotmc.org/wiki/spigot-installation/");
            getLogger().severe("Plugin will now disable.");
            getLogger().severe("============= SPIGOT NOT DETECTED =============");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MinecraftVersion.disableUpdateCheck();

        // Initialize Folia scheduling (if needed)
        FoliaLib foliaLib = new FoliaLib(this);
        scheduler = foliaLib.getScheduler();

        // Metrics
        new MetricsLite(this, BSTATS_ID);

        // Hooks and config
        hooksManager = new HooksManager(this);

        configManager = new ConfigManager();
        configManager.loadFiles(this);

        if (!getServer().getPluginManager().isPluginEnabled(this)) {
            return;
        }

        // Core managers
        commandManager = new CommandManager(this);
        commandManager.reload();

        cooldownManager = new CooldownManager();

        inventoryManager = new InventoryManager();
        inventoryManager.onEnable(this);

        moduleManager = new ModuleManager();
        moduleManager.loadModules(this);

        actionManager = new ActionManager(this);

        // Optional update check
        if (getConfigManager().getFile(ConfigType.SETTINGS).getConfig().getBoolean("update-check")) {
            new UpdateChecker(this).checkForUpdate();
        }

        // BungeeCord channel registration
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("");
        getLogger().info("Successfully loaded in " + (System.currentTimeMillis() - start) + "ms");
    }

    private boolean isSpigotEnvironment() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public boolean isPurpurEnviroment() {
        try {
            Class.forName("org.purpurmc.purpur.PurpurConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void onDisable() {
        scheduler.cancelAllTasks();
        moduleManager.unloadModules();
        inventoryManager.onDisable();
        configManager.saveFiles();
    }

    public void reload() {
        scheduler.cancelAllTasks();
        HandlerList.unregisterAll(this);

        configManager.reloadFiles();

        inventoryManager.onDisable();
        inventoryManager.onEnable(this);

        getCommandManager().reload();

        moduleManager.loadModules(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        try {
            getCommandManager().execute(cmd.getName(), args, sender);
        } catch (CommandPermissionsException e) {
            Messages.NO_PERMISSION.send(sender);
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + "Usage: " + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An internal error has occurred. See console.");
                getLogger().severe("An error occurred while executing command: " + e.getMessage());
                if (e.getCause() != null) {
                    getLogger().severe("Caused by: " + e.getCause().getMessage());
                }
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }

        return true;
    }

    public HologramManager getHologramManager() {
        return (HologramManager) moduleManager.getModule(ModuleType.HOLOGRAMS);
    }

    public HooksManager getHookManager() {
        return hooksManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }
}
