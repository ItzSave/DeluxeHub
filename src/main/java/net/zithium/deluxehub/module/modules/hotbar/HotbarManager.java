package net.zithium.deluxehub.module.modules.hotbar;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.config.ConfigType;
import net.zithium.deluxehub.module.Module;
import net.zithium.deluxehub.module.ModuleType;
import net.zithium.deluxehub.module.modules.hotbar.items.CustomItem;
import net.zithium.deluxehub.module.modules.hotbar.items.PlayerHider;
import net.zithium.deluxehub.utility.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HotbarManager extends Module {

    private List<HotbarItem> hotbarItems;
    private int selectedSlot = -1;
    private final PlatformScheduler scheduler;

    public HotbarManager(DeluxeHubPlugin plugin) {
        super(plugin, ModuleType.HOTBAR_ITEMS);
        this.scheduler = DeluxeHubPlugin.scheduler();
    }

    @Override
    public void onEnable() {
        hotbarItems = new CopyOnWriteArrayList<>();
        FileConfiguration config = getConfig(ConfigType.SETTINGS);

        if (config.getBoolean("custom_join_items.enabled")) {
            String selectedItemKey = config.getString("custom_join_items.selected_item");
            if (selectedItemKey != null && config.contains("custom_join_items.items." + selectedItemKey)) {
                org.bukkit.configuration.ConfigurationSection itemSection = config.getConfigurationSection("custom_join_items.items." + selectedItemKey);
                ItemStack item = ItemStackBuilder.getItemStack(itemSection).build();
                int slot = itemSection.getInt("slot");
                CustomItem customItem = new CustomItem(this, item, slot, selectedItemKey);

                if (itemSection.contains("permission")) {
                    customItem.setPermission(itemSection.getString("permission"));
                }

                customItem.setConfigurationSection(itemSection);
                customItem.setAllowMovement(config.getBoolean("custom_join_items.disable_inventory_movement"));
                registerHotbarItem(customItem);
                this.selectedSlot = slot;
            }
        }

        if (config.getBoolean("player_hider.enabled")) {
            ItemStack item = ItemStackBuilder.getItemStack(config.getConfigurationSection("player_hider.not_hidden")).build();
            PlayerHider playerHider = new PlayerHider(this, item, config.getInt("player_hider.slot"), "PLAYER_HIDER");

            playerHider.setAllowMovement(config.getBoolean("player_hider.disable_inventory_movement"));

            registerHotbarItem(playerHider);
        }

        giveItems();
    }

    @Override
    public void onDisable() {
        removeItems();
    }

    public List<HotbarItem> getHotbarItems() {
        return hotbarItems;
    }

    public void registerHotbarItem(HotbarItem hotbarItem) {
        getPlugin().getServer().getPluginManager().registerEvents(hotbarItem, getPlugin());
        hotbarItems.add(hotbarItem);
    }

    private void giveItems() {
        Bukkit.getOnlinePlayers().stream().filter(player -> !inDisabledWorld(player.getLocation())).forEach(player -> {
            hotbarItems.forEach(hotbarItem -> hotbarItem.giveItem(player));
            if (selectedSlot != -1) {
                player.getInventory().setHeldItemSlot(selectedSlot);
            }
        });
    }

    private void removeItems() {
        Bukkit.getOnlinePlayers().stream().filter(player -> !inDisabledWorld(player.getLocation())).forEach(player -> hotbarItems.forEach(hotbarItem -> hotbarItem.removeItem(player)));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!inDisabledWorld(player.getLocation()) && selectedSlot != -1) {
            scheduler.runLater(() -> player.getInventory().setHeldItemSlot(selectedSlot), 2L);
        }
    }
}
