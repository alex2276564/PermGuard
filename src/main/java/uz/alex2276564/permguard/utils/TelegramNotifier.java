package uz.alex2276564.permguard.utils;

import com.google.gson.JsonObject;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;

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
                try {
                    sendMessage(telegram, chatId.trim(), message);
                } catch (Exception e) {
                    plugin.getLogger().warning("Telegram send failed: " + e.getMessage());
                }
            }


        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send telegram notification: " + e.getMessage());
        }
    }

    private void sendMessage(MainConfig.TelegramSection config, String chatId, String message) {
        String urlString = String.format(TELEGRAM_API_URL,
                config.botToken,
                chatId,
                URLEncoder.encode(message, StandardCharsets.UTF_8));

        for (int attempt = 1; attempt <= config.maxRetries + 1; attempt++) {
            try {
                HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);

                if (response.responseCode() == HttpURLConnection.HTTP_OK) {
                    return;
                } else if (response.responseCode() == 429) {
                    if (attempt < config.maxRetries + 1) {
                        Thread.sleep(config.retryDelay);
                    } else {
                        throw new Exception("Too Many Requests (429) after max retries");
                    }
                } else {
                    throw new Exception("HTTP " + response.responseCode());
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                        String.format("Failed to send Telegram notification (attempt %d/%d): %s",
                                attempt, config.maxRetries + 1, e.getMessage())
                );

                if (attempt < config.maxRetries + 1) {
                    try {
                        Thread.sleep(config.retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    return; // Give up after max retries
                }
            }
        }
    }

    private String getCountryByIp(String ip) {
        try {
            String urlString = String.format(IP_API_URL, ip);
            HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);

            if (response.responseCode() == HttpURLConnection.HTTP_OK) {
                JsonObject json = response.jsonBody();
                return json.has("country") ? json.get("country").getAsString() : "Unknown";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get country for IP " + ip + ": " + e.getMessage());
        }
        return "Unknown";
    }
}