package uz.alex2276564.permguard.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import uz.alex2276564.permguard.PermGuard;

import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final PermGuard plugin;
    private FileConfiguration config;

    @Getter
    private List<Map<String, Object>> restrictedPermissions;

    @Getter
    private boolean telegramEnabled;

    @Getter
    private String telegramBotToken;

    @Getter
    private String[] telegramChatIds;

    @Getter
    private int telegramMaxRetries;

    @Getter
    private long telegramRetryDelay;

    @Getter
    private String telegramMessage;

    public ConfigManager(PermGuard plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        restrictedPermissions = (List<Map<String, Object>>) (Object) config.getMapList("restrictedPermissions");

        telegramEnabled = config.getBoolean("telegram.enabled", false);
        if (telegramEnabled) {
            telegramBotToken = config.getString("telegram.bot-token", "");
            if (telegramBotToken.equals("your_bot_token_here") || telegramBotToken.isEmpty()) {
                plugin.getLogger().warning("Telegram notifications are enabled but bot token is not configured!");
                telegramEnabled = false;
            }

            String chatIdsString = config.getString("telegram.chat-ids", "");
            if (chatIdsString.equals("123456789,987654321") || chatIdsString.isEmpty()) {
                plugin.getLogger().warning("Telegram notifications are enabled but chat IDs are not configured!");
                telegramEnabled = false;
            }
            telegramChatIds = chatIdsString.split(",");

            telegramMaxRetries = config.getInt("telegram.max-retries", 0);

            telegramRetryDelay = config.getLong("telegram.retry-delay", 1100);

            telegramMessage = config.getString("telegram.message",
                    "⚠️ Security Alert!\\n\\nPlayer %player% tried to join with restricted permission %permission% and was kicked\\n\\n\uD83D\uDCCD Details:\\n\uD83D\uDC64 Player: %player%\\n\uD83D\uDD12 Permission: %permission%\\n\uD83C\uDF10 IP: %ip%\\n\uD83D\uDDFA️ Country: %country%\\n⏰ Time: %date% (Server time)\\n\\n❗If this wasn't authorized by you, please take immediate action to secure your server.");
        }
    }
}
