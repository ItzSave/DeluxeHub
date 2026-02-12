package net.zithium.deluxehub.module.modules.hotbar;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.reflection.XReflection;
import com.tcoded.folialib.impl.PlatformScheduler;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.zithium.deluxehub.DeluxeHubPlugin;
import net.zithium.deluxehub.utility.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public abstract class HotbarItem implements Listener {

    private final PlatformScheduler scheduler;
    private final HotbarManager hotbarManager;
    private final ItemStack item;
    private ConfigurationSection configurationSection;
    private final String key;
    private String permission = null;
    private final int slot;
    private boolean allowMovement;

    public HotbarItem(HotbarManager hotbarManager, ItemStack item, int slot, String key) {
        this.scheduler = DeluxeHubPlugin.scheduler();
        this.hotbarManager = hotbarManager;
        this.key = key;
        this.slot = slot;

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("hotbarItem", key);
        this.item = nbtItem.getItem();
    }

    public DeluxeHubPlugin getPlugin() {
        return hotbarManager.getPlugin();
    }

    public HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public ItemStack getItem() {
        return item;
    }

    protected abstract void onInteract(Player player);

    public String getKey() {
        return key;
    }

    public int getSlot() {
        return slot;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setAllowMovement(boolean allowMovement) {
        this.allowMovement = allowMovement;
    }

    public String getPermission() {
        return permission;
    }

    public void setConfigurationSection(ConfigurationSection configurationSection) {
        this.configurationSection = configurationSection;
    }

    public ConfigurationSection getConfigurationSection() {
        return configurationSection;
    }

    public void giveItem(Player player) {
        if (permission != null && !player.hasPermission(permission)) {
            return;
        }

        ItemStack newItem = item.clone();
        if (getConfigurationSection() != null && getConfigurationSection().contains("username")) {
            newItem = new ItemStackBuilder(newItem).setSkullOwner(player.getName()).build();
        }

        player.getInventory().setItem(slot, newItem);
    }

    public void removeItem(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItem(slot);

        if (item != null && new NBTItem(item).getString("hotbarItem").equals(key)) {
            inventory.remove(inventory.getItem(slot));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!allowMovement) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (getHotbarManager().inDisabledWorld(player.getLocation())) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (event.getSlot() == slot && new NBTItem(clicked).getString("hotbarItem").equals(key)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void hotbarItemInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (XReflection.supports(9) && event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (getHotbarManager().inDisabledWorld(player.getLocation())) {
            return;
        } else if (item.getType() == Material.AIR) {
            return;
        } else if (!new NBTItem(item).getString("hotbarItem").equals(key)) {
            return;
        }

        onInteract(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hotbarPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!getHotbarManager().inDisabledWorld(player.getLocation())) {
            giveItem(player);
        }
    }

    @EventHandler
    public void hotbarPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!getHotbarManager().inDisabledWorld(player.getLocation())) {
            removeItem(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hotbarWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        scheduler.runLater(task -> {
            if (getHotbarManager().inDisabledWorld(player.getLocation())) {
                removeItem(player);
            } else {
                giveItem(player);
            }
        }, 5L);
    }

    @EventHandler
    public void hotbarPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!getHotbarManager().inDisabledWorld(player.getLocation())) {
            giveItem(player);
        }
    }
}
