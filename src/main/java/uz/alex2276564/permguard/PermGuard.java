package uz.alex2276564.permguard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.commands.reloadcommand.ReloadCommand;
import uz.alex2276564.permguard.listeners.PlayerJoinListener;
import uz.alex2276564.permguard.task.BukkitRunner;
import uz.alex2276564.permguard.task.Runner;
import uz.alex2276564.permguard.utils.ConfigManager;
import uz.alex2276564.permguard.utils.UpdateChecker;

public final class PermGuard extends JavaPlugin {
    @Getter
    private static PermGuard instance;

    @Getter
    private Runner runner;

    @Override
    public void onEnable() {
        instance = this;
        setupRunner();
        registerListeners();
        registerCommands();
        loadUtils();
        checkUpdates();
    }

    private void setupRunner() {
        runner = new BukkitRunner(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    private void registerCommands() {
        getCommand("permguard").setExecutor(new ReloadCommand());
    }

    private void loadUtils() {
        ConfigManager.reload();
    }

    private void checkUpdates() {
        UpdateChecker updateChecker = new UpdateChecker(this, "alex2276564/PermGuard", runner);
        updateChecker.checkForUpdates();
    }

    @Override
    public void onDisable() {
        runner.cancelTasks();
        getLogger().info("The server is shutting down because PermGuard was disabled.");
        getServer().shutdown();
    }
}
