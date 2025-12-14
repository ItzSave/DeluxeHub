package net.zithium.deluxehub.module.modules.visual.tablist;

import com.cryptomorin.xseries.reflection.XReflection;
import com.google.common.base.Strings;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.reflection.ReflectionUtils;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;

public class TablistHelper {

    private static final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(DeluxeHubPlugin.class);

    public static void sendTabList(Player player, String header, String footer) {

        Objects.requireNonNull(player, "Cannot update tab for null player");
        header = Strings.isNullOrEmpty(header) ?
                "" : ColorUtil.color(header).replace("%player%", player.getDisplayName());
        footer = Strings.isNullOrEmpty(footer) ?
                "" : ColorUtil.color(footer).replace("%player%", player.getDisplayName());

        if (XReflection.supports(13)) {
            player.setPlayerListHeaderFooter(header, footer);
            return;
        }

        try {
            Method chatComponentBuilderMethod = ReflectionUtils.getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class);
            Object tabHeader = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + header + "\"}");
            Object tabFooter = chatComponentBuilderMethod.invoke(null, "{\"text\":\"" + footer + "\"}");
            Object packet = ReflectionUtils.getNMSClass("PacketPlayOutPlayerListHeaderFooter").getConstructor().newInstance();

            Field aField;
            Field bField;
            try {
                aField = packet.getClass().getDeclaredField("a");
                bField = packet.getClass().getDeclaredField("b");
            } catch (Exception ex) {
                aField = packet.getClass().getDeclaredField("header");
                bField = packet.getClass().getDeclaredField("footer");
            }

            aField.setAccessible(true);
            aField.set(packet, tabHeader);

            bField.setAccessible(true);
            bField.set(packet, tabFooter);

            ReflectionUtils.sendPacket(player, packet);
        } catch (Exception ex) {
            PLUGIN.getLogger().log(Level.SEVERE, "Failed to send tablist header/footer to player " + player.getName(), ex);
        }
    }

    /**
     * Sets a custom player list name for the player in the tablist
     * @param player The player to set the name for
     * @param name The custom name to display (null to reset to default)
     */
    public static void setPlayerListName(Player player, String name) {
        Objects.requireNonNull(player, "Cannot set player list name for null player");

        if (name == null || name.isEmpty()) {
            // Reset to default (player's display name)
            player.setPlayerListName(player.getName());
        } else {
            // Set custom name with color codes
            String formattedName = ColorUtil.color(name);
            player.setPlayerListName(formattedName);
        }
    }
}
