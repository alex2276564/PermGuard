package uz.alex2276564.permguard;

import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.commands.ReloadCommand;
import uz.alex2276564.permguard.listeners.JoinListener;
import uz.alex2276564.permguard.utils.ConfigManager;

public final class PermGuard extends JavaPlugin {
    private static PermGuard instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigManager.reload();
        registerListeners();
        registerCommands();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
    }

    private void registerCommands() {
        getCommand("permguardreload").setExecutor(new ReloadCommand());
    }

    public static PermGuard getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        getLogger().info("The server is shutting down because PermGuard was disabled.");
        getServer().shutdown();
    }
}