package net.zithium.deluxehub.action.actions;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.deluxehub.utility.TextUtil;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

public class TitleAction implements Action {

    @Override
    public String getIdentifier() {
        return "TITLE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String[] args = data.split(";");

        var mainTitle = TextUtil.parse(args[0]);
        var subTitle = TextUtil.parse(args[1]);

        int fadeIn;
        int stay;
        int fadeOut;
        try {
            fadeIn = Integer.parseInt(args[2]);
            stay = Integer.parseInt(args[3]);
            fadeOut = Integer.parseInt(args[4]);
        } catch (NumberFormatException ex) {
            fadeIn = 1;
            stay = 3;
            fadeOut = 1;
        }
        player.showTitle(Title.title(mainTitle, subTitle, Title.Times.times(
                java.time.Duration.ofSeconds(fadeIn), java.time.Duration.ofSeconds(stay), java.time.Duration.ofSeconds(fadeOut))));
    }
}
