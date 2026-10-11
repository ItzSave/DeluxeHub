package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastMessageAction implements Action {

    @Override
    public String getIdentifier() {
        return "BROADCAST";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String msg = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(data, player);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(ColorUtil.color(msg));
        }
    }
}
