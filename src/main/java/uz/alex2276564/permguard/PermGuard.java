package uz.alex2276564.permguard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.commands.PermGuardCommands;
import uz.alex2276564.permguard.commands.framework.builder.BuiltCommand;
import uz.alex2276564.permguard.commands.framework.builder.MultiCommandManager;
import uz.alex2276564.permguard.config.PermGuardConfigManager;
import uz.alex2276564.permguard.listeners.PlayerJoinListener;
import uz.alex2276564.permguard.telegram.TelegramNotifier;
import uz.alex2276564.permguard.utils.HttpUtils;
import uz.alex2276564.permguard.utils.UpdateChecker;
import uz.alex2276564.permguard.utils.adventure.AdventureMessageManager;
import uz.alex2276564.permguard.utils.adventure.LegacyMessageManager;
import uz.alex2276564.permguard.utils.adventure.MessageManager;
import uz.alex2276564.permguard.utils.backup.BackupManager;
import uz.alex2276564.permguard.utils.runner.FoliaRunner;
import uz.alex2276564.permguard.utils.runner.Runner;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PermGuard extends JavaPlugin {

    @Getter
    private Runner runner;

    @Getter
    private HttpUtils httpUtils;

    @Getter
    private PermGuardConfigManager configManager;

    @Getter
    private BackupManager backupManager;

    @Getter
    private TelegramNotifier telegramNotifier;

    @Getter
    private MessageManager messageManager;

    @Getter
    private UpdateChecker updateChecker;

    @Getter
    private PermGuardServices services;

    @Override
    public void onEnable() {
        try {
            setupRunner();
            setupHttpClient();
            setupMessageManager();
            setupConfig();
            setupBackupManager();
            setupTelegramNotifier();
            setupServices();
            setupUpdateChecker();
            registerListeners();
            registerCommands();

            getLogger().info("PermGuard has been enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable PermGuard", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupRunner() {
        runner = new FoliaRunner(this);
        getLogger().info("Initialized " + runner.getPlatformName() + " scheduler support");

        if (runner.isFolia()) {
            getLogger().info("Folia detected - using RegionScheduler and EntityScheduler for optimal performance");
        }
    }

    private void setupHttpClient() {
        this.httpUtils = new HttpUtils();
    }

    private void setupMessageManager() {
        if (isMiniMessageAvailable()) {
            try {
                messageManager = new AdventureMessageManager(runner);
                getLogger().info("Using Adventure MiniMessage for text formatting - full MiniMessage syntax supported");
                return;
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Adventure MiniMessage: " + e.getMessage());
                getLogger().warning("Falling back to Legacy formatting...");
            }
        }

        messageManager = new LegacyMessageManager();
        getLogger().info("Using Legacy ChatColor formatting with MiniMessage-like basic tags");
        getLogger().info("Supported: colors, bold, italic, underlined, strikethrough, obfuscated, reset");
        getLogger().info("Note: Complex features (gradients, hover, click events) are not available on older versions");
    }

    private boolean isMiniMessageAvailable() {
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            return true;
        } catch (ClassNotFoundException e) {
            getLogger().info("MiniMessage library not found - this is normal for Paper versions below 1.18");
            return false;
        }
    }

    private void setupConfig() {
        File dataFolder = getDataFolder();
        Logger logger = getLogger();
        this.configManager = new PermGuardConfigManager(
                dataFolder,
                logger,
                messageManager,
                getClassLoader()
        );
        configManager.reload();
    }

    private void setupBackupManager() {
        backupManager = new BackupManager(runner, getLogger(), getDataFolder().toPath());

        // Check for backup need on startup
        backupManager.checkAndBackupAsync();

        // Schedule periodic checks - daily (24 hours)
        long dailySeconds = 24L * 60L * 60L;
        long dailyTicks = Runner.secondsToTicks(dailySeconds);
        runner.runAsyncTimer(() -> backupManager.checkAndBackupAsync(), dailyTicks, dailyTicks);
    }

    private void setupTelegramNotifier() {
        this.telegramNotifier = new TelegramNotifier(
                configManager,
                httpUtils,
                runner,
                getDescription().getName(),
                getDescription().getVersion(),
                getLogger()
        );
    }

    private void setupServices() {
        this.services = new PermGuardServices(
                runner,
                configManager,
                messageManager,
                telegramNotifier,
                getLogger()
        );
    }

    private void setupUpdateChecker() {
        this.updateChecker = new UpdateChecker(
                getDescription().getName(),
                getDescription().getVersion(),
                "alex2276564/PermGuard",
                runner,
                httpUtils,
                getLogger()
        );

        updateChecker.checkForUpdates();
    }

    private void registerListeners() {
        Path dataFolderPath = getDataFolder().toPath();

        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(
                        configManager,
                        runner,
                        telegramNotifier,
                        messageManager,
                        getLogger(),
                        dataFolderPath
                ),
                this
        );
    }

    private void registerCommands() {
        MultiCommandManager multiManager = new MultiCommandManager(this, services);

        BuiltCommand permGuardCommand = PermGuardCommands.createPermGuardCommand(services);
        multiManager.registerCommand(permGuardCommand);
    }

    @Override
    public void onDisable() {
        if (runner != null) {
            runner.cancelAllTasks();
        }
        if (telegramNotifier != null) {
            telegramNotifier.shutdown();
        }
        handleDisableAndOptionalShutdown();
    }

    /**
     * Handle plugin disable and optionally shut down the server based on configuration.
     */
    private void handleDisableAndOptionalShutdown() {
        if (configManager != null && configManager.getMainConfig().settings.shutdownOnDisable) {
            getLogger().severe("PermGuard was disabled. For security reasons the server will now shut down "
                    + "(settings.shutdownOnDisable = true).");
            getServer().shutdown();
        } else {
            getLogger().warning("PermGuard has been disabled but the server will continue running "
                    + "(settings.shutdownOnDisable = false).");
            getLogger().warning("Make sure you understand the security implications and have other protection in place.");
        }
    }
}
