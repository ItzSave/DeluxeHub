package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import org.bukkit.entity.Player;

public class CommandAction implements Action {

    @Override
    public String getIdentifier() {
        return "COMMAND";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String msg = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(data, player);
        player.chat(msg.contains("/") ? msg : "/" + msg);
    }
}
