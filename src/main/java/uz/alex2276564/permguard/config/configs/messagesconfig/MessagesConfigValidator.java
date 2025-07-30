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
                ".*\\{type\\}.*", "Reload success message must contain {type} placeholder");
        Validators.pattern(result, "commands.reload.error", commands.reload.error,
                ".*\\{error\\}.*", "Reload error message must contain {error} placeholder");
    }

    private static void validateGeneralSection(ValidationResult result, MessagesConfig.GeneralSection general) {
        Validators.notBlank(result, "general.wildcardPermissionConflict", general.wildcardPermissionConflict, "Wildcard permission conflict message cannot be empty");
    }
}