package net.zithium.deluxehub.inventory.condition;

import net.zithium.deluxehub.utility.PlaceholderUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and evaluates condition strings for menu items.
 *
 * Supports:
 * - Logical operators: AND, OR, NOT
 * - Comparison operators: EQUALS, EQUALSIGNORECASE, NOTEQUALS, CONTAINS, STARTSWITH, ENDSWITH
 * - Numeric operators: ATLEAST, ATMOST, GREATERTHAN, LESSTHAN
 * - Permission operators: HASPERM, NOTHASPERM
 * - String operators: MATCHES (regex)
 * - Grouping with single-depth brackets
 * - PlaceholderAPI integration
 *
 * Example conditions:
 * - "%player_name% EQUALS Notch"
 * - "%player_level% ATLEAST 10"
 * - "%player_world% CONTAINS lobby"
 * - "%player_gamemode% NOTEQUALS SPECTATOR"
 * - "HASPERM deluxehub.vip"
 * - "(%player_name% EQUALS Notch) OR (HASPERM deluxehub.admin)"
 * - "NOT HASPERM deluxehub.banned"
 */
public class ConditionParser {

    private static final Pattern BRACKET_PATTERN = Pattern.compile("\\(([^()]+)\\)");

    /**
     * Parses a condition string into a Condition object.
     *
     * @param conditionString The condition string to parse
     * @return A Condition that can be evaluated
     */
    public static Condition parse(String conditionString) {
        if (conditionString == null || conditionString.trim().isEmpty()) {
            return player -> true; // No condition = always true
        }

        return player -> evaluate(conditionString.trim(), player);
    }

    /**
     * Evaluates a condition string for a specific player.
     *
     * @param condition The condition string
     * @param player The player to evaluate against
     * @return true if condition is met, false otherwise
     */
    private static boolean evaluate(String condition, Player player) {
        // Store original condition for debugging
        String originalCondition = condition;

        // Replace placeholders first
        condition = PlaceholderUtil.setPlaceholders(condition, player);

        // Handle brackets first (single depth)
        // Note: This replaces bracket groups with their boolean result to avoid
        // confusion with literal parentheses that might appear in placeholder values
        int maxIterations = 100; // Safety limit to prevent infinite loops
        int iterations = 0;
        while (condition.contains("(") && iterations < maxIterations) {
            Matcher matcher = BRACKET_PATTERN.matcher(condition);
            if (!matcher.find()) {
                break; // No valid brackets found
            }

            String bracketContent = matcher.group(1);
            boolean bracketResult = evaluate(bracketContent, player);

            // Use a unique placeholder to avoid conflicts with literal parentheses
            String placeholder = "##BRACKET_RESULT_" + iterations + "##";
            condition = matcher.replaceFirst(placeholder);
            condition = condition.replace(placeholder, String.valueOf(bracketResult));
            iterations++;
        }

        // Handle OR (lower precedence)
        if (condition.contains(" OR ")) {
            String[] parts = condition.split(" OR ", 2);
            return evaluate(parts[0].trim(), player) || evaluate(parts[1].trim(), player);
        }

        // Handle AND
        if (condition.contains(" AND ")) {
            String[] parts = condition.split(" AND ", 2);
            return evaluate(parts[0].trim(), player) && evaluate(parts[1].trim(), player);
        }

        // Handle NOT
        if (condition.startsWith("NOT ")) {
            return !evaluate(condition.substring(4).trim(), player);
        }

        // Handle boolean literals (from bracket evaluation)
        if (condition.equals("true")) {
            return true;
        }
        if (condition.equals("false")) {
            return false;
        }

        // Handle comparison operators
        return evaluateComparison(condition, player);
    }

