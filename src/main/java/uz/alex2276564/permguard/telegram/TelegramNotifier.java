package uz.alex2276564.permguard.telegram;

import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.utils.IpCountryResolver;
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

    private final PermGuard plugin;
    private final TelegramSendQueue sendQueue;

    public TelegramNotifier(PermGuard plugin) {
        this.plugin = plugin;

        String userAgent = plugin.getDescription().getName()
                + "/" + plugin.getDescription().getVersion();

        long defaultDelayMs = plugin.getConfigManager()
                .getMainConfig()
                .telegram
                .minDelayMs;

        this.sendQueue = new TelegramSendQueue(plugin, plugin.getHttpUtils(), userAgent, defaultDelayMs);
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
            String country = IpCountryResolver.resolveCountry(safeIp, plugin);

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
     * Shutdown the Telegram notifier and cleanup resources.
     * Should be called on plugin disable.
     */
    public void shutdown() {
        sendQueue.shutdown();
    }
}