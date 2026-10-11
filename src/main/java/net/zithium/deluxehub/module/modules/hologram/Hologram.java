package net.zithium.deluxehub.module.modules.hologram;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.TeleportUtil;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Hologram {

    private static final double LINE_HEIGHT = 0.25;

    private final PlatformScheduler scheduler;
    private final DeluxeHubPlugin plugin;
    
    // original lines of the hologram
    private final List<String> originalLines;
    
    // Mapping of player UUID to their personal armor stands
    private final Map<UUID, List<ArmorStand>> playerStands;
    
    private Location location;
    private final String name;

    public Hologram(DeluxeHubPlugin plugin, String name, Location location) {
        this.scheduler = DeluxeHubPlugin.scheduler();
        this.plugin = plugin;
        this.name = name;
        this.location = location;
        this.originalLines = new CopyOnWriteArrayList<>();
        this.playerStands = new ConcurrentHashMap<>();
    }

    public Hologram setLines(List<String> lines) {
        // Remove all existing stands for all players
        for (UUID uuid : playerStands.keySet()) {
            removeFor(uuid);
        }
        originalLines.clear();
        originalLines.addAll(lines);
        
        // Respawn for all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            spawnFor(p);
        }
        return this;
    }

    public Hologram addLine(String text) {
        originalLines.add(text);
        int lineIndex = originalLines.size() - 1;
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            spawnLineFor(p, lineIndex, text);
        }
        return this;
    }

    public Hologram setLine(int line, String text) {
        if (line < 1 || line > originalLines.size()) {
            return this;
        }
        int index = line - 1;
        originalLines.set(index, text);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            List<ArmorStand> stands = playerStands.get(p.getUniqueId());
            if (stands != null && index < stands.size()) {
                ArmorStand stand = stands.get(index);
                scheduler.runAtEntity(stand, task -> {
                    String parsedText = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(text, p);
                    stand.setCustomName(ColorUtil.color(parsedText).trim());
                });
            }
        }
        return this;
    }

    public Hologram removeLine(int line) {
        if (line < 1 || line > originalLines.size()) {
            return this;
        }
        int index = line - 1;
        originalLines.remove(index);
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            List<ArmorStand> stands = playerStands.get(p.getUniqueId());
            if (stands != null && index < stands.size()) {
                ArmorStand stand = stands.remove(index);
                scheduler.runAtEntity(stand, task -> stand.remove());
                refreshLinesFor(p, index);
            }
        }
        return this;
    }

    private void refreshLinesFor(Player player, int fromIndex) {
        List<ArmorStand> stands = playerStands.get(player.getUniqueId());
        if (stands == null) return;
        
        for (int i = fromIndex; i < stands.size(); i++) {
            ArmorStand stand = stands.get(i);
            Location newLocation = location.clone().subtract(0, i * LINE_HEIGHT, 0);
            TeleportUtil.teleportCompat(stand, newLocation);
        }
    }

    public Hologram setLocation(Location location) {
        this.location = location;
        setLines(new ArrayList<>(originalLines));
        return this;
    }

    public boolean hasLine(int line) {
        return line - 1 < originalLines.size() && line > 0;
    }

    public void remove() {
        for (UUID uuid : playerStands.keySet()) {
            removeFor(uuid);
        }
        originalLines.clear();
    }
    
    public void spawnFor(Player player) {
        if (playerStands.containsKey(player.getUniqueId())) return;
        
        playerStands.put(player.getUniqueId(), new CopyOnWriteArrayList<>());
        for (int i = 0; i < originalLines.size(); i++) {
            spawnLineFor(player, i, originalLines.get(i));
        }
    }
    
    private void spawnLineFor(Player player, int lineIndex, String text) {
        final double height = lineIndex * LINE_HEIGHT;
        Location spawnLoc = location.clone().subtract(0, height, 0);
        
        scheduler.runAtLocation(spawnLoc, task -> {
            if (player == null || !player.isOnline()) return; // Player might have left
            
            ArmorStand stand = (ArmorStand) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setPersistent(false); // don't save to world
            stand.addScoreboardTag("deluxehub_active");
            
            String parsedText = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(text, player);
            stand.setCustomName(ColorUtil.color(parsedText).trim());
            stand.setCanPickupItems(false);
            
            // Hide from all OTHER players
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.getUniqueId().equals(player.getUniqueId())) {
                    other.hideEntity(plugin, stand);
                }
            }
            
            // Hide all OTHER players' existing stands from this player
            for (Map.Entry<UUID, List<ArmorStand>> entry : playerStands.entrySet()) {
                if (!entry.getKey().equals(player.getUniqueId())) {
                    for (ArmorStand otherStand : entry.getValue()) {
                        player.hideEntity(plugin, otherStand);
                    }
                }
            }
            
            List<ArmorStand> stands = playerStands.get(player.getUniqueId());
            if (stands != null) {
                stands.add(stand);
            } else {
                stand.remove();
            }
        });
    }
    
    public void removeFor(UUID uuid) {
        List<ArmorStand> stands = playerStands.remove(uuid);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                scheduler.runAtEntity(stand, task -> stand.remove());
            }
        }
    }

    public void updatePlaceholders() {
        for (Map.Entry<UUID, List<ArmorStand>> entry : playerStands.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;
            
            List<ArmorStand> stands = entry.getValue();
            for (int i = 0; i < stands.size(); i++) {
                if (i >= originalLines.size()) break;
                String originalText = originalLines.get(i);
                ArmorStand stand = stands.get(i);
                
                scheduler.runAtEntity(stand, task -> {
                    String parsedText = net.zithium.deluxehub.utility.PlaceholderUtil.setPlaceholders(originalText, player);
                    stand.setCustomName(ColorUtil.color(parsedText).trim());
                });
            }
        }
    }

    public Location getLocation() {
        return location;
    }

    public List<String> getLines() {
        return originalLines;
    }
    
    public List<ArmorStand> getStands() {
        List<ArmorStand> allStands = new ArrayList<>();
        for (List<ArmorStand> s : playerStands.values()) {
            allStands.addAll(s);
        }
        return allStands;
    }

    public String getName() {
        return name;
    }
}
