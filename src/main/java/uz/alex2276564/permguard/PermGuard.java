package uz.alex2276564.permguard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.commands.MainCommandExecutor;
import uz.alex2276564.permguard.listeners.PlayerJoinListener;
import uz.alex2276564.permguard.runner.BukkitRunner;
import uz.alex2276564.permguard.runner.Runner;
import uz.alex2276564.permguard.config.ConfigManager;
import uz.alex2276564.permguard.utils.UpdateChecker;

public final class PermGuard extends JavaPlugin {
    @Getter
    private static PermGuard instance;

    @Getter
    private Runner runner;

    @Getter
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        setupRunner();

        configManager = new ConfigManager(this);
        configManager.reload();

        registerListeners();
        registerCommands();
        checkUpdates();

        getLogger().info("PermGuard has been enabled!");
    }


    private void setupRunner() {
        runner = new BukkitRunner(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void registerCommands() {
        // Register main command executor that handles all subcommands
        getCommand("permguard").setExecutor(new MainCommandExecutor(this));
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
        getLogger().info("The server is shutting down because PermGuard was disabled.");
        getServer().shutdown();
    }
}
