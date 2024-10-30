package uz.alex2276564.permguard;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.commands.reloadcommand.ReloadCommand;
import uz.alex2276564.permguard.listeners.PlayerJoinListener;
import uz.alex2276564.permguard.utils.ConfigManager;

public final class PermGuard extends JavaPlugin {
    @Getter
    private static PermGuard instance;

    @Override
    public void onEnable() {
        instance = this;
        registerListeners();
        registerCommands();
        loadUtils();
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

    @Override
    public void onDisable() {
        getLogger().info("The server is shutting down because PermGuard was disabled.");
        getServer().shutdown();
    }
}
