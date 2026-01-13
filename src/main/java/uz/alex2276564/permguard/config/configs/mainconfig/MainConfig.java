package uz.alex2276564.permguard.config.configs.mainconfig;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

public class MainConfig extends OkaeriConfig {

    @Comment("# ================================================================")
    @Comment("# 🎯 Main Configuration")
    @Comment("# ================================================================")
    @Comment("# 📖 Documentation: https://github.com/alex2276564/PermGuard")
    @Comment("# 💬 Support: https://github.com/alex2276564/PermGuard/issues")
    @Comment("# ================================================================")
    @Comment("")
    @Comment("General settings")
    public SettingsSection settings = new SettingsSection();

    @Comment("")
    @Comment("Logging settings")
    public LoggingSection logging = new LoggingSection();

    @Comment("")
    @Comment("Telegram notification settings")
    public TelegramSection telegram = new TelegramSection();

    public static class SettingsSection extends OkaeriConfig {
        @Comment("Whether to shutdown the server when PermGuard is disabled")
        @Comment("This provides additional security by ensuring no security gaps are left open")
        @Comment("Set to false if you want to disable the plugin without shutting down the server")
        public boolean shutdownOnDisable = true;
    }

    public static class LoggingSection extends OkaeriConfig {
        @Comment("File name for the violations log (saved under the plugin's data folder)")
        @Comment("Must be a simple file name ending with .log")
        public String violationsFile = "violations.log";

        @Comment("")
        @Comment("Whether to sanitize player names before writing them to logs.")
        @Comment("true  = strip control characters and other potentially problematic chars")
        @Comment("false = write raw player names as returned by Bukkit (safe on online-mode servers)")
        @Comment("")
        @Comment("Recommendation:")
        @Comment(" - Keep this enabled (true) on offline/cracked servers or when you copy logs into")
        @Comment("   external systems that may be sensitive to bad/control characters.")
        @Comment(" - On normal online-mode servers this setting usually makes no visible difference.")
        public boolean sanitizePlayerNames = true;
    }

    public static class TelegramSection extends OkaeriConfig {
        @Comment("Enable or disable Telegram notifications")
        public boolean enabled = false;

        @Comment("")
        @Comment("Your Telegram bot token (get it from @BotFather).")
        @Comment("Steps:")
        @Comment("  1) Open Telegram and talk to @BotFather.")
        @Comment("  2) Create a new bot or reuse an existing one.")
        @Comment("  3) Copy the HTTP API token and paste it here.")
        public String botToken = "your_bot_token_here";

        @Comment("")
        @Comment("Chat IDs where notifications will be sent (separate multiple IDs with commas)")
        @Comment("")
        @Comment("How to get your Chat ID:")
        @Comment("")
        @Comment("Quick start (personal chat, recommended for first setup):")
        @Comment("  1) Send any message to your bot (e.g. \"hello\").")
        @Comment("2. Open the following link in your browser (replace <YourBOTToken> with your bot token):")
        @Comment("   https://api.telegram.org/bot<YourBOTToken>/getUpdates")
        @Comment("3. In the JSON response, look for \"chat\": {\"id\": ...} — this is your Chat ID.")
        @Comment("4. Add this Chat ID to the configuration below (example: 123456789).")
        @Comment("")
        @Comment("How to add the bot to a group:")
        @Comment("1. Add the bot to the group.")
        @Comment("2. Send a message in the group.")
        @Comment("3. Visit https://api.telegram.org/bot<YourBOTToken>/getUpdates")
        @Comment("4. Find the group Chat ID (it will start with a \"-\", e.g., -987654321).")
        @Comment("5. Add this ID to the configuration below.")
        @Comment("")
        @Comment("How to add the bot to a channel:")
        @Comment("1. Add the bot as an administrator of the channel.")
        @Comment("2. Open Web Telegram (https://web.telegram.org/a/) and go to the channel.")
        @Comment("3. Look at the URL in your browser; it will be something like:")
        @Comment("   https://web.telegram.org/a/#-1001234567890")
        @Comment("4. Channel Chat IDs always start with \"-100\" (e.g., -1001234567890).")
        @Comment("5. Add this Chat ID to the configuration below.")
        @Comment("")
        @Comment("Telegram API Limit:")
        @Comment("You can enter up to 30 Chat IDs in total (Telegram API restriction).")
        @Comment("")
        @Comment("You can add multiple Chat IDs separated by commas (e.g., 123456789, -987654321, -1001234567890).")
        public String chatIds = "123456789,987654321";

        @Comment("")
        @Comment("Number of retry attempts if sending fails")
        @Comment("Set to 0 for dedicated hosting with stable network (recommended)")
        @Comment("Increase this value (1-3) for shared hosting or unstable network")
        @Comment("Note: Telegram API can sometimes return incorrect responses,")
        @Comment("so it's better to keep this at 0 on stable connections")
        public int maxRetries = 0;

        @Comment("")
        @Comment("Delay between retry attempts in milliseconds")
        @Comment("Only used if max-retries > 0")
        public long retryDelay = 1100;

        @Comment("")
        @Comment("Notification message template.")
        @Comment("Available placeholders:")
        @Comment("  %player%     - player name")
        @Comment("  %permission% - restricted permission")
        @Comment("  %ip%         - player's IP address")
        @Comment("  %country%    - player's country (based on IP)")
        @Comment("  %date%       - date and time of the incident (server time)")
        public String message = "⚠️ Security Alert!\\n\\nPlayer %player% tried to join with restricted permission %permission% and was kicked\\n\\n📍 Details:\\n👤 Player: %player%\\n🔒 Permission: %permission%\\n🌐 IP: %ip%\\n🗺️ Country: %country%\\n⏰ Time: %date% (Server time)\\n\\n❗If this wasn't authorized by you, please take immediate action to secure your server.";

        public boolean isConfigured() {
            return enabled &&
                    !botToken.equals("your_bot_token_here") &&
                    !botToken.isEmpty() &&
                    !chatIds.equals("123456789,987654321");
        }

        public String[] getChatIdsArray() {
            return chatIds.split(",");
        }
    }
}