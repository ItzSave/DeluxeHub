package net.zithium.deluxehub.inventory;

import net.zithium.deluxehub.inventory.condition.Condition;
import net.zithium.deluxehub.utility.PlaceholderUtil;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryItem {

    public final ItemStack itemStack;
    public final List<ClickAction> clickActions;
    private Condition condition;

    public InventoryItem(final ItemStack itemStack) {
        this.clickActions = new ArrayList<>();
        this.itemStack = itemStack;
        this.condition = player -> true; // Default: always visible
    }

    public InventoryItem addClickAction(final ClickAction clickAction) {
        this.clickActions.add(clickAction);
        return this;
    }

    public List<ClickAction> getClickActions() {
        return this.clickActions;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Gets the item stack with placeholders replaced for the given player.
     *
     * @param player The player to replace placeholders for
     * @return A cloned ItemStack with placeholders replaced
     */
    public ItemStack getItemStack(final Player player) {
        ItemStack cloned = this.itemStack.clone();
        ItemMeta meta = cloned.getItemMeta();

        if (meta != null) {
            // Replace placeholders in display name
            if (meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                displayName = PlaceholderUtil.setPlaceholders(displayName, player);
                meta.setDisplayName(ColorUtil.color(displayName));
            }

            // Replace placeholders in lore
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore = lore.stream()
                            .map(line -> ColorUtil.color(PlaceholderUtil.setPlaceholders(line, player)))
                            .collect(Collectors.toList());
                    meta.setLore(lore);
                }
            }

            cloned.setItemMeta(meta);
        }

        return cloned;
    }

    public InventoryItem setCondition(final Condition condition) {
        this.condition = condition;
        return this;
    }

    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Checks if this item should be visible for the given player.
     *
     * @param player The player to check visibility for
     * @return true if the item should be visible, false otherwise
     */
    public boolean isVisibleFor(final Player player) {
        return this.condition == null || this.condition.evaluate(player);
    }
}
