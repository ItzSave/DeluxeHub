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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractInventory implements Listener {

    private final DeluxeHubPlugin plugin;
    private final PlatformScheduler scheduler;
    private boolean refreshEnabled = false;
    private final List<UUID> openInventories;

    public AbstractInventory(DeluxeHubPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = DeluxeHubPlugin.scheduler();
        openInventories = new ArrayList<>();
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
            ItemStack item = inventory.getItem(i);
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

    /**
     * Gets a player-specific inventory that respects conditions.
     * Override this to provide player-specific inventory generation.
     *
     * @param player The player to get the inventory for
     * @return The player-specific inventory, or default inventory if not overridden
     */
    protected Inventory getInventory(Player player) {
        return getInventory();
    }

    public void openInventory(Player player) {
        if (getInventory() == null) {
            return;
        }

        scheduler.runAtEntity(player, task -> player.openInventory(refreshInventory(player, getInventory(player))));
        if (refreshEnabled && !openInventories.contains(player.getUniqueId())) {
            openInventories.add(player.getUniqueId());
        }
    }

    public List<UUID> getOpenInventories() {
        return openInventories;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof InventoryBuilder && refreshEnabled) {
            openInventories.remove(event.getPlayer().getUniqueId());
        }
    }
}
