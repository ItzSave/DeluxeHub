package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;

public class MessageAction implements Action {

    @Override
    public String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String msg = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(data, player);
        player.sendMessage(ColorUtil.color(msg));
    }
}
