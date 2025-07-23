package uz.alex2276564.permguard.utils;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.data.TelegramConfig;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class TelegramNotifier {
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    private static final String IP_API_URL = "http://ip-api.com/json/%s";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final PermGuard plugin;
    private final HttpUtils httpUtils;

    public TelegramNotifier(PermGuard plugin) {
        this.plugin = plugin;
        this.httpUtils = new HttpUtils();
    }

    public void sendNotification(Player player, String permission) {
        TelegramConfig telegram = plugin.getConfigManager().telegram();

        if (!telegram.enabled() || !telegram.isConfigured()) {
            return;
        }

        try {
            String ip = player.getAddress().getAddress().getHostAddress();
            String country = getCountryByIp(ip);

            String message = telegram.message()
                    .replace("%player%", player.getName())
                    .replace("%permission%", permission)
                    .replace("%ip%", ip)
                    .replace("%country%", country)
                    .replace("%date%", DATE_FORMAT.format(new Date()));

            String finalMessage = message;
            CompletableFuture<?>[] futures = Arrays.stream(telegram.chatIds())
                    .map(chatId -> CompletableFuture.runAsync(() ->
                            sendMessage(telegram, chatId.trim(), finalMessage)))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send telegram notification: " + e.getMessage());
        }
    }

    private void sendMessage(TelegramConfig config, String chatId, String message) {
        String urlString = String.format(TELEGRAM_API_URL,
                config.botToken(),
                chatId,
                URLEncoder.encode(message, StandardCharsets.UTF_8));

        for (int attempt = 1; attempt <= config.maxRetries() + 1; attempt++) {
            try {
                HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);

                if (response.responseCode() == HttpURLConnection.HTTP_OK) {
                    return;
                } else if (response.responseCode() == 429) {
                    if (attempt < config.maxRetries() + 1) {
                        Thread.sleep(config.retryDelay());
                    } else {
                        throw new Exception("Too Many Requests (429) after max retries");
                    }
                } else {
                    throw new Exception("HTTP " + response.responseCode());
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                        String.format("Failed to send Telegram notification (attempt %d/%d): %s",
                                attempt, config.maxRetries() + 1, e.getMessage())
                );

                if (attempt < config.maxRetries() + 1) {
                    try {
                        Thread.sleep(config.retryDelay());
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
