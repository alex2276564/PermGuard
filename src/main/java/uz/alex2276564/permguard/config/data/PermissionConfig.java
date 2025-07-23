package uz.alex2276564.permguard.config.data;

import java.util.List;

public record PermissionConfig(List<PermissionEntry> restrictedPermissions) {

    public record PermissionEntry(String permission, String command, boolean log, String kickMessage) {}

    public boolean hasWildcardPermission() {
        return restrictedPermissions.stream().anyMatch(entry -> "*".equals(entry.permission()));
    }
}
