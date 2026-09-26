package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.deluxehub.utility.TextUtil;
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
            p.sendMessage(TextUtil.parse(data));
        }
    }
}
