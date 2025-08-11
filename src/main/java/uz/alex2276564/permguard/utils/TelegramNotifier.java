package uz.alex2276564.permguard.utils;

import com.google.gson.JsonObject;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramNotifier {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    @SuppressWarnings("HttpUrlsUsage")
    private static final String IP_API_URL = "http://ip-api.com/json/%s";

    private final PermGuard plugin;
    private final HttpUtils httpUtils;

    public TelegramNotifier(PermGuard plugin) {
        this.plugin = plugin;
        this.httpUtils = new HttpUtils();
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
        String urlString = String.format(TELEGRAM_API_URL,
                config.botToken,
                chatId,
                URLEncoder.encode(message, StandardCharsets.UTF_8));

        int maxAttempts = config.maxRetries + 1;
        MessagesConfig.TelegramMessagesSection tmsg =
                plugin.getConfigManager().getMessagesConfig().telegramMessages;

        try {
            HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);

            if (response.responseCode() == HttpURLConnection.HTTP_OK) {
                return;
            }

            long nextDelayMs = config.retryDelay;

            if (response.responseCode() == 429) {
                // Try to read retry_after from Telegram API response for better rate limiting
                try {
                    JsonObject body = response.jsonBody();
                    if (body != null && body.has("parameters")) {
                        JsonObject params = body.getAsJsonObject("parameters");
                        if (params.has("retry_after")) {
                            long retryAfter = params.get("retry_after").getAsLong() * 1000L;
                            nextDelayMs = Math.max(nextDelayMs, retryAfter);
                        }
                    }
                } catch (Exception ignored) {
                    // Ignore parsing errors, use default delay
                }

                if (attempt < maxAttempts) {
                    logRetryAttempt(tmsg, attempt, maxAttempts, "Rate limit (429)");
                    scheduleRetry(config, chatId, message, attempt, nextDelayMs);
                } else {
                    // Use message from config instead of hardcode
                    plugin.getLogger().warning(tmsg.tooManyRequests);
                }
            } else {
                throw new Exception("HTTP " + response.responseCode());
            }
        } catch (Exception e) {
            if (attempt < maxAttempts) {
                logRetryAttempt(tmsg, attempt, maxAttempts, e.getMessage());
                scheduleRetry(config, chatId, message, attempt, config.retryDelay);
            } else {
                // Use sendFailed message from config
                String msg = tmsg.sendFailed.replace("<error>", e.getMessage());
                plugin.getLogger().warning(msg);
            }
        }
    }

    private void scheduleRetry(MainConfig.TelegramSection config, String chatId,
                               String message, int attempt, long delayMs) {
        // Convert milliseconds to ticks (20 ticks = 1 second), round up with minimum 1 tick
        // Since retryDelay is minimum 1000ms, this will be at least 20 ticks
        long delayTicks = Math.max(1L, (delayMs + 49L) / 50L);

        plugin.getRunner().runDelayedAsync(() -> sendMessageAttempt(config, chatId, message, attempt + 1), delayTicks);
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
            String urlString = String.format(IP_API_URL, ip);
            HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);

            if (response.responseCode() == HttpURLConnection.HTTP_OK) {
                JsonObject json = response.jsonBody();
                return json.has("country") ? json.get("country").getAsString() : tmsg.unknownCountry;
            }
        } catch (Exception e) {
            String msg = tmsg.countryLookupFailed
                    .replace("<ip>", ip)
                    .replace("<error>", e.getMessage());
            plugin.getLogger().warning(msg);
        }
        return tmsg.unknownCountry;
    }
}