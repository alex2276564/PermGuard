package uz.alex2276564.permguard.config.configs.permissionsconfig;

import lombok.experimental.UtilityClass;
import uz.alex2276564.permguard.config.utils.validation.ValidationResult;
import uz.alex2276564.permguard.config.utils.validation.Validators;

@UtilityClass
public class PermissionsConfigValidator {

    public static void validate(PermissionsConfig config, String fileName) {
        ValidationResult result = new ValidationResult();

        validatePermissionsList(result, config.restrictedPermissions);

        result.throwIfInvalid("Permissions configuration (" + fileName + ")");
    }

    private static void validatePermissionsList(ValidationResult result, java.util.List<PermissionsConfig.PermissionEntry> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            result.addError("restrictedPermissions", "List cannot be empty");
            return;
        }

        for (int i = 0; i < permissions.size(); i++) {
            PermissionsConfig.PermissionEntry entry = permissions.get(i);
            String prefix = "restrictedPermissions[" + i + "]";

            validatePermissionEntry(result, entry, prefix);
        }
    }

    private static void validatePermissionEntry(ValidationResult result, PermissionsConfig.PermissionEntry entry, String prefix) {
        // Permission validation
        Validators.notBlank(result, prefix + ".permission", entry.permission, "Permission cannot be empty");

        // Command validation
        Validators.notBlank(result, prefix + ".cmd", entry.cmd, "Command cannot be empty");
        if (entry.cmd != null && !entry.cmd.contains("<player>")) {
            result.addError(prefix + ".cmd", "Command must contain <player> placeholder");
        }

        // Kick message validation
        Validators.notBlank(result, prefix + ".kickMessage", entry.kickMessage, "Kick message cannot be empty");
        if (entry.kickMessage != null && !entry.kickMessage.contains("<permission>")) {
            result.addError(prefix + ".kickMessage", "Kick message must contain <permission> placeholder");
        }

        // Additional validations
        if (entry.permission != null) {
            // Validate permission format (basic check)
            if (entry.permission.contains(" ")) {
                result.addError(prefix + ".permission", "Permission cannot contain spaces");
            }
        }
    }
}