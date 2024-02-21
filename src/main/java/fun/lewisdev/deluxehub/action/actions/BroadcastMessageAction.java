package fun.lewisdev.deluxehub.action.actions;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.utility.TextUtil;
import fun.lewisdev.deluxehub.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastMessageAction implements Action {

    @Override
    public String getIdentifier() {
        return "BROADCAST";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(TextUtil.color(data));
        }
    }
}
