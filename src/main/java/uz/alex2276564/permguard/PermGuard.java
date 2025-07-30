package uz.alex2276564.permguard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.commands.PermGuardCommands;
import uz.alex2276564.permguard.commands.framework.builder.BuiltCommand;
import uz.alex2276564.permguard.commands.framework.builder.MultiCommandManager;
import uz.alex2276564.permguard.config.PermGuardConfigManager;
import uz.alex2276564.permguard.listeners.PlayerJoinListener;
import uz.alex2276564.permguard.utils.runner.BukkitRunner;
import uz.alex2276564.permguard.utils.runner.Runner;
import uz.alex2276564.permguard.utils.UpdateChecker;
import uz.alex2276564.permguard.utils.adventure.AdventureMessageManager;
import uz.alex2276564.permguard.utils.adventure.LegacyMessageManager;
import uz.alex2276564.permguard.utils.adventure.MessageManager;

public final class PermGuard extends JavaPlugin {
    @Getter
    private static PermGuard instance;

    @Getter
    private Runner runner;

    @Getter
    private PermGuardConfigManager configManager;

    @Getter
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            setupRunner();
            setupMessageManager();
            setupConfig();
            registerListeners();
            registerCommands();
            checkUpdates();

            getLogger().info("PermGuard has been enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable PermGuard: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupRunner() {
        runner = new BukkitRunner(this);
    }

    private void setupMessageManager() {
        if (isMiniMessageAvailable()) {
            try {
                messageManager = new AdventureMessageManager();
                getLogger().info("Using Adventure MiniMessage for text formatting - full MiniMessage syntax supported");
                return;
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Adventure MiniMessage: " + e.getMessage());
                getLogger().warning("Falling back to Legacy formatting...");
            }
        }

        messageManager = new LegacyMessageManager();
        getLogger().info("Using Legacy ChatColor formatting with MiniMessage syntax compatibility");
        getLogger().info("You can continue using MiniMessage syntax in your config - basic tags will be converted automatically");
        getLogger().info("Supported: colors, bold, italic, underlined, strikethrough, obfuscated, reset");
        getLogger().info("Note: Complex features (gradients, hover, click events) are not available on older server versions");
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
        configManager = new PermGuardConfigManager(this);
        configManager.reload();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void registerCommands() {
        MultiCommandManager multiManager = new MultiCommandManager(this);

        BuiltCommand permGuardCommand = PermGuardCommands.createPermGuardCommand();
        multiManager.registerCommand(permGuardCommand);
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, "alex2276564/PermGuard", runner);
        updateChecker.checkForUpdates();
    }

    @Override
    public void onDisable() {
        if (runner != null) {
            runner.cancelTasks();
        }

        shutdown();
    }

    private void shutdown() {
        if (configManager != null && configManager.getMainConfig().settings.shutdownOnDisable) {
            getLogger().info("The server is shutting down because PermGuard was disabled and shutdown-on-disable is enabled.");
            getServer().shutdown();
        } else {
            getLogger().warning("PermGuard has been disabled but the server will continue running. " +
                    "Please ensure your server security is maintained through other means!");
        }
    }
}
