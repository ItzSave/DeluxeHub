package net.zithium.deluxehub.module.modules.visual.scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ScoreHelper {

    private static final DeluxeHubPlugin plugin = JavaPlugin.getPlugin(DeluxeHubPlugin.class);
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Player player;
    private Scoreboard scoreboard;
    private Objective objective;
    private final Map<Integer, Team> slotTeams = new HashMap<>();

    public ScoreHelper(Player player) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void setTitle(String title) {
        Component titleComponent = parse(title);

        try {
            if (objective == null) {
                objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, titleComponent);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                objective.numberFormat(NumberFormat.blank());
                player.setScoreboard(scoreboard);
            } else {
                objective.displayName(titleComponent);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set scoreboard title", e);
        }
    }

    public void setSlot(int slot, String text) {
        if (objective == null) return;

        try {
            Component lineComponent = parse(text);
            Team team = slotTeams.get(slot);

            if (team == null) {
                String teamName = "slot_" + slot;
                team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                }
                String entry = getEntry(slot);
                team.addEntry(entry);
                objective.getScore(entry).setScore(slot);
                slotTeams.put(slot, team);
            }

            team.prefix(lineComponent);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set scoreboard slot " + slot, e);
        }
    }

    public void removeSlot(int slot) {
        Team team = slotTeams.remove(slot);
        if (team != null) {
            try {
                scoreboard.resetScores(getEntry(slot));
                team.unregister();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to remove scoreboard slot " + slot, e);
            }
        }
    }

    public void remove() {
        if (objective == null) return;

        try {
            objective.unregister();
            objective = null;
            slotTeams.clear();
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        } catch (Exception e) {
            // Silently fail during shutdown
        }
    }

    private Component parse(String text) {
        return MINI_MESSAGE.deserialize(PlaceholderUtil.setPlaceholders(text, player));
    }

    private String getEntry(int slot) {
        return "§" + Integer.toHexString(slot);
    }
}