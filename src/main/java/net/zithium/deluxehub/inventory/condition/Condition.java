package net.zithium.deluxehub.inventory.condition;

import org.bukkit.entity.Player;

/**
 * Represents a condition that can be evaluated against a player.
 * Used to control visibility and accessibility of menu items.
 */
@FunctionalInterface
public interface Condition {

    /**
     * Evaluates this condition for the given player.
     *
     * @param player The player to evaluate against
     * @return true if the condition is met, false otherwise
     */
    boolean evaluate(Player player);

}

