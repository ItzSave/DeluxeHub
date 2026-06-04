package net.zithium.deluxehub.action;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.actions.*;
import net.zithium.deluxehub.utility.PlaceholderUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManager {

    private final DeluxeHubPlugin plugin;
    private final Map<String, Action> actions;

    public ActionManager(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
        actions = new HashMap<>();
        load();
    }

    private void load() {
        registerAction(
                new MessageAction(),
                new BroadcastMessageAction(),
                new CommandAction(),
                new ConsoleCommandAction(),
                new SoundAction(),
                new PotionEffectAction(),
                new GamemodeAction(),
                new CloseInventoryAction(),
                new ActionbarAction(),
                new TitleAction(),
                new MenuAction(),
                new ProxyAction(),
                new DelayAction()
        );
    }

    public void registerAction(Action... actions) {
        Arrays.asList(actions).forEach(action -> this.actions.put(action.getIdentifier(), action));
    }

    public void executeActions(Player player, List<String> items) {
        items.forEach(item -> {
            String actionName = StringUtils.substringBetween(item, "[", "]");

            String inlineArg = null;
            if (actionName != null && actionName.contains(":")) {
                String[] split = actionName.split(":", 2);
                actionName = split[0];
                inlineArg = split[1];
            }

            Action action = actionName == null ? null : actions.get(actionName.toUpperCase());

            if (action != null) {
                item = item.contains(" ") ? item.split(" ", 2)[1] : "";
                item = PlaceholderUtil.setPlaceholders(item, player);

                if (inlineArg != null) {
                    item = inlineArg + " " + item;
                }

                action.execute(plugin, player, item);
            } else {
                plugin.getLogger().warning("There was a problem attempting to process action: '" + item + "'");
            }
        });
    }

    public Action getAction(String identifier) {
        return actions.get(identifier.toUpperCase());
    }
}
