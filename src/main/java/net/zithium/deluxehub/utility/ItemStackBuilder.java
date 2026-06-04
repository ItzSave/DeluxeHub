package net.zithium.deluxehub.utility;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.zithium.library.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemStackBuilder {

    private final ItemStack ITEM_STACK;

    private static final Multimap<Attribute, AttributeModifier> EMPTY_ATTRIBUTES_MAP =
            MultimapBuilder.hashKeys().hashSetValues().build();

    public ItemStackBuilder(ItemStack item) {
        this.ITEM_STACK = item;
    }

    public static @NotNull ItemStackBuilder getItemStack(@NotNull ConfigurationSection section, Player player) {
        ItemStack item = XMaterial.matchXMaterial(section.getString("material").toUpperCase()).get().parseItem();

        if (item.getType() == Material.PLAYER_HEAD && section.contains("base64")) {
            item = Base64Util.getBaseHead(section.getString("base64")).clone();
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

    public static @NotNull ItemStackBuilder getItemStack(ConfigurationSection section) {
        return getItemStack(section, null);
    }

    public ItemStackBuilder withAmount(Integer amount) {
        ITEM_STACK.setAmount(Optional.ofNullable(amount).orElse(1));
        return this;
    }

    public ItemStackBuilder withFlags(ItemFlag... flags) {
        ItemMeta meta = ITEM_STACK.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            for (ItemFlag itemFlag : flags) {
                if (itemFlag == ItemFlag.HIDE_ATTRIBUTES) {
                    meta.setAttributeModifiers(EMPTY_ATTRIBUTES_MAP);
                    break;
                }
            }
        }

        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withName(String name) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.setDisplayName(ColorUtil.color(name));
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withName(String name, Player player) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.setDisplayName(ColorUtil.color(PlaceholderUtil.setPlaceholders(name, player)));
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder setSkullOwner(String owner) {
        ItemMeta meta = ITEM_STACK.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(owner);
            ITEM_STACK.setItemMeta(skullMeta);
        }

        return this;
    }

    public ItemStackBuilder withLore(List<String> lore, Player player) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        List<String> coloredLore = new ArrayList<>();
        for (String s : lore) {
            s = PlaceholderUtil.setPlaceholders(s, player);
            coloredLore.add(ColorUtil.color(s));
        }

        meta.setLore(coloredLore);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withLore(List<String> lore) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        List<String> coloredLore = new ArrayList<>();
        for (String s : lore) {
            coloredLore.add(ColorUtil.color(s));
        }

        meta.setLore(coloredLore);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    private ItemStackBuilder withCustomModelData(int data) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.setCustomModelData(data);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    private ItemStackBuilder withGlow() {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        ITEM_STACK.setItemMeta(meta);
        ITEM_STACK.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        return this;
    }

    public ItemStack build() {
        return ITEM_STACK;
    }
}
