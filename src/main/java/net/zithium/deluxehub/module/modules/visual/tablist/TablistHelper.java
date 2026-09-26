package net.zithium.deluxehub.module.modules.visual.tablist;

import com.google.common.base.Strings;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class TablistHelper {

    private static final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(DeluxeHubPlugin.class);

    public static void sendTabList(Player player, String header, String footer) {

        Objects.requireNonNull(player, "Cannot update tab for null player");
        header = Strings.isNullOrEmpty(header) ?
                "" : header.replace("%player%", player.getDisplayName());
        footer = Strings.isNullOrEmpty(footer) ?
                "" : footer.replace("%player%", player.getDisplayName());

        player.sendPlayerListHeaderAndFooter(TextUtil.parse(header), TextUtil.parse(footer));
    }
}
