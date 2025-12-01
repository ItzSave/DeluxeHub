package net.zithium.deluxehub.inventory;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractInventory implements Listener {

    private final DeluxeHubPlugin plugin;
    private final PlatformScheduler scheduler;
    private boolean refreshEnabled = false;
    private final Set<UUID> openInventories = ConcurrentHashMap.newKeySet();

    public AbstractInventory(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = DeluxeHubPlugin.scheduler();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void setInventoryRefresh(long value) {
        if (value <= 0) {
            return;
        }

        scheduler.runTimerAsync(new InventoryTask(this), 1L, value);
        refreshEnabled = true;
    }

    public abstract void onEnable();

    protected abstract Inventory getInventory();

    protected DeluxeHubPlugin getPlugin() {
        return plugin;
    }

    public Inventory refreshInventory(Player player, Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
                continue;
            }

            ItemStackBuilder newItem = new ItemStackBuilder(item.clone());
            if (item.getItemMeta().hasDisplayName()) {
                newItem.withName(item.getItemMeta().getDisplayName(), player);
            }

            if (item.getItemMeta().hasLore()) {
                newItem.withLore(item.getItemMeta().getLore(), player);
            }

            inventory.setItem(i, newItem.build());
        }

        return inventory;
    }

    public void openInventory(Player player) {
        if (getInventory() == null) {
            return;
        }

        scheduler.runAtEntity(player, task -> player.openInventory(refreshInventory(player, getInventory())));
        if (refreshEnabled) {
            openInventories.add(player.getUniqueId());
        }
    }

    public Collection<UUID> getOpenInventories() {
        return openInventories;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof InventoryBuilder && refreshEnabled) {
            openInventories.remove(event.getPlayer().getUniqueId());
        }
    }
}
