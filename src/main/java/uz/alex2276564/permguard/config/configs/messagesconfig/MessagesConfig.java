package uz.alex2276564.permguard.config.configs.messagesconfig;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.HashSet;
import java.util.Set;

public class MessagesConfig extends OkaeriConfig {

    @Comment("# ================================================================")
    @Comment("# 💬 Messages Configuration")
    @Comment("# ================================================================")
    @Comment("# 🎨 TEXT FORMATTING:")
    @Comment("# • Full MiniMessage support on Paper 1.18+")
    @Comment("# • Automatic fallback to legacy colors on older versions")
    @Comment("# • Examples: <red>Error!</red>, <green>Success!</green>")
    @Comment("# • Advanced: gradients, hover effects, click events")
    @Comment("# • Web editor: https://webui.advntr.dev/")
    @Comment("#")
    @Comment("# 🌍 LOCALIZATION:")
    @Comment("# • This plugin doesn't include built-in multi-language support")
    @Comment("# • For multiple languages, use Triton plugin:")
    @Comment("#   → https://www.spigotmc.org/resources/triton.30331/")
    @Comment("# ================================================================")
    @Comment("")

    @Comment("# ================================================================")
    @Comment("# 🔇 MESSAGE CONTROL SYSTEM")
    @Comment("# ================================================================")
    @Comment("# You can selectively disable individual messages by adding their")
    @Comment("# keys to the list below. This is useful for customizing user")
    @Comment("# experience without editing every message.")
    @Comment("#")
    @Comment("# 📝 HOW TO USE:")
    @Comment("# 1. Find the message you want to disable in this config")
    @Comment("# 2. Copy its full path using dot notation")
    @Comment("# 3. Add the path to disabledKeys list below")
    @Comment("# 4. Reload the plugin")
    @Comment("#")
    @Comment("# 🎯 EXAMPLES:")
    @Comment("# To disable specific command feedback:")
    @Comment("# - 'commands.reload.success'")
    @Comment("# - 'commands.help.header'")
    @Comment("#")
    @Comment("# To disable general system messages:")
    @Comment("# - 'general.noSpawnFound'")
    @Comment("# - 'general.systemDisabled'")
    @Comment("#")
    @Comment("# ⚠️ IMPORTANT NOTES:")
    @Comment("# • Keys are case-sensitive and must match exactly")
    @Comment("# • This affects ALL recipients (players AND console)")
    @Comment("# • Some messages may be important for debugging")
    @Comment("# • Also keep in mind that some messages are hardcoded")
    @Comment("# • into the plugin logic and cannot be disabled")
    @Comment("# • Do NOT use empty strings (\"\") to disable messages")
    @Comment("# • Do NOT delete message entries - use this system instead")
    @Comment("#")
    @Comment("# 🔍 FINDING KEYS:")
    @Comment("# Structure follows: section.subsection.messageKey")
    @Comment("# Check the organization below to find the correct path")
    @Comment("# ================================================================")
    @Comment("")
    public Set<String> disabledKeys = new HashSet<>();
    @Comment("")
    @Comment("Command messages")
    public CommandsSection commands = new CommandsSection();

    @Comment("")
    @Comment("General messages")
    public GeneralSection general = new GeneralSection();

    @Comment("")
    @Comment("Logging (server console / file) messages and templates")
    public LoggingSection logging = new LoggingSection();

    @Comment("")
    @Comment("Telegram-related log messages")
    public TelegramMessagesSection telegramMessages = new TelegramMessagesSection();

    public static class CommandsSection extends OkaeriConfig {
        @Comment("Help command messages")
        public HelpSection help = new HelpSection();

        @Comment("")
        @Comment("Reload command messages")
        public ReloadSection reload = new ReloadSection();

        public static class HelpSection extends OkaeriConfig {
            @Comment("Help command header")
            public String header = "<gold>=== PermGuard Help ===";

            @Comment("Reload command help line")
            public String reloadLine = "<yellow>/permguard reload <type> <gray>- Reload the plugin configuration";

            @Comment("Help command help line")
            public String helpLine = "<yellow>/permguard help <gray>- Show this help message";
        }

        public static class ReloadSection extends OkaeriConfig {
            @Comment("Reload success message. <type> = config type")
            public String success = "<green>PermGuard configuration successfully reloaded (<type>).";

            @Comment("Reload error message. <error> = error details")
            public String error = "<red>Failed to reload configuration: <error>";
        }
    }

    public static class GeneralSection extends OkaeriConfig {
        @Comment("Shown when player has wildcard permission and needs to remove it first")
        public String wildcardPermissionConflict = "<red>[PermGuard] You currently have the wildcard permission (*). Remove it before revoking other permissions.";
    }

    public static class LoggingSection extends OkaeriConfig {
        @Comment("Template for a violation entry written to the log file and console")
        @Comment("Placeholders: <date>, <player>, <permission>, <ip>")
        public String violationEntry = "[<date>] Player <player> tried to join with restricted permission <permission> from IP <ip>";

        @Comment("Error message when writing to the log file fails. <error> = exception message")
        public String fileWriteError = "Could not write to log file: <error>";

        @Comment("Warning when a dangerous character was blocked in a command input. Placeholders: <char>, <input>")
        public String dangerousCharBlocked = "Blocked dangerous char '<char>' in: <input>";
    }

    public static class TelegramMessagesSection extends OkaeriConfig {
        @Comment("Generic 'send failed' log. <error> = exception message")
        public String sendFailed = "Telegram send failed: <error>";

        @Comment("Top-level notification failure. <error> = exception message")
        public String notificationFailed = "Failed to send Telegram notification: <error>";

        @Comment("Per-attempt failure log. Placeholders: <attempt>, <max>, <error>")
        public String sendFailedAttempt = "Failed to send Telegram notification (attempt <attempt>/<max>): <error>";

        @Comment("Used when rate-limited after all retries")
        public String tooManyRequests = "Too Many Requests (429) after max retries";

        @Comment("IP geolocation failure. Placeholders: <ip>, <error>")
        public String countryLookupFailed = "Failed to get country for IP <ip>: <error>";

        @Comment("Fallback country name when unknown")
        public String unknownCountry = "Unknown";
    }
}