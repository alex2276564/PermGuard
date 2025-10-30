package uz.alex2276564.permguard.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfigValidator;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfigValidator;
import uz.alex2276564.permguard.config.configs.permissionsconfig.CompiledPermissions;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfig;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfigValidator;
import uz.alex2276564.permguard.utils.ResourceUtils;

import java.io.File;
import java.util.*;

public class PermGuardConfigManager {
    private final PermGuard plugin;

    @Getter
    private MainConfig mainConfig;

    @Getter
    private MessagesConfig messagesConfig;

    @Getter
    private final List<PermissionsConfig> permissionConfigs = new ArrayList<>();

    // Atomic, immutable snapshot for join-time reads
    @Getter
    @SuppressWarnings("java:S3077")
    // Thread-safe: volatile ensures visibility + CompiledPermissions is immutable (record + List.copyOf)
    private volatile CompiledPermissions compiledPermissions = CompiledPermissions.empty();

    public PermGuardConfigManager(PermGuard plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        try {
            loadMainConfig();
            loadMessagesConfig();
            loadPermissionConfigs(); // loads files into permissionConfigs
            rebuildCompiledPermissions(); // builds atomic immutable cache

            plugin.getLogger().info("Configuration reloaded.");
            plugin.getLogger().info("Permission cache: wildcard=" + (compiledPermissions.wildcard() != null)
                    + ", regular=" + compiledPermissions.regular().size());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMainConfig() {
        mainConfig = ConfigManager.create(MainConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(plugin.getDataFolder(), "config.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MainConfigValidator.validate(mainConfig);
        plugin.getLogger().info("Main configuration loaded and validated successfully");
    }

    private void loadMessagesConfig() {
        messagesConfig = ConfigManager.create(MessagesConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(plugin.getDataFolder(), "messages.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MessagesConfigValidator.validate(messagesConfig);
        PermGuard.getInstance().getMessageManager().configureDisabledKeysProvider(() -> getMessagesConfig().disabledKeys);
        plugin.getLogger().info("Messages configuration loaded and validated successfully");
    }

    private void loadPermissionConfig(File file) {
        try {
            PermissionsConfig config = ConfigManager.create(PermissionsConfig.class, it -> {
                it.withConfigurer(new YamlSnakeYamlConfigurer());
                it.withBindFile(file);
                it.withRemoveOrphans(true);
                it.saveDefaults();
                it.load(true);
            });

            PermissionsConfigValidator.validate(config, file.getName());
            permissionConfigs.add(config);
            plugin.getLogger().info("Permission configuration loaded and validated successfully: " + file.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load permission config " + file.getName() + ": " + e.getMessage());
        }
    }

    private void loadPermissionConfigs() {
        permissionConfigs.clear();

        File permissionsDir = new File(plugin.getDataFolder(), "restrictedpermissions");
        if (!permissionsDir.exists()) permissionsDir.mkdirs();

        // Always update examples.txt from resources (to keep it up-to-date)
        File examplesFile = new File(permissionsDir, "examples.txt");
        ResourceUtils.updateFromResource(plugin, "restrictedpermissions/examples.txt", examplesFile);

        // Check if we need to create default permissions.yml
        File[] existingFiles = permissionsDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (existingFiles == null || existingFiles.length == 0) {
            // No yml files found - create default permissions.yml from resources
            File defaultPermFile = new File(permissionsDir, "permissions.yml");
            if (ResourceUtils.copyResourceIfNotExists(plugin, "restrictedpermissions/permissions.yml", defaultPermFile)) {
                plugin.getLogger().info("Created default permission configuration: permissions.yml");
            }
        }

        // Load all .yml files
        File[] files = permissionsDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null && files.length > 0) {
            for (File file : files) loadPermissionConfig(file);
        } else {
            plugin.getLogger().warning("No permission configuration files found!");
        }

        plugin.getLogger().info("Loaded " + permissionConfigs.size() + " permission configuration file(s).");
    }

    // Build immutable, deduplicated cache. First '*' wins, first occurrence of each permission wins.
    private void rebuildCompiledPermissions() {
        PermissionsConfig.PermissionEntry star = null;

        // Dedup by permission name (case-insensitive), preserve first occurrence order
        Map<String, PermissionsConfig.PermissionEntry> map = new LinkedHashMap<>();

        for (var cfg : permissionConfigs) {
            for (var e : cfg.restrictedPermissions) {
                var raw = e.permission;
                var p = (raw == null) ? null : raw.trim(); // normalize
                if (p == null || p.isBlank()) continue;

                if ("*".equals(p)) {
                    if (star == null) star = e;
                    else plugin.getLogger().warning("Multiple '*' entries detected. Using the first one.");
                    continue;
                }

                var key = p.toLowerCase(Locale.ROOT);
                map.putIfAbsent(key, e);
            }
        }

        var nonStar = List.copyOf(map.values()); // immutable snapshot
        compiledPermissions = new CompiledPermissions(star, nonStar);
    }
}