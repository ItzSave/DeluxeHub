package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.deluxehub.utility.reflection.ActionBar;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;

public class ActionbarAction implements Action {

    @Override
    public String getIdentifier() {
        return "ACTIONBAR";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String msg = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(data, player);
        ActionBar.sendActionBar(player, ColorUtil.color(msg));
    }
}
