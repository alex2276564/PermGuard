package uz.alex2276564.permguard.config;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfig;
import uz.alex2276564.permguard.config.configs.mainconfig.MainConfigValidator;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfigValidator;
import uz.alex2276564.permguard.config.configs.permissionsconfig.CompiledPermissions;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfig;
import uz.alex2276564.permguard.config.configs.permissionsconfig.PermissionsConfigValidator;
import uz.alex2276564.permguard.utils.ResourceUtils;
import uz.alex2276564.permguard.utils.adventure.MessageManager;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PermGuardConfigManager {

    private final File dataFolder;
    private final Logger logger;
    private final MessageManager messageManager;
    private final ClassLoader resourceLoader;

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

    public PermGuardConfigManager(File dataFolder,
                                  Logger logger,
                                  MessageManager messageManager,
                                  ClassLoader resourceLoader) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.messageManager = messageManager;
        this.resourceLoader = resourceLoader;
    }

    public void reload() {
        try {
            loadMainConfig();
            loadMessagesConfig();
            loadPermissionConfigs(); // loads files into permissionConfigs
            rebuildCompiledPermissions(); // builds atomic immutable cache

            logger.info("Configuration reloaded.");
            logger.info("Permission cache: wildcard=" + (compiledPermissions.wildcard() != null)
                    + ", regular=" + compiledPermissions.regular().size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    private void loadMainConfig() {
        mainConfig = ConfigManager.create(MainConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(dataFolder, "config.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MainConfigValidator.validate(mainConfig);
        logger.info("Main configuration loaded and validated successfully");
    }

    private void loadMessagesConfig() {
        messagesConfig = ConfigManager.create(MessagesConfig.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer());
            it.withBindFile(new File(dataFolder, "messages.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });

        MessagesConfigValidator.validate(messagesConfig);
        messageManager.configureDisabledKeysProvider(() -> getMessagesConfig().disabledKeys);
        logger.info("Messages configuration loaded and validated successfully");
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
            logger.info("Permission configuration loaded and validated successfully: " + file.getName());
        } catch (Exception e) {
            logger.warning("Failed to load permission config " + file.getName() + ": " + e.getMessage());
        }
    }

    private void loadPermissionConfigs() {
        permissionConfigs.clear();

        File permissionsDir = new File(dataFolder, "restrictedpermissions");
        if (!permissionsDir.exists()) permissionsDir.mkdirs();

        // Always update examples.txt from resources (to keep it up-to-date)
        File examplesFile = new File(permissionsDir, "examples.txt");
        ResourceUtils.updateFromResource(resourceLoader, logger, "restrictedpermissions/examples.txt", examplesFile);

        // Check if we need to create default permissions.yml
        File[] existingFiles = permissionsDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (existingFiles == null || existingFiles.length == 0) {
            // No yml files found - create default permissions.yml from resources
            File defaultPermFile = new File(permissionsDir, "permissions.yml");
            if (ResourceUtils.copyResourceIfNotExists(resourceLoader, logger,
                    "restrictedpermissions/permissions.yml", defaultPermFile)) {
                logger.info("Created default permission configuration: permissions.yml");
            }
        }

        // Load all .yml files
        File[] files = permissionsDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null && files.length > 0) {
            for (File file : files) loadPermissionConfig(file);
        } else {
            logger.warning("No permission configuration files found!");
        }

        logger.info("Loaded " + permissionConfigs.size() + " permission configuration file(s).");
    }

    // Build immutable, deduplicated cache. First '*' wins, first occurrence of each permission wins.
    private void rebuildCompiledPermissions() {
        PermissionsConfig.PermissionEntry star = null;

        // Dedup by permission name (case-insensitive), preserve first occurrence order
        Map<String, PermissionsConfig.PermissionEntry> map = new LinkedHashMap<>();

        for (var cfg : permissionConfigs) {
            for (var e : cfg.restrictedPermissions) {
                String p = e.permission.trim();

                if ("*".equals(p)) {
                    if (star == null) {
                        star = e;
                    } else {
                        logger.warning("Multiple '*' entries detected in restricted permissions. Using the first one.");
                    }
                    continue;
                }

                String key = p.toLowerCase(Locale.ROOT);
                map.putIfAbsent(key, e);
            }
        }

        var nonStar = List.copyOf(map.values()); // immutable snapshot
        compiledPermissions = new CompiledPermissions(star, nonStar);
    }
}