package net.zithium.deluxehub.action.actions;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleCommandAction implements Action {

    private final PlatformScheduler scheduler = DeluxeHubPlugin.scheduler();

    @Override
    public String getIdentifier() {
        return "CONSOLE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String msg = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(data, player);
        scheduler.runNextTick(task -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), msg));
    }
}
