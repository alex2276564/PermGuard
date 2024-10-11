package uz.alex2276564.permguard.utils;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import uz.alex2276564.permguard.PermGuard;

import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static FileConfiguration config;
    @Getter
    private static List<Map<String, Object>> restrictedPermissions;

    public static void reload() {
        Plugin plugin = PermGuard.getInstance();

        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }

    @SuppressWarnings("unchecked")
    public static void loadConfig() {
        restrictedPermissions = (List<Map<String, Object>>) (Object) config.getMapList("restrictedPermissions");
    }

    private ConfigManager() {
        throw new IllegalStateException("Utility class");
    }

}
