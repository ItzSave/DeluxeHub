package net.zithium.deluxehub.module.modules.visual.scoreboard;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoreUpdateTask implements Runnable {

    private final ScoreboardManager scoreboardManager;

    public ScoreUpdateTask(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public void run() {
        List<UUID> toRemove = new ArrayList<>();

        for (UUID uuid : new ArrayList<>(scoreboardManager.getPlayers())) {
            if (Bukkit.getPlayer(uuid) == null) {
                toRemove.add(uuid);
                continue;
            }

            try {
                scoreboardManager.updateScoreboard(uuid);
            } catch (Exception e) {
                toRemove.add(uuid);
            }
        }

        toRemove.forEach(uuid -> scoreboardManager.getPlayers().remove(uuid));
    }
}