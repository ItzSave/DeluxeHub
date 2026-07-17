package net.zithium.deluxehub.action.actions;

import com.cryptomorin.xseries.reflection.XReflection;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.action.Action;
import net.zithium.deluxehub.utility.reflection.Titles;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;

public class TitleAction implements Action {

    @Override
    public String getIdentifier() {
        return "TITLE";
    }

    @Override
    public void execute(DeluxeHubPlugin plugin, Player player, String data) {
        String[] args = data.split(";");

        String mainTitle = ColorUtil.color(args[0]);
        String subTitle = ColorUtil.color(args[1]);

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
        player.sendTitle(mainTitle, subTitle, fadeIn * 20, stay * 20, fadeOut * 20);
    }
}
