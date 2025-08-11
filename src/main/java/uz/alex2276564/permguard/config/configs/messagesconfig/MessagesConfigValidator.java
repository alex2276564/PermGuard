package uz.alex2276564.permguard.config.configs.messagesconfig;

import lombok.experimental.UtilityClass;
import uz.alex2276564.permguard.config.utils.validation.ValidationResult;
import uz.alex2276564.permguard.config.utils.validation.Validators;

@UtilityClass
public class MessagesConfigValidator {

    public static void validate(MessagesConfig config) {
        ValidationResult result = new ValidationResult();

        validateCommandsSection(result, config.commands);
        validateGeneralSection(result, config.general);
        validateLoggingSection(result, config.logging);
        validateTelegramSection(result, config.telegramMessages);

        result.throwIfInvalid("Messages configuration");
    }

    private static void validateCommandsSection(ValidationResult result, MessagesConfig.CommandsSection commands) {
        // Help section validation
        Validators.notBlank(result, "commands.help.header", commands.help.header, "Help header cannot be empty");
        Validators.notBlank(result, "commands.help.reloadLine", commands.help.reloadLine, "Help reload line cannot be empty");
        Validators.notBlank(result, "commands.help.helpLine", commands.help.helpLine, "Help help line cannot be empty");

        // Reload section validation
        Validators.notBlank(result, "commands.reload.success", commands.reload.success, "Reload success message cannot be empty");
        Validators.notBlank(result, "commands.reload.error", commands.reload.error, "Reload error message cannot be empty");

        // Check for required placeholders
        Validators.pattern(result, "commands.reload.success", commands.reload.success,
                ".*\\<type\\>.*", "Reload success message must contain <type> placeholder");
        Validators.pattern(result, "commands.reload.error", commands.reload.error,
                ".*\\<error\\>.*", "Reload error message must contain <error> placeholder");
    }

    private static void validateGeneralSection(ValidationResult result, MessagesConfig.GeneralSection general) {
        Validators.notBlank(result, "general.wildcardPermissionConflict", general.wildcardPermissionConflict, "Wildcard permission conflict message cannot be empty");
    }

    private static void validateLoggingSection(ValidationResult result, MessagesConfig.LoggingSection logging) {
        Validators.notBlank(result, "logging.violationEntry", logging.violationEntry, "Violation entry template cannot be empty");
        Validators.pattern(result, "logging.violationEntry", logging.violationEntry,
                ".*\\<date\\>.*\\<player\\>.*\\<permission\\>.*\\<ip\\>.*",
                "Violation entry must contain <date>, <player>, <permission>, and <ip> placeholders");

        Validators.notBlank(result, "logging.fileWriteError", logging.fileWriteError, "File write error message cannot be empty");
        Validators.pattern(result, "logging.fileWriteError", logging.fileWriteError,
                ".*\\<error\\>.*", "File write error message must contain <error> placeholder");

        Validators.notBlank(result, "logging.dangerousCharBlocked", logging.dangerousCharBlocked, "Dangerous char message cannot be empty");
        Validators.pattern(result, "logging.dangerousCharBlocked", logging.dangerousCharBlocked,
                ".*\\<char\\>.*\\<input\\>.*", "Dangerous char message must contain <char> and <input> placeholders");
    }

    private static void validateTelegramSection(ValidationResult result, MessagesConfig.TelegramMessagesSection t) {
        Validators.notBlank(result, "telegramMessages.sendFailed", t.sendFailed, "sendFailed cannot be empty");
        Validators.pattern(result, "telegramMessages.sendFailed", t.sendFailed,
                ".*\\<error\\>.*", "sendFailed must contain <error>");

        Validators.notBlank(result, "telegramMessages.notificationFailed", t.notificationFailed, "notificationFailed cannot be empty");
        Validators.pattern(result, "telegramMessages.notificationFailed", t.notificationFailed,
                ".*\\<error\\>.*", "notificationFailed must contain <error>");

        Validators.notBlank(result, "telegramMessages.sendFailedAttempt", t.sendFailedAttempt, "sendFailedAttempt cannot be empty");
        Validators.pattern(result, "telegramMessages.sendFailedAttempt", t.sendFailedAttempt,
                ".*\\<attempt\\>.*\\<max\\>.*\\<error\\>.*", "sendFailedAttempt must contain <attempt>, <max>, <error>");

        Validators.notBlank(result, "telegramMessages.tooManyRequests", t.tooManyRequests, "tooManyRequests cannot be empty");

        Validators.notBlank(result, "telegramMessages.countryLookupFailed", t.countryLookupFailed, "countryLookupFailed cannot be empty");
        Validators.pattern(result, "telegramMessages.countryLookupFailed", t.countryLookupFailed,
                ".*\\<ip\\>.*\\<error\\>.*", "countryLookupFailed must contain <ip> and <error>");

        Validators.notBlank(result, "telegramMessages.unknownCountry", t.unknownCountry, "unknownCountry cannot be empty");
    }
}