    /**
     * Evaluates a comparison operation.
     *
     * @param condition The comparison condition
     * @param player The player to evaluate against
     * @return true if comparison is satisfied
     */
    private static boolean evaluateComparison(String condition, Player player) {
        // Permission operators
        if (condition.startsWith("HASPERM ")) {
            String permission = condition.substring(8).trim();
            return player.hasPermission(permission);
        }

        if (condition.startsWith("NOTHASPERM ")) {
            String permission = condition.substring(11).trim();
            return !player.hasPermission(permission);
        }

        // String equality operators (case-insensitive)
        if (condition.contains(" EQUALSIGNORECASE ")) {
            String[] parts = condition.split(" EQUALSIGNORECASE ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim();
            String right = parts[1].trim();
            return left.equalsIgnoreCase(right);
        }

        // String equality operators (case-sensitive)
        if (condition.contains(" EQUALS ")) {
            String[] parts = condition.split(" EQUALS ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim();
            String right = parts[1].trim();
            return left.equals(right);
        }

        // String not equals
        if (condition.contains(" NOTEQUALS ")) {
            String[] parts = condition.split(" NOTEQUALS ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim();
            String right = parts[1].trim();
            return !left.equalsIgnoreCase(right);
        }

        // String contains
        if (condition.contains(" CONTAINS ")) {
            String[] parts = condition.split(" CONTAINS ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim().toLowerCase();
            String right = parts[1].trim().toLowerCase();
            return left.contains(right);
        }

        // String starts with
        if (condition.contains(" STARTSWITH ")) {
            String[] parts = condition.split(" STARTSWITH ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim().toLowerCase();
            String right = parts[1].trim().toLowerCase();
            return left.startsWith(right);
        }

        // String ends with
        if (condition.contains(" ENDSWITH ")) {
            String[] parts = condition.split(" ENDSWITH ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim().toLowerCase();
            String right = parts[1].trim().toLowerCase();
            return left.endsWith(right);
        }

        // Regex matching
        if (condition.contains(" MATCHES ")) {
            String[] parts = condition.split(" MATCHES ", 2);
            if (parts.length != 2) return false;
            String left = parts[0].trim();
            String pattern = parts[1].trim();
            try {
                return left.matches(pattern);
            } catch (Exception e) {
                return false;
            }
        }

        // Numeric: At least (greater than or equal)
        if (condition.contains(" ATLEAST ")) {
            String[] parts = condition.split(" ATLEAST ", 2);
            if (parts.length != 2) return false;

            try {
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left >= right;
            } catch (NumberFormatException e) {
                // Debug: Log failed parsing to help identify placeholder issues
                if (parts[0].trim().contains("%")) {
                    System.out.println("[DeluxeHub] Failed to parse condition - placeholder not replaced: " + parts[0].trim());
                }
                return false;
            }
        }

        // Numeric: At most (less than or equal)
        if (condition.contains(" ATMOST ")) {
            String[] parts = condition.split(" ATMOST ", 2);
            if (parts.length != 2) return false;

            try {
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left <= right;
            } catch (NumberFormatException e) {
                if (parts[0].trim().contains("%")) {
                    System.out.println("[DeluxeHub] Failed to parse condition - placeholder not replaced: " + parts[0].trim());
                }
                return false;
            }
        }

        // Numeric: Greater than
        if (condition.contains(" GREATERTHAN ")) {
            String[] parts = condition.split(" GREATERTHAN ", 2);
            if (parts.length != 2) return false;

            try {
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left > right;
            } catch (NumberFormatException e) {
                if (parts[0].trim().contains("%")) {
                    System.out.println("[DeluxeHub] Failed to parse condition - placeholder not replaced: " + parts[0].trim());
                }
                return false;
            }
        }

        // Numeric: Less than
        if (condition.contains(" LESSTHAN ")) {
            String[] parts = condition.split(" LESSTHAN ", 2);
            if (parts.length != 2) return false;

            try {
                double left = Double.parseDouble(parts[0].trim());
                double right = Double.parseDouble(parts[1].trim());
                return left < right;
            } catch (NumberFormatException e) {
                if (parts[0].trim().contains("%")) {
                    System.out.println("[DeluxeHub] Failed to parse condition - placeholder not replaced: " + parts[0].trim());
                }
                return false;
            }
        }

        // No valid operator found - treat as boolean permission check for backwards compatibility
        // Log warning if this might be a typo (contains common operator keywords)
        String upperCondition = condition.toUpperCase();
        if (upperCondition.contains("EQUAL") || upperCondition.contains("THAN") ||
            upperCondition.contains("LEAST") || upperCondition.contains("MOST") ||
            upperCondition.contains("CONTAIN") || upperCondition.contains("START") ||
            upperCondition.contains("END") || upperCondition.contains("MATCH")) {
            System.out.println("[DeluxeHub] Warning: Unrecognized condition operator in: '" + condition + "'. Treating as permission check. Check for typos!");
        }
        return player.hasPermission(condition);
    }

    /**
     * Parses multiple condition strings and combines them with AND logic.
     *
     * @param conditions List of condition strings
     * @return A single Condition that represents all conditions ANDed together
     */
    public static Condition parseMultiple(List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return player -> true;
        }

        List<Condition> parsedConditions = new ArrayList<>();
        for (String conditionString : conditions) {
            parsedConditions.add(parse(conditionString));
        }

        return player -> {
            for (Condition condition : parsedConditions) {
                if (!condition.evaluate(player)) {
                    return false;
                }
            }
            return true;
        };
    }
}

