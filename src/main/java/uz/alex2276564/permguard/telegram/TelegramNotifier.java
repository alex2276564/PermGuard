package uz.alex2276564.permguard.telegram;

import com.alibaba.fastjson2.JSONObject;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.utils.HttpUtils;
import uz.alex2276564.permguard.utils.SecurityUtils;
import uz.alex2276564.permguard.utils.StringUtils;

/**
 * High-level service responsible for:
 * - building Telegram notification messages,
 * - resolving country by IP,
 * - enqueuing send jobs into TelegramSendQueue.
 * <p>
 * All actual network I/O to Telegram Bot API is delegated to TelegramSendQueue,
 * which applies global rate limiting and retry logic.
 */
public class TelegramNotifier {

    // SECURITY NOTE: HTTP is used because ip-api.com requires a paid tier for HTTPS.
    // MITM risks are mitigated by strict input/output verification via SecurityUtils.
    @SuppressWarnings("HttpUrlsUsage")
    private static final String IP_API_URL = "http://ip-api.com/json/%s";

    private final PermGuard plugin;
    private final HttpUtils httpUtils;
    private final TelegramSendQueue sendQueue;

    public TelegramNotifier(PermGuard plugin, HttpUtils httpUtils) {
        this.plugin = plugin;
        this.httpUtils = httpUtils;

        String userAgent = plugin.getDescription().getName()
                + "/" + plugin.getDescription().getVersion();

        long defaultDelayMs = plugin.getConfigManager()
                .getMainConfig()
                .telegram
                .minDelayMs;

        this.sendQueue = new TelegramSendQueue(plugin, httpUtils, userAgent, defaultDelayMs);
    }

    /**
     * Public API used from PlayerJoinListener.
     * <p>
     * This method is expected to be called from an async context (PlayerJoinListener
     * already wraps it in runner.runAsync), because getCountryByIp() performs a
     * blocking HTTP request.
     */
    public void sendNotification(String safeName, String permission, String safeIp, String date) {
        MainConfig.TelegramSection telegram = plugin.getConfigManager().getMainConfig().telegram;

        if (!telegram.enabled || !telegram.isConfigured()) {
            return;
        }

        MessagesConfig.TelegramMessagesSection tmsg =
                plugin.getConfigManager().getMessagesConfig().telegramMessages;

        try {
            String country = getCountryByIp(safeIp);

            String message = StringUtils.processEscapeSequences(telegram.message)
                    .replace("%player%", safeName)
                    .replace("%permission%", permission)
                    .replace("%ip%", safeIp)
                    .replace("%country%", country)
                    .replace("%date%", date);

            int maxAttempts = telegram.maxRetries + 1;

            for (String rawId : telegram.getChatIdsArray()) {
                String chatId = rawId.trim();
                if (chatId.isEmpty()) continue;

                TelegramSendQueue.SendJob job = new TelegramSendQueue.SendJob(
                        telegram,
                        tmsg,
                        chatId,
                        message,
                        1,
                        maxAttempts
                );

                sendQueue.enqueue(job);
            }

        } catch (Exception e) {
            String msg = tmsg.notificationFailed
                    .replace("<error>", String.valueOf(e.getMessage()));
            plugin.getLogger().warning(msg);
        }
    }

    /**
     * Country lookup by IP using ip-api.com.
     * Runs on an async thread (see comment on sendNotification).
     */
    private String getCountryByIp(String safeIp) {
        MessagesConfig.TelegramMessagesSection tmsg =
                plugin.getConfigManager().getMessagesConfig().telegramMessages;

        try {
            String urlString = String.format(IP_API_URL, safeIp);

            HttpUtils.HttpResponse response = httpUtils.getJson(urlString, null);

            if (response.statusCode() == 200) {
                JSONObject json = response.jsonBody();
                if (json.containsKey("country")) {
                    String country = json.getString("country");
                    return SecurityUtils.sanitize(country, SecurityUtils.SanitizeType.COUNTRY);
                }
            }
        } catch (Exception e) {
            String msg = tmsg.countryLookupFailed
                    .replace("<ip>", safeIp)
                    .replace("<error>", SecurityUtils.sanitize(
                            e.getMessage(),
                            SecurityUtils.SanitizeType.ERROR_MESSAGE
                    ));
            plugin.getLogger().warning(msg);
        }
        return tmsg.unknownCountry;
    }

    /**
     * Shutdown the Telegram notifier and cleanup resources.
     * Should be called on plugin disable.
     */
    public void shutdown() {
        sendQueue.shutdown();
    }
}