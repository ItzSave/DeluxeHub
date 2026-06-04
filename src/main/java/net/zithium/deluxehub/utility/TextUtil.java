package net.zithium.deluxehub.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.List;
import java.util.regex.Pattern;

public class TextUtil {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '&' + "[0-9A-FK-OR]");

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Pattern CUSTOM_PATTERN = Pattern.compile("<[^>]+>");

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
            centeredText.append(MiniMessage.miniMessage().deserialize(text.replace("<center>", "")));

            return centeredText.build();
        } else {
            return MiniMessage.miniMessage().deserialize(text);
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
