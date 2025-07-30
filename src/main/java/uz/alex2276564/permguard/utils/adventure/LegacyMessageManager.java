package uz.alex2276564.permguard.utils.adventure;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.regex.Pattern;

public class LegacyMessageManager implements MessageManager {

    // Patterns for converting MiniMessage tags to ChatColor
    private static final Pattern COLOR_PATTERN = Pattern.compile("<(red|blue|green|yellow|aqua|light_purple|gold|gray|dark_red|dark_blue|dark_green|dark_aqua|dark_purple|dark_gray|black|white)>");
    private static final Pattern STYLE_PATTERN = Pattern.compile("<(bold|italic|underlined|strikethrough|obfuscated)>");
    private static final Pattern RESET_PATTERN = Pattern.compile("<reset>");
    private static final Pattern CLOSING_TAG_PATTERN = Pattern.compile("</(red|blue|green|yellow|aqua|light_purple|gold|gray|dark_red|dark_blue|dark_green|dark_aqua|dark_purple|dark_gray|black|white|bold|italic|underlined|strikethrough|obfuscated)>");

    // Patterns for new line
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("<(newline|br)>");
    private static final Pattern ESCAPE_NEWLINE_PATTERN = Pattern.compile("\\\\n");

    // Complex tags (gradients, hover, etc.) - just remove them
    private static final Pattern COMPLEX_TAG_PATTERN = Pattern.compile("<(gradient:[^>]+|hover:[^>]+|click:[^>]+|font:[^>]+|insertion:[^>]+|key:[^>]+|lang:[^>]+|selector:[^>]+|score:[^>]+|nbt:[^>]+)>");
    private static final Pattern CLOSING_COMPLEX_TAG_PATTERN = Pattern.compile("</(gradient|hover|click|font|insertion|key|lang|selector|score|nbt)>");

    @Override
    public @NotNull Component parse(@NotNull String message) {
        String converted = convertMiniMessageToLegacy(message);
        return Component.text(ChatColor.translateAlternateColorCodes('&', converted));
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        return parse(message.replace(placeholder, replacement));
    }

    @Override
    public @NotNull Component parse(@NotNull String message, @NotNull Map<String, String> placeholders) {
        String processedMessage = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processedMessage = processedMessage.replace(entry.getKey(), entry.getValue());
        }
        return parse(processedMessage);
    }

    @Override
    public @NotNull String stripTags(@NotNull String message) {
        String result = message;
        result = NEWLINE_PATTERN.matcher(result).replaceAll(" ");
        result = ESCAPE_NEWLINE_PATTERN.matcher(result).replaceAll(" ");
        result = COLOR_PATTERN.matcher(result).replaceAll("");
        result = STYLE_PATTERN.matcher(result).replaceAll("");
        result = RESET_PATTERN.matcher(result).replaceAll("");
        result = CLOSING_TAG_PATTERN.matcher(result).replaceAll("");
        result = COMPLEX_TAG_PATTERN.matcher(result).replaceAll("");
        result = CLOSING_COMPLEX_TAG_PATTERN.matcher(result).replaceAll("");
        return result;
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message) {
        String converted = convertMiniMessageToLegacy(message);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', converted));
    }

    @Override
    public void sendMessage(@NotNull Player player, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        sendMessage(player, message.replace(placeholder, replacement));
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (sender instanceof Player player) {
            sendMessage(player, message);
        } else {
            // For the console - strip tags
            sender.sendMessage(stripTags(message));
        }
    }

    @Override
    public void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull String placeholder, @NotNull String replacement) {
        sendMessage(sender, message.replace(placeholder, replacement));
    }

    private String convertMiniMessageToLegacy(String message) {
        String result = message;

        // Remove new line
        result = NEWLINE_PATTERN.matcher(result).replaceAll(" ");
        result = ESCAPE_NEWLINE_PATTERN.matcher(result).replaceAll(" ");

        // Convert colors
        result = COLOR_PATTERN.matcher(result).replaceAll(matchResult -> {
            String color = matchResult.group(1);
            return "&" + getColorCode(color);
        });

        // Convert styles
        result = STYLE_PATTERN.matcher(result).replaceAll(matchResult -> {
            String style = matchResult.group(1);
            return "&" + getStyleCode(style);
        });

        // Handle reset
        result = RESET_PATTERN.matcher(result).replaceAll("&r");

        // Remove closing tags (legacy doesn't have closing)
        result = CLOSING_TAG_PATTERN.matcher(result).replaceAll("");

        // Remove complex tags
        result = COMPLEX_TAG_PATTERN.matcher(result).replaceAll("");
        result = CLOSING_COMPLEX_TAG_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    private String getColorCode(String colorName) {
        return switch (colorName.toLowerCase()) {
            case "black" -> "0";
            case "dark_blue" -> "1";
            case "dark_green" -> "2";
            case "dark_aqua" -> "3";
            case "dark_red" -> "4";
            case "dark_purple" -> "5";
            case "gold" -> "6";
            case "gray" -> "7";
            case "dark_gray" -> "8";
            case "blue" -> "9";
            case "green" -> "a";
            case "aqua" -> "b";
            case "red" -> "c";
            case "light_purple" -> "d";
            case "yellow" -> "e";
            default -> "f";
        };
    }

    private String getStyleCode(String styleName) {
        return switch (styleName.toLowerCase()) {
            case "obfuscated" -> "k";
            case "bold" -> "l";
            case "strikethrough" -> "m";
            case "underlined" -> "n";
            case "italic" -> "o";
            default -> "r";
        };
    }
}
