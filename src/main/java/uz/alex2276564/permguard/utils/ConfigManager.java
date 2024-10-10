package uz.alex2276564.permguard.utils;

import org.bukkit.configuration.file.FileConfiguration;
import uz.alex2276564.permguard.PermGuard;

import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static FileConfiguration config;
    private static List<Map<String, Object>> restrictedPermission;

    public static void reload() {
        PermGuard.getInstance().reloadConfig();
        config = PermGuard.getInstance().getConfig();
        loadRestricedPermissions();
    }

    @SuppressWarnings("unchecked")
    public static void loadRestricedPermissions() {
        restrictedPermission = (List<Map<String, Object>>) (Object) config.getMapList("restrictedPermissions");
    }


    public static List<Map<String, Object>> getRestrictedPermissions() {
        return restrictedPermission;
    }
}
