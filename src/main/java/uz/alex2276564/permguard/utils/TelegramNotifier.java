package uz.alex2276564.permguard.utils;

import com.alibaba.fastjson2.JSONObject;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.utils.runner.Runner;

public class TelegramNotifier {
    private static final String TELEGRAM_SEND_MESSAGE_URL = "https://api.telegram.org/bot%s/sendMessage";
    // SECURITY NOTE: HTTP is used because ip-api.com requires a paid tier for HTTPS.
    // MITM risks are mitigated by strict input/output verification via SecurityUtils.
    @SuppressWarnings("HttpUrlsUsage")
    private static final String IP_API_URL = "http://ip-api.com/json/%s";

    private final PermGuard plugin;
    private final HttpUtils httpUtils;

    public TelegramNotifier(PermGuard plugin, HttpUtils httpUtils) {
        this.plugin = plugin;
        this.httpUtils = httpUtils;
    }

    public void sendNotification(String name, String permission, String ip, String date) {
        MainConfig.TelegramSection telegram = plugin.getConfigManager().getMainConfig().telegram;

        if (!telegram.enabled || !telegram.isConfigured()) {
            return;
        }

        try {
            String country = getCountryByIp(ip);

            String message = StringUtils.processEscapeSequences(telegram.message)
                    .replace("%player%", name)
                    .replace("%permission%", permission)
                    .replace("%ip%", ip)
                    .replace("%country%", country)
                    .replace("%date%", date);

            for (String chatId : telegram.getChatIdsArray()) {
                sendMessage(telegram, chatId.trim(), message);
            }

        } catch (Exception e) {
            String msg = plugin.getConfigManager().getMessagesConfig()
                    .telegramMessages.notificationFailed
                    .replace("<error>", e.getMessage());
            plugin.getLogger().warning(msg);
        }
    }

    private void sendMessage(MainConfig.TelegramSection config, String chatId, String message) {
        sendMessageAttempt(config, chatId, message, 1);
    }

    private void sendMessageAttempt(MainConfig.TelegramSection config, String chatId,
                                    String message, int attempt) {
        String urlString = String.format(TELEGRAM_SEND_MESSAGE_URL, config.botToken);
        JSONObject body = new JSONObject();
        body.put("chat_id", chatId);
        body.put("text", message);

        int maxAttempts = config.maxRetries + 1;
        MessagesConfig.TelegramMessagesSection tmsg =
                plugin.getConfigManager().getMessagesConfig().telegramMessages;

        try {
            HttpUtils.HttpResponse response = httpUtils.postJson(
                    urlString,
                    body,
                    "PermGuard/" + plugin.getDescription().getVersion()
            );

            int status = response.statusCode();
            if (status == 200) {
                return;
            }

            long nextDelayMs = config.retryDelay;

            if (status == 429) {
                // Try to read retry_after from Telegram API response for better rate limiting
                try {
                    JSONObject json = response.jsonBody();
                    JSONObject params = json.getJSONObject("parameters");
                    if (params != null) {
                        Long retryAfter = params.getLong("retry_after");
                        if (retryAfter != null) {
                            long retryAfterMs = retryAfter * 1000L;
                            nextDelayMs = Math.max(nextDelayMs, retryAfterMs);
                        }
                    }
                } catch (Exception ignored) {
                    // Ignore parsing errors, use default delay
                }

                if (attempt < maxAttempts) {
                    logRetryAttempt(tmsg, attempt, maxAttempts, "Rate limit (429)");
                    scheduleRetry(config, chatId, message, attempt, nextDelayMs);
                } else {
                    plugin.getLogger().warning(tmsg.tooManyRequests);
                }
            } else {
                throw new Exception("HTTP " + status);
            }
        } catch (Exception e) {
            if (attempt < maxAttempts) {
                logRetryAttempt(tmsg, attempt, maxAttempts, e.getMessage());
                scheduleRetry(config, chatId, message, attempt, config.retryDelay);
            } else {
                String msg = tmsg.sendFailed.replace("<error>", e.getMessage());
                plugin.getLogger().warning(msg);
            }
        }
    }

    private void scheduleRetry(MainConfig.TelegramSection config, String chatId,
                               String message, int attempt, long delayMs) {
        // Convert milliseconds to ticks (20 ticks = 1 second), round up with minimum 1 tick
        // Since retryDelay is minimum 1000ms, this will be at least 20 ticks
        long delayTicks = Runner.msToTicks(delayMs);

        plugin.getRunner().runAsyncLater(() ->
                sendMessageAttempt(config, chatId, message, attempt + 1), delayTicks);
    }

    private void logRetryAttempt(MessagesConfig.TelegramMessagesSection tmsg,
                                 int attempt, int maxAttempts, String error) {
        String msg = tmsg.sendFailedAttempt
                .replace("<attempt>", String.valueOf(attempt))
                .replace("<max>", String.valueOf(maxAttempts))
                .replace("<error>", error);
        plugin.getLogger().warning(msg);
    }

    private String getCountryByIp(String ip) {
        MessagesConfig.TelegramMessagesSection tmsg =
                plugin.getConfigManager().getMessagesConfig().telegramMessages;

        try {
            String cleanIp = SecurityUtils.sanitize(ip, SecurityUtils.SanitizeType.IP_ADDRESS);
            String urlString = String.format(IP_API_URL, cleanIp);

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
                    .replace("<ip>", SecurityUtils.safeLog(ip))
                    .replace("<error>", SecurityUtils.sanitize(
                            e.getMessage(),
                            SecurityUtils.SanitizeType.ERROR_MESSAGE
                    ));
            plugin.getLogger().warning(msg);
        }
        return tmsg.unknownCountry;
    }
}