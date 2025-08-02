package uz.alex2276564.permguard.config.configs.mainconfig;

import lombok.experimental.UtilityClass;
import uz.alex2276564.permguard.config.utils.validation.ValidationResult;
import uz.alex2276564.permguard.config.utils.validation.Validators;

@UtilityClass
public class MainConfigValidator {

    public static void validate(MainConfig config) {
        ValidationResult result = new ValidationResult();

        validateTelegramSection(result, config.telegram);

        result.throwIfInvalid("Main configuration");
    }

    private static void validateTelegramSection(ValidationResult result, MainConfig.TelegramSection telegram) {

        if (telegram.isConfigured()) {
            // Bot token validation
            Validators.notBlank(result, "telegram.botToken", telegram.botToken, "Bot token cannot be empty");
            Validators.pattern(result, "telegram.botToken", telegram.botToken,
                    "^\\d+:[A-Za-z0-9_-]{35}$", "Invalid Telegram bot token format");

            // Chat IDs validation
            Validators.notBlank(result, "telegram.chatIds", telegram.chatIds, "Chat IDs cannot be empty");
            Validators.pattern(result, "telegram.chatIds", telegram.chatIds,
                    "^\\s*-?\\d+(\\s*,\\s*-?\\d+)*\\s*$", "Chat IDs must be comma-separated numbers (e.g., 123456789,-987654321)");

            // Max retries validation
            Validators.min(result, "telegram.maxRetries", telegram.maxRetries, 0, "Max retries cannot be negative");
            Validators.max(result, "telegram.maxRetries", telegram.maxRetries, 10, "Max retries cannot exceed 10");

            // Retry delay validation
            Validators.min(result, "telegram.retryDelay", telegram.retryDelay, 1000L, "Retry delay must be at least 1000ms");

            // Message template validation
            Validators.notBlank(result, "telegram.message", telegram.message, "Message template cannot be empty");

            // Additional message validation - check for required placeholders
            if (telegram.message != null && !telegram.message.contains("%player%")) {
                result.addError("telegram.message", "Message template must contain %player% placeholder");
            }
            if (telegram.message != null && !telegram.message.contains("%permission%")) {
                result.addError("telegram.message", "Message template must contain %permission% placeholder");
            }
        }
    }
}