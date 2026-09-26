package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.deluxehub.utility.TextUtil;
import org.bukkit.entity.Player;

public class MessageAction implements Action {

    @Override
    public String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        player.sendMessage(TextUtil.parse(data));
    }
}
