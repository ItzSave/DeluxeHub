package net.zithium.deluxehub.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InventoryBuilder implements InventoryHolder {

    private final Map<Integer, InventoryItem> icons;
    private int size;
    private final String title;

    public InventoryBuilder(int size, String title) {
        this.icons = new HashMap<>();
        this.size = size;
        this.title = title;
    }

    public void setItem(int slot, InventoryItem item) {
        icons.put(slot, item);
    }

    public InventoryItem getIcon(final int slot) {
        return icons.get(slot);
    }

    public Map<Integer, InventoryItem> getIcons() {
        return icons;
    }

    public @NotNull Inventory getInventory() {
        if (size > 54) {
            size = 54;
        } else if (size < 9) {
            size = 9;
        }

        Inventory inventory = Bukkit.createInventory(this, size, title);
        for (Map.Entry<Integer, InventoryItem> entry : icons.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
        }

        return inventory;
    }

    /**
     * Gets a player-specific inventory that respects item conditions.
     * Items that don't meet their conditions for this player will not be shown.
     *
     * @param player The player to create the inventory for
     * @return A customized inventory for the player
     */
    public @NotNull Inventory getInventory(Player player) {
        if (size > 54) {
            size = 54;
        } else if (size < 9) {
            size = 9;
        }

        Inventory inventory = Bukkit.createInventory(this, size, title);
        for (Map.Entry<Integer, InventoryItem> entry : icons.entrySet()) {
            InventoryItem item = entry.getValue();
            // Only add item if it meets the condition for this player
            if (item.isVisibleFor(player)) {
                inventory.setItem(entry.getKey(), item.getItemStack(player));
            }
        }

        return inventory;
    }
}
