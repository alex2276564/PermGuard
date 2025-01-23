package uz.alex2276564.permguard.utils;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import uz.alex2276564.permguard.PermGuard;

import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static FileConfiguration config;
    @Getter
    private static List<Map<String, Object>> restrictedPermissions;
    @Getter
    private static boolean telegramEnabled;
    @Getter
    private static String telegramBotToken;
    @Getter
    private static String[] telegramChatIds;
    @Getter
    private static int telegramMaxRetries;
    @Getter
    private static long telegramRetryDelay;
    @Getter
    private static String telegramMessage;

    public static void reload() {
        Plugin plugin = PermGuard.getInstance();

        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig() {
        restrictedPermissions = (List<Map<String, Object>>) (Object) config.getMapList("restrictedPermissions");

        telegramEnabled = config.getBoolean("telegram.enabled", false);
        if (telegramEnabled) {
            telegramBotToken = config.getString("telegram.bot-token", "");
            if (telegramBotToken.equals("your_bot_token_here") || telegramBotToken.isEmpty()) {
                PermGuard.getInstance().getLogger().warning("Telegram notifications are enabled but bot token is not configured!");
                telegramEnabled = false;
            }

            String chatIdsString = config.getString("telegram.chat-ids", "");
            if (chatIdsString.equals("123456789,987654321") || chatIdsString.isEmpty()) {
                PermGuard.getInstance().getLogger().warning("Telegram notifications are enabled but chat IDs are not configured!");
                telegramEnabled = false;
            }
            telegramChatIds = chatIdsString.split(",");

            telegramMaxRetries = config.getInt("telegram.max-retries", 0);

            telegramRetryDelay = config.getLong("telegram.retry-delay", 1100);

            telegramMessage = config.getString("telegram.message",
                    "⚠️ Security Alert!\\n\\nPlayer %player% tried to join with restricted permission %permission% and was kicked\\n\\n\uD83D\uDCCD Details:\\n\uD83D\uDC64 Player: %player%\\n\uD83D\uDD12 Permission: %permission%\\n\uD83C\uDF10 IP: %ip%\\n\uD83D\uDDFA️ Country: %country%\\n⏰ Time: %date%\\n\\n❗If this wasn't authorized by you, please take immediate action to secure your server.");
        }
    }

    private ConfigManager() {
        throw new IllegalStateException("Utility class");
    }

}
