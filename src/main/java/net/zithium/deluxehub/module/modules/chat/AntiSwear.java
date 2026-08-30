package net.zithium.deluxehub.module.modules.chat;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.Permissions;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.config.Messages;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class AntiSwear extends Module {

    private List<String> blockedWords;
    private final DeluxeHubPlugin plugin;

    public AntiSwear(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.ANTI_SWEAR);
        this.plugin = plugin;


    }

    @Override
    public void onEnable() {
        blockedWords = getConfig(ConfigType.SETTINGS).getStringList("anti_swear.blocked_words");

        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent", false, plugin.getClass().getClassLoader());
            plugin.getServer().getPluginManager().registerEvents(new PaperHandler(plugin, this), plugin);
        } catch (ClassNotFoundException ignored) {
            plugin.getServer().getPluginManager().registerEvents(new SpigotHandler(plugin, this), plugin);
        }
    }

    @Override
    public void onDisable() {
    }

    private void handleSwearCheck(Player player, String message, Runnable cancelAction) {
        if (player.hasPermission(Permissions.ANTI_SWEAR_BYPASS.getPermission())) {
            return;
        }

		if (blockedWords == null)
			return ;
        for (String word : blockedWords) {
            if (message.toLowerCase().contains(word.toLowerCase())) {
                cancelAction.run();
                Messages.ANTI_SWEAR_WORD_BLOCKED.send(player);

                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission(Permissions.ANTI_SWEAR_NOTIFY.getPermission()))
                        .forEach(p -> Messages.ANTI_SWEAR_ADMIN_NOTIFY.send(p, "%player%", player.getName(), "%word%", message));
                return;
            }
        }
    }

    /**
     * Paper handler using AsyncChatEvent + Adventure.
     * Only registered when Paper's event exists.
     */
    private record PaperHandler(DeluxeHubPlugin plugin, AntiSwear antiSwear) implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerChat(AsyncChatEvent event) {
            final Player player = event.getPlayer();
            final String message = PlainTextComponentSerializer.plainText()
                    .serialize(event.message())
                    .trim();

            antiSwear.handleSwearCheck(player, message, () -> event.setCancelled(true));
        }
    }

    /**
     * Spigot handler using AsyncPlayerChatEvent (deprecated on Paper).
     * Registered only when Paper's event is absent.
     */
    private record SpigotHandler(DeluxeHubPlugin plugin, AntiSwear antiSwear) implements Listener {

        @SuppressWarnings("deprecation")
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            final Player player = event.getPlayer();
            final String message = event.getMessage().trim();

            antiSwear.handleSwearCheck(player, message, () -> event.setCancelled(true));
        }
    }
}
