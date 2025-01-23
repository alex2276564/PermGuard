package uz.alex2276564.permguard.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import uz.alex2276564.permguard.PermGuard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public static void sendNotification(Player player, String permission) {
        if (!ConfigManager.isTelegramEnabled()) {
            return;
        }

        String botToken = ConfigManager.getTelegramBotToken();
        String[] chatIds = ConfigManager.getTelegramChatIds();
        String messageTemplate = ConfigManager.getTelegramMessage();
        String ip = player.getAddress().getAddress().getHostAddress();
        String country = getCountryByIp(ip);

        String message = messageTemplate
                .replace("%player%", player.getName())
                .replace("%permission%", permission)
                .replace("%ip%", ip)
                .replace("%country%", country)
                .replace("%date%", DATE_FORMAT.format(new Date()));

        CompletableFuture<?>[] futures = Arrays.stream(chatIds)
                .map(chatId -> CompletableFuture.runAsync(() ->
                        sendMessage(botToken, chatId.trim(), message)))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private static void sendMessage(String botToken, String chatId, String message) {
        int maxRetries = ConfigManager.getTelegramMaxRetries();
        long retryDelay = ConfigManager.getTelegramRetryDelay();

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            HttpURLConnection conn = null;
            try {
                String urlString = String.format(TELEGRAM_API_URL,
                        botToken,
                        chatId,
                        URLEncoder.encode(message, StandardCharsets.UTF_8));

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return; // Successful send
                }

                if (responseCode == 429 && attempt < maxRetries) { // Too Many Requests
                    Thread.sleep(retryDelay);
                }
            } catch (Exception e) {
                PermGuard.getInstance().getLogger().warning(
                        String.format("Failed to send Telegram notification (attempt %d/%d): %s",
                                attempt + 1, maxRetries, e.getMessage())
                );

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    private static String getCountryByIp(String ip) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(String.format(IP_API_URL, ip));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();

                if (response.has("country")) {
                    return response.get("country").getAsString();
                }
            }
        } catch (Exception e) {
            PermGuard.getInstance().getLogger().warning(
                    String.format("Failed to get country for IP %s: %s", ip, e.getMessage())
            );
            return "Unknown";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return "Unknown";
    }
}
