package uz.alex2276564.permguard.config.data;

import uz.alex2276564.permguard.config.defaults.ConfigDefaults;

public record TelegramConfig(
        boolean enabled,
        String botToken,
        String[] chatIds,
        int maxRetries,
        long retryDelay,
        String message
) {
    public boolean isConfigured() {
        return enabled &&
                !ConfigDefaults.BOT_TOKEN.equals(botToken) &&
                !botToken.isEmpty() &&
                chatIds.length > 0 &&
                !String.join(",", chatIds).equals(ConfigDefaults.CHAT_IDS);
    }
}
