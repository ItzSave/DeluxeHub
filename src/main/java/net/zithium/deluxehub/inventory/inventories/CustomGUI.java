package net.zithium.deluxehub.inventory.inventories;

import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.inventory.AbstractInventory;
import net.zithium.deluxehub.inventory.InventoryBuilder;
import net.zithium.deluxehub.inventory.InventoryItem;
import net.zithium.deluxehub.inventory.condition.ConditionParser;
import net.zithium.deluxehub.utility.ItemStackBuilder;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;

import java.util.logging.Level;

public class CustomGUI extends AbstractInventory {

    private InventoryBuilder inventory;
    private final FileConfiguration config;

    public CustomGUI(DeluxeHubPlugin plugin, FileConfiguration config) {
        super(plugin);
        this.config = config;
    }

    @Override
    public void onEnable() {

        InventoryBuilder inventoryBuilder = new InventoryBuilder(config.getInt("slots"), ColorUtil.color(config.getString("title")));

        if (config.contains("refresh") && config.getBoolean("refresh.enabled")) {
            setInventoryRefresh(config.getLong("refresh.rate"));
        }

        for (String entry : config.getConfigurationSection("items").getKeys(false)) {

            try {
                ItemStackBuilder builder = ItemStackBuilder.getItemStack(config.getConfigurationSection("items." + entry));

                InventoryItem inventoryItem;
                if (!config.contains("items." + entry + ".actions")) {
                    inventoryItem = new InventoryItem(builder.build());
                } else {
                    inventoryItem = new InventoryItem(builder.build()).addClickAction(p -> getPlugin().getActionManager().executeActions(p, config.getStringList("items." + entry + ".actions")));
                }

                // Parse and apply conditions if they exist
                if (config.contains("items." + entry + ".conditions")) {
                    if (config.isList("items." + entry + ".conditions")) {
                        inventoryItem.setCondition(ConditionParser.parseMultiple(config.getStringList("items." + entry + ".conditions")));
                    } else {
                        inventoryItem.setCondition(ConditionParser.parse(config.getString("items." + entry + ".conditions")));
                    }
                }

                if (config.contains("items." + entry + ".slots")) {
                    for (String slot : config.getStringList("items." + entry + ".slots")) {
                        inventoryBuilder.setItem(Integer.parseInt(slot), inventoryItem);
                    }
                } else if (config.contains("items." + entry + ".slot")) {
                    int slot = config.getInt("items." + entry + ".slot");
                    if (slot == -1) {
                        while (inventoryBuilder.getInventory().firstEmpty() != -1) {
                            inventoryBuilder.setItem(inventoryBuilder.getInventory().firstEmpty(), inventoryItem);
                        }
                    } else inventoryBuilder.setItem(slot, inventoryItem);
                }
            } catch (Exception e) {
                getPlugin().getLogger().log(Level.WARNING, "Failed to load GUI item ID '" + entry + "', skipping", e);
            }
        }

        inventory = inventoryBuilder;
    }

    @Override
    protected Inventory getInventory() {
        return inventory.getInventory();
    }

    @Override
    protected Inventory getInventory(org.bukkit.entity.Player player) {
        return inventory.getInventory(player);
    }
}
