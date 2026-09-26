package net.zithium.deluxehub.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class TextUtil {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '&' + "[0-9A-FK-OR]");

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Pattern CUSTOM_PATTERN = Pattern.compile("<[^>]+>");
    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"), Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"), Map.entry('7', "gray"), Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"), Map.entry('a', "green"), Map.entry('b', "aqua"),
            Map.entry('c', "red"), Map.entry('d', "light_purple"), Map.entry('e', "yellow"),
            Map.entry('f', "white"), Map.entry('k', "obfuscated"), Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"), Map.entry('n', "underlined"), Map.entry('o', "italic"),
            Map.entry('r', "reset")
    );

    public static String fromList(List<?> list) {
        if (list == null || list.isEmpty()) return null;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            if (ChatColor.stripColor(list.get(i).toString()).isEmpty()) {
                builder.append("\n&r");
            } else {
                builder.append(list.get(i).toString()).append(i + 1 != list.size() ? "\n" : "");
            }
        }

        return builder.toString();
    }

    public static String joinString(int index, String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            builder.append(args[i]).append(" ");
        }

        return builder.toString();
    }

    public static Color getColor(String s) {
        return switch (s.toUpperCase()) {
            case "AQUA" -> Color.AQUA;
            case "BLACK" -> Color.BLACK;
            case "BLUE" -> Color.BLUE;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "GRAY" -> Color.GRAY;
            case "GREEN" -> Color.GREEN;
            case "LIME" -> Color.LIME;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "RED" -> Color.RED;
            case "SILVER" -> Color.SILVER;
            case "TEAL" -> Color.TEAL;
            case "WHITE" -> Color.WHITE;
            case "YELLOW" -> Color.YELLOW;
            default -> null;
        };
    }

    public static Component color(String input) {
        return centerText(input);
    }

    /** Parses MiniMessage while retaining support for legacy ampersand and section codes. */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        String trimmed = input.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            try {
                return GsonComponentSerializer.gson().deserialize(trimmed);
            } catch (RuntimeException ignored) {
                // It may be ordinary text beginning with a brace or a bracket.
            }
        }

        String normalized = input.replaceAll("(?i)&([0-9A-FK-OR])", "§$1");
        normalized = normalized.replaceAll("(?i)&#([0-9A-F]{6})", "<#$1>");
        normalized = normalized.replaceAll("(?i)\\[COLOR=(#[0-9A-F]{6})\\]", "<color:$1>")
                .replaceAll("(?i)\\[/COLOR\\]", "</color>");
        var hex = Pattern.compile("(?i)§x(?:§([0-9A-F])){6}").matcher(normalized);
        StringBuffer buffer = new StringBuffer();
        while (hex.find()) {
            String sequence = hex.group();
            String color = sequence.replaceAll("(?i)§x|§", "");
            hex.appendReplacement(buffer, "<#$color>");
        }
        hex.appendTail(buffer);
        normalized = buffer.toString().replaceAll("(?i)§([0-9A-FK-OR])", "<$1>");
        var codes = Pattern.compile("<([0-9A-FK-OR])>", Pattern.CASE_INSENSITIVE).matcher(normalized);
        buffer = new StringBuffer();
        while (codes.find()) {
            char code = codes.group(1).toLowerCase(Locale.ROOT).charAt(0);
            codes.appendReplacement(buffer, "<" + LEGACY_TAGS.get(code) + ">");
        }
        codes.appendTail(buffer);
        normalized = buffer.toString();
        return MiniMessage.miniMessage().deserialize(normalized);
    }

    public static String legacy(String input) {
        return LegacyComponentSerializer.legacySection().serialize(parse(input));
    }

    /**
     * Centers the provided text in Minecraft chat.
     *
     * @param text The text to be centered.
     * @return The centered text as a Component.
     */
    public static Component centerText(String text) {
        if (text.contains("<center>")) {
            int strippedLength = stripColorAndCustomCodes(text).length();
            int TOTAL_WIDTH = 60;
            int spacesNeeded = Math.max(0, (TOTAL_WIDTH - strippedLength) / 2);
            TextComponent.Builder centeredText = Component.text();
            for (int i = 0; i < spacesNeeded; i++) {
                centeredText.append(Component.text(" "));
            }
            centeredText.append(parse(text.replace("<center>", "")));

            return centeredText.build();
        } else {
            return parse(text);
        }
    }

    // Helper method to strip color and custom codes
    private static String stripColorAndCustomCodes(String text) {
        // Remove Minecraft color codes
        String strippedText = COLOR_PATTERN.matcher(text).replaceAll("");

        // Remove custom codes
        strippedText = CUSTOM_PATTERN.matcher(strippedText).replaceAll("");

        // Replace legacy formatting codes with modern formatting tags
        return strippedText;
    }
}
