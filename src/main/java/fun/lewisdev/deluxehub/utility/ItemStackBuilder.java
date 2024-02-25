package fun.lewisdev.deluxehub.utility;

import fun.lewisdev.deluxehub.DeluxeHubPlugin;
import fun.lewisdev.deluxehub.hook.hooks.head.HeadHook;
import fun.lewisdev.deluxehub.utility.universal.XMaterial;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder {

    private final ItemStack ITEM_STACK;

    private static final DeluxeHubPlugin PLUGIN = JavaPlugin.getPlugin(DeluxeHubPlugin.class);

    public ItemStackBuilder(ItemStack item) {
        this.ITEM_STACK = item;
    }

    public static ItemStackBuilder getItemStack(ConfigurationSection section, Player player) {
        ItemStack item = XMaterial.matchXMaterial(section.getString("material").toUpperCase()).get().parseItem();

        if (item.getType() == XMaterial.PLAYER_HEAD.parseMaterial()) {
            if (section.contains("base64")) {
                item = ((HeadHook) PLUGIN.getHookManager().getPluginHook("BASE64")).getHead(section.getString("base64")).clone();
            } else if (section.contains("hdb") && PLUGIN.getHookManager().isHookEnabled("HEAD_DATABASE")) {
                item = ((HeadHook) PLUGIN.getHookManager().getPluginHook("HEAD_DATABASE")).getHead(section.getString("hdb"));
            }
        }

        ItemStackBuilder builder = new ItemStackBuilder(item);

        if (section.contains("amount")) {
            builder.withAmount(section.getInt("amount"));
        }

        if (section.contains("username") && player != null) {
            builder.setSkullOwner(section.getString("username").replace("%player%", player.getName()));
        }

        if (section.contains("display_name")) {
            if (player != null) builder.withName(section.getString("display_name"), player);
            else builder.withName(section.getString("display_name"));
        }

        if (section.contains("lore")) {
            if (player != null) builder.withLore(section.getStringList("lore"), player);
            else builder.withLore(section.getStringList("lore"));
        }

        if (section.contains("glow") && section.getBoolean("glow")) {
            builder.withGlow();
        }

        if (section.contains("custom_model_data")) {
            builder.withCustomModelData(section.getInt("custom_model_data"));
        }

        if (section.contains("item_flags")) {
            List<ItemFlag> flags = new ArrayList<>();
            section.getStringList("item_flags").forEach(text -> {
                try {
                    ItemFlag flag = ItemFlag.valueOf(text);
                    flags.add(flag);
                } catch (IllegalArgumentException ignored) {
                }
            });
            builder.withFlags(flags.toArray(new ItemFlag[0]));
        }

        return builder;
    }

    public static ItemStackBuilder getItemStack(ConfigurationSection section) {
        return getItemStack(section, null);
    }

    public ItemStackBuilder withAmount(int amount) {
        ITEM_STACK.setAmount(amount);
        return this;
    }

    public ItemStackBuilder withFlags(ItemFlag... flags) {
        ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.addItemFlags(flags);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withName(String name) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();

        if (ITEM_STACK == XMaterial.AIR.parseItem()) {
            return this;
        }

        meta.setDisplayName(ColorUtil.color(name));
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withName(String name, Player player) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();

        if (ITEM_STACK == XMaterial.AIR.parseItem()) {
            return this;
        }

        meta.setDisplayName(ColorUtil.color(PlaceholderUtil.setPlaceholders(name, player)));
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder setSkullOwner(String owner) {
        try {
            SkullMeta im = (SkullMeta) ITEM_STACK.getItemMeta();
            im.setOwner(owner);
            ITEM_STACK.setItemMeta(im);
        } catch (ClassCastException expected) {
        }
        return this;
    }

    public ItemStackBuilder withLore(List<String> lore, Player player) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();

        if (ITEM_STACK == XMaterial.AIR.parseItem()) {
            return this;
        }

        List<String> coloredLore = new ArrayList<String>();
        for (String s : lore) {
            s = PlaceholderUtil.setPlaceholders(s, player);
            coloredLore.add(ColorUtil.color(s));
        }
        meta.setLore(coloredLore);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withLore(List<String> lore) {

        if (ITEM_STACK == XMaterial.AIR.parseItem()) {
            return this;
        }

        final ItemMeta meta = ITEM_STACK.getItemMeta();
        List<String> coloredLore = new ArrayList<String>();
        for (String s : lore) {
            coloredLore.add(ColorUtil.color(s));
        }
        meta.setLore(coloredLore);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withCustomModelData(int data) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.setCustomModelData(data);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withGlow() {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        ITEM_STACK.setItemMeta(meta);
        ITEM_STACK.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return this;
    }

    public ItemStack build() {
        return ITEM_STACK;
    }
}

