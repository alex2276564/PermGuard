package uz.alex2276564.permguard.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.config.data.*;
import uz.alex2276564.permguard.config.defaults.ConfigDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private final PermGuard plugin;
    private FileConfiguration config;
    private static final String CONFIG_EXAMPLE_URL = "https://github.com/alex2276564/PermGuard/blob/main/src/main/resources/config.yml";

    @Getter
    private ConfigData configData;

    public ConfigManager(PermGuard plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        boolean debugEnabled = config.getBoolean("settings.config-debug", ConfigDefaults.CONFIG_DEBUG);

        // Check configuration for missing sections and values
        checkConfigurationIssues(debugEnabled);

        configData = loadConfig(debugEnabled);
        plugin.getLogger().info("Configuration loaded successfully!");
    }

    /**
     * Check configuration for missing sections and values
     */
    private void checkConfigurationIssues(boolean debug) {
        if (debug) plugin.getLogger().info("[CONFIG-DEBUG] Checking configuration for missing sections...");

        List<String> issues = new ArrayList<>();

        // Get actual top-level keys from config file
        Set<String> topLevelKeys = config.getKeys(false);

        // Check settings section
        if (!topLevelKeys.contains("settings")) {
            issues.add("Missing 'settings' section");
        } else {
            ConfigurationSection settingsSection = config.getConfigurationSection("settings");
            if (settingsSection != null) {
                if (!settingsSection.isSet("shutdown-on-disable")) {
                    issues.add("Missing 'settings.shutdown-on-disable'");
                }
                if (!settingsSection.isSet("config-debug")) {
                    issues.add("Missing 'settings.config-debug'");
                }
            }
        }

        // Check telegram section
        if (!topLevelKeys.contains("telegram")) {
            issues.add("Missing 'telegram' section");
        } else {
            ConfigurationSection telegramSection = config.getConfigurationSection("telegram");
            if (telegramSection != null) {
                if (!telegramSection.isSet("enabled")) {
                    issues.add("Missing 'telegram.enabled'");
                }
                if (!telegramSection.isSet("bot-token")) {
                    issues.add("Missing 'telegram.bot-token'");
                }
                if (!telegramSection.isSet("chat-ids")) {
                    issues.add("Missing 'telegram.chat-ids'");
                }
                if (!telegramSection.isSet("max-retries")) {
                    issues.add("Missing 'telegram.max-retries'");
                }
                if (!telegramSection.isSet("retry-delay")) {
                    issues.add("Missing 'telegram.retry-delay'");
                }
                if (!telegramSection.isSet("message")) {
                    issues.add("Missing 'telegram.message'");
                }
            }
        }

        // Check restrictedPermissions section
        if (!topLevelKeys.contains("restrictedPermissions") || config.getMapList("restrictedPermissions").isEmpty()) {
            issues.add("Missing or empty 'restrictedPermissions' section");
        }

        // Show warnings if there are issues
        if (!issues.isEmpty()) {
            plugin.getLogger().warning("========== CONFIG ISSUES DETECTED ==========");
            plugin.getLogger().warning("Found " + issues.size() + " configuration issue(s):");

            for (String issue : issues) {
                plugin.getLogger().warning("  - " + issue);
            }

            plugin.getLogger().warning("");
            plugin.getLogger().warning("Using default values for missing configuration options.");
            plugin.getLogger().warning("For a complete configuration example, see:");
            plugin.getLogger().warning(CONFIG_EXAMPLE_URL);
            plugin.getLogger().warning("============================================");
        } else if (debug) {
            plugin.getLogger().info("[CONFIG-DEBUG] All configuration sections are present!");
        }
    }

    private ConfigData loadConfig(boolean debug) {
        GeneralConfig general = loadGeneralConfig(debug);
        TelegramConfig telegram = loadTelegramConfig(debug);
        PermissionConfig permissions = loadPermissionConfig();

        return new ConfigData(general, telegram, permissions);
    }

    private GeneralConfig loadGeneralConfig(boolean debug) {
        if (debug) plugin.getLogger().info("[CONFIG-DEBUG] Loading general config...");

        boolean shutdownOnDisable = config.getBoolean("settings.shutdown-on-disable", ConfigDefaults.SHUTDOWN_ON_DISABLE);
        boolean configDebug = config.getBoolean("settings.config-debug", ConfigDefaults.CONFIG_DEBUG);

        // Check if using default values - only show individual warnings if section exists
        Set<String> topLevelKeys = config.getKeys(false);
        if (topLevelKeys.contains("settings")) {
            ConfigurationSection settingsSection = config.getConfigurationSection("settings");
            if (settingsSection != null) {
                if (!settingsSection.isSet("shutdown-on-disable")) {
                    plugin.getLogger().warning("Missing 'settings.shutdown-on-disable'. Using default: " + ConfigDefaults.SHUTDOWN_ON_DISABLE);
                }
                if (!settingsSection.isSet("config-debug")) {
                    plugin.getLogger().warning("Missing 'settings.config-debug'. Using default: " + ConfigDefaults.CONFIG_DEBUG);
                }
            }
        }
        // If section is missing entirely, we already reported it in checkConfigurationIssues()

        if (debug) {
            plugin.getLogger().info("[CONFIG-DEBUG] General settings loaded:");
            plugin.getLogger().info("[CONFIG-DEBUG]   shutdown-on-disable: " + shutdownOnDisable);
            plugin.getLogger().info("[CONFIG-DEBUG]   config-debug: " + configDebug);
        }

        return new GeneralConfig(shutdownOnDisable, configDebug);
    }

    private TelegramConfig loadTelegramConfig(boolean debug) {
        if (debug) plugin.getLogger().info("[CONFIG-DEBUG] Loading telegram config...");

        boolean enabled = config.getBoolean("telegram.enabled", ConfigDefaults.TELEGRAM_ENABLED);
        String botToken = config.getString("telegram.bot-token", ConfigDefaults.BOT_TOKEN);
        String chatIdsString = config.getString("telegram.chat-ids", ConfigDefaults.CHAT_IDS);
        String[] chatIds = chatIdsString.split(",");
        int maxRetries = config.getInt("telegram.max-retries", ConfigDefaults.MAX_RETRIES);
        long retryDelay = config.getLong("telegram.retry-delay", ConfigDefaults.RETRY_DELAY);
        String message = config.getString("telegram.message", ConfigDefaults.MESSAGE);

        // Check if using default values - only show individual warnings if section exists
        Set<String> topLevelKeys = config.getKeys(false);
        if (topLevelKeys.contains("telegram")) {
            ConfigurationSection telegramSection = config.getConfigurationSection("telegram");
            if (telegramSection != null) {
                if (!telegramSection.isSet("enabled")) {
                    plugin.getLogger().warning("Missing 'telegram.enabled'. Using default: " + ConfigDefaults.TELEGRAM_ENABLED);
                }
                if (!telegramSection.isSet("bot-token")) {
                    plugin.getLogger().warning("Missing 'telegram.bot-token'. Using default: " + ConfigDefaults.BOT_TOKEN);
                }
                if (!telegramSection.isSet("chat-ids")) {
                    plugin.getLogger().warning("Missing 'telegram.chat-ids'. Using default: " + ConfigDefaults.CHAT_IDS);
                }
                if (!telegramSection.isSet("max-retries")) {
                    plugin.getLogger().warning("Missing 'telegram.max-retries'. Using default: " + ConfigDefaults.MAX_RETRIES);
                }
                if (!telegramSection.isSet("retry-delay")) {
                    plugin.getLogger().warning("Missing 'telegram.retry-delay'. Using default: " + ConfigDefaults.RETRY_DELAY);
                }
                if (!telegramSection.isSet("message")) {
                    plugin.getLogger().warning("Missing 'telegram.message'. Using default: <default template>");
                }
            }
        }
        // If section is missing entirely, we already reported it in checkConfigurationIssues()

        if (debug) {
            plugin.getLogger().info("[CONFIG-DEBUG] Telegram config loaded:");
            plugin.getLogger().info("[CONFIG-DEBUG]   enabled: " + enabled);
            plugin.getLogger().info("[CONFIG-DEBUG]   bot-token: " + (botToken.equals(ConfigDefaults.BOT_TOKEN) ? "DEFAULT" : "CUSTOM"));
            plugin.getLogger().info("[CONFIG-DEBUG]   chat-ids: '" + chatIdsString + "'");
            plugin.getLogger().info("[CONFIG-DEBUG]   max-retries: " + maxRetries);
            plugin.getLogger().info("[CONFIG-DEBUG]   retry-delay: " + retryDelay);
        }

        // Validate configuration
        if (enabled) {
            boolean hasValidToken = !botToken.trim().isEmpty() && !ConfigDefaults.BOT_TOKEN.equals(botToken);
            boolean hasValidChatIds = !chatIdsString.trim().isEmpty() && !ConfigDefaults.CHAT_IDS.equals(chatIdsString);

            if (debug) {
                plugin.getLogger().info("[CONFIG-DEBUG] Validation:");
                plugin.getLogger().info("[CONFIG-DEBUG]   hasValidToken: " + hasValidToken);
                plugin.getLogger().info("[CONFIG-DEBUG]   hasValidChatIds: " + hasValidChatIds);
            }

            if (!hasValidToken) {
                plugin.getLogger().warning("Telegram is enabled but bot-token is not properly configured!");
            }
            if (!hasValidChatIds) {
                plugin.getLogger().warning("Telegram is enabled but chat-ids are not properly configured!");
            }

            if (!hasValidToken || !hasValidChatIds) {
                plugin.getLogger().warning("Telegram notifications will be disabled due to invalid configuration.");
                plugin.getLogger().info("Please configure bot-token and chat-ids, then reload the plugin.");
                enabled = false;
            }
        }

        return new TelegramConfig(enabled, botToken, chatIds, maxRetries, retryDelay, message);
    }

    @SuppressWarnings("unchecked")
    private PermissionConfig loadPermissionConfig() {
        List<Map<String, Object>> rawPermissions =
                (List<Map<String, Object>>) (Object) config.getMapList("restrictedPermissions");

        if (rawPermissions.isEmpty()) {
            plugin.getLogger().warning("No restricted permissions found in config!");
        }

        List<PermissionConfig.PermissionEntry> permissions = new ArrayList<>();
        for (Map<String, Object> entry : rawPermissions) {
            String permission = (String) entry.get("permission");
            String command = (String) entry.get("cmd");
            boolean log = (boolean) entry.getOrDefault("log", true);
            String kickMessage = (String) entry.get("kickMessage");

            if (permission == null || command == null || kickMessage == null) {
                plugin.getLogger().warning("Invalid permission entry found, skipping...");
                continue;
            }

            permissions.add(new PermissionConfig.PermissionEntry(permission, command, log, kickMessage));
        }

        return new PermissionConfig(permissions);
    }

    // Direct getters
    public GeneralConfig general() {
        return configData.general();
    }

    public TelegramConfig telegram() {
        return configData.telegram();
    }

    public PermissionConfig permissions() {
        return configData.permissions();
    }
}
