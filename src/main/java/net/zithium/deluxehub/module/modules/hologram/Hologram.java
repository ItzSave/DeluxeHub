package net.zithium.deluxehub.module.modules.hologram;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.TeleportUtil;
import net.zithium.deluxehub.utility.reflection.ArmorStandName;
import net.zithium.deluxehub.utility.TextUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Hologram {

    private static final double LINE_HEIGHT = 0.25;

    private final PlatformScheduler scheduler;
    private final DeluxeHubPlugin plugin;
    private final List<ArmorStand> stands;
    private final AtomicInteger lineCounter;
    private Location location;
    private final String name;

    public Hologram(DeluxeHubPlugin plugin, String name, Location location) {
        this.scheduler = DeluxeHubPlugin.scheduler();
        this.plugin = plugin;
        this.name = name;
        this.location = location;
        this.stands = new CopyOnWriteArrayList<>();
        this.lineCounter = new AtomicInteger(0);
    }

    public Hologram setLines(List<String> lines) {
        List<ArmorStand> oldStands = new CopyOnWriteArrayList<>(stands);
        stands.clear();
        lineCounter.set(0);

        for (ArmorStand stand : oldStands) {
            scheduler.runAtEntity(stand, task -> stand.remove());
        }

        for (String s : lines) {
            addLine(s);
        }

        return this;
    }

    public Hologram addLine(String text) {
        final int currentIndex = lineCounter.getAndIncrement();
        final double height = currentIndex * LINE_HEIGHT;

        scheduler.runAtLocation(location, task -> {
            ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(
                location.clone().subtract(0, height, 0),
                EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.customName(TextUtil.parse(text.trim()));
            stand.setCanPickupItems(false);
            stands.add(stand);
        });

        return this;
    }

    public Hologram setLine(int line, String text) {
        if (line < 1 || line > stands.size()) {
            return this;
        }

        ArmorStand stand = stands.get(line - 1);
        scheduler.runAtEntity(stand, task -> stand.customName(TextUtil.parse(text.trim())));
        return this;
    }

    public Hologram removeLine(int line) {
        if (line < 1 || line > stands.size()) {
            return this;
        }

        ArmorStand stand = stands.get(line - 1);
        int index = line - 1;
        scheduler.runAtEntity(stand, task -> {
            stand.remove();
            stands.remove(index);
            lineCounter.decrementAndGet();
            refreshLines(index);
        });

        return this;
    }

    public boolean refreshLines(int line) {
        List<ArmorStand> standsTemp = new ArrayList<>();

        int count = 0;
        for (ArmorStand entry : stands) {
            if (count >= line) {
                standsTemp.add(entry);
            }

            count++;
        }

        for (ArmorStand stand : standsTemp) {
            Location newLocation = stand.getLocation().add(0, LINE_HEIGHT, 0);
            TeleportUtil.teleportCompat(stand, newLocation);
        }

        return count >= 1;
    }

    public Hologram setLocation(Location location) {
        this.location = location;
        setLines(stands.stream().map(ArmorStandName::getName).collect(Collectors.toList()));
        return this;
    }

    public boolean hasLine(int line) {
        return line - 1 < stands.size() && line > 0;
    }

    public void remove() {
        if (plugin.isEnabled()) {
            for (ArmorStand stand : stands) {
                scheduler.runAtEntity(stand, task -> stand.remove());
            }
        }

        stands.clear();
        lineCounter.set(0);
    }

    public Location getLocation() {
        return location;
    }

    public List<ArmorStand> getStands() {
        return stands;
    }

    public String getName() {
        return name;
    }
}
