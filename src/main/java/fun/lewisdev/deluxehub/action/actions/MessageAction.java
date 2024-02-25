package fun.lewisdev.deluxehub.action.actions;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.action.Action;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;

public class MessageAction implements Action {

    @Override
    public String getIdentifier() {
        return "MESSAGE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        player.sendMessage(ColorUtil.color(data));
    }
}
