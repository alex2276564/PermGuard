package uz.alex2276564.permguard.config.configs.permissionsconfig;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CompiledPermissions(
        @Nullable PermissionsConfig.PermissionEntry wildcard,
        List<PermissionsConfig.PermissionEntry> regular // immutable
) {
    public static CompiledPermissions empty() {
        return new CompiledPermissions(null, List.of());
    }
}