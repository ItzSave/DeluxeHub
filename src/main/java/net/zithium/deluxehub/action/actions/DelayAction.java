package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

public class DelayAction implements Action {

    @Override
    public String getIdentifier() {
        return "DELAY";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        // data = "50 [MESSAGE] It's been 10 seconds!"
        String[] parts = data.split(" ", 2);

        if (parts.length < 2) {
            plugin.getLogger().warning("Invalid DELAY action format. Expected: [DELAY:{seconds}] [ACTION]");
            return;
        }

        int ticks;
        try {
            ticks = Integer.parseInt(parts[0].trim()) * 20;
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid delay time in DELAY action: " + parts[0]);
            return;
        }

        String remainingAction = parts[1].trim();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            String actionName = StringUtils.substringBetween(remainingAction, "[", "]");
            Action action = actionName == null ? null : plugin.getActionManager().getAction(actionName.toUpperCase());

            if (action != null) {
                String actionData = remainingAction.contains(" ") ? remainingAction.split(" ", 2)[1] : "";
                action.execute(plugin, player, actionData);
            } else {
                plugin.getLogger().warning("Invalid action in DELAY: " + remainingAction);
            }
        }, ticks);
    }
}