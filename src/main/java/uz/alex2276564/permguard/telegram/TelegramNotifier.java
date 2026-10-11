package uz.alex2276564.permguard.telegram;

import uz.alex2276564.permguard.config.PermGuardConfigManager;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.utils.HttpUtils;
import uz.alex2276564.permguard.utils.IpCountryResolver;
import uz.alex2276564.permguard.utils.SecurityUtils;
import uz.alex2276564.permguard.utils.StringUtils;
import uz.alex2276564.permguard.utils.runner.Runner;

import java.util.logging.Logger;

/**
 * High-level service responsible for:
 * - building Telegram notification messages,
 * - resolving country by IP (only when required by template),
 * - enqueuing send jobs into TelegramSendQueue.
 * <p>
 * All actual network I/O to Telegram Bot API is delegated to TelegramSendQueue,
 * which applies global rate limiting and retry logic.
 */
public class TelegramNotifier {

    private final PermGuardConfigManager configManager;
    private final TelegramSendQueue sendQueue;
    private final HttpUtils httpUtils;
    private final Logger logger;

    public TelegramNotifier(PermGuardConfigManager configManager,
                            HttpUtils httpUtils,
                            Runner runner,
                            String pluginName,
                            String pluginVersion,
                            Logger logger) {
        this.configManager = configManager;
        this.httpUtils = httpUtils;
        this.logger = logger;

        String userAgent = pluginName + "/" + pluginVersion;
        long defaultDelayMs = configManager.getMainConfig().telegram.minDelayMs;

        this.sendQueue = new TelegramSendQueue(
                runner,
                httpUtils,
                userAgent,
                defaultDelayMs,
                logger
        );
    }

    /**
     * Public API used from PlayerJoinListener.
     * <p>
     * This method is expected to be called from an async context (PlayerJoinListener
     * already wraps it in runner.runAsync), because IP geolocation and telegram is a
     * blocking HTTP requests when used.
     */
    public void sendNotification(String safeName, String permission, String safeIp, String telegramDate) {
        MainConfig.TelegramSection telegram = configManager.getMainConfig().telegram;

        if (!telegram.enabled || !telegram.isConfigured()) {
            return;
        }

        MessagesConfig.TelegramMessagesSection tmsg =
                configManager.getMessagesConfig().telegramMessages;

        try {
            String template = StringUtils.processEscapeSequences(telegram.message);

            String country = tmsg.unknownCountry;
            if (template.contains("%country%")) {
                country = IpCountryResolver.resolveCountry(
                        safeIp,
                        telegram.ipGeolocationEndpoint,
                        httpUtils,
                        tmsg,
                        logger
                );
            }

            String message = template
                    .replace("%player%", safeName)
                    .replace("%permission%", permission)
                    .replace("%ip%", safeIp)
                    .replace("%country%", country)
                    .replace("%date%", telegramDate);

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
                    .replace("<error>", SecurityUtils.sanitize(e.getMessage(), SecurityUtils.SanitizeType.ERROR_MESSAGE));
            logger.warning(msg);
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