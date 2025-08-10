package uz.alex2276564.permguard.config.configs.permissionsconfig;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;

import java.util.Arrays;
import java.util.List;

public class PermissionsConfig extends OkaeriConfig {

    @Comment("List of restricted permissions")
    public List<PermissionEntry> restrictedPermissions = Arrays.asList(
            new PermissionEntry(
                    "*",
                    "lp user <player> permission unset <permission>",
                    true,
                    "<red><bold>⚠ SECURITY ALERT ⚠</bold></red><newline><newline><yellow>Your <red><permission></red> permissions have been revoked.</yellow><newline><gray>Please rejoin and restore them via console.</gray>"
            )
    );

    public static class PermissionEntry extends OkaeriConfig {
        public String permission;
        public String cmd;
        public boolean log;
        public String kickMessage;

        public PermissionEntry() {}

        public PermissionEntry(String permission, String cmd, boolean log, String kickMessage) {
            this.permission = permission;
            this.cmd = cmd;
            this.log = log;
            this.kickMessage = kickMessage;
        }
    }
}