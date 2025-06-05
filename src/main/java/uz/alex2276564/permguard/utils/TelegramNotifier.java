package uz.alex2276564.permguard.utils;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import uz.alex2276564.permguard.PermGuard;

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
        if (!plugin.getConfigManager().isTelegramEnabled()) {
            return;
        }

        String botToken = plugin.getConfigManager().getTelegramBotToken();
        String[] chatIds = plugin.getConfigManager().getTelegramChatIds();
        String messageTemplate = plugin.getConfigManager().getTelegramMessage();
        String ip = player.getAddress().getAddress().getHostAddress();
        String country = messageTemplate.contains("%country%") ? getCountryByIp(ip) : null;

        String message = messageTemplate
                .replace("%player%", player.getName())
                .replace("%permission%", permission)
                .replace("%ip%", ip)
                .replace("%date%", DATE_FORMAT.format(new Date()));

        if (country != null) {
            message = message.replace("%country%", country);
        }

        String finalMessage = message;
        CompletableFuture<?>[] futures = Arrays.stream(chatIds)
                .map(chatId -> CompletableFuture.runAsync(() ->
                        sendMessage(botToken, chatId.trim(), finalMessage)))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private void sendMessage(String botToken, String chatId, String message) {
        int maxRetries = plugin.getConfigManager().getTelegramMaxRetries();
        long retryDelay = plugin.getConfigManager().getTelegramRetryDelay();

        String urlString = String.format(TELEGRAM_API_URL,
                botToken,
                chatId,
                URLEncoder.encode(message, StandardCharsets.UTF_8));

        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);

                if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return; // Successful send
                } else if (response.getResponseCode() == 429) { // Too Many Requests
                    if (attempt < maxRetries + 1) {
                        Thread.sleep(retryDelay);
                    } else {
                        throw new Exception("Failed to send Telegram message: Too Many Requests (429) after max retries.");
                    }
                } else {
                    throw new Exception("Failed to send Telegram message: HTTP " + response.getResponseCode());
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                        String.format("Failed to send Telegram notification (attempt %d/%d): %s",
                                attempt, maxRetries + 1, e.getMessage())
                );

                if (attempt < maxRetries + 1) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private String getCountryByIp(String ip) {
        String urlString = String.format(IP_API_URL, ip);
        try {
            HttpUtils.HttpResponse response = httpUtils.getResponse(urlString, null);
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                JsonObject json = response.getJsonBody();
                return json.has("country") ? json.get("country").getAsString() : "Unknown";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get country for IP " + ip + ": " + e.getMessage());
        }
        return "Unknown";
    }
}
