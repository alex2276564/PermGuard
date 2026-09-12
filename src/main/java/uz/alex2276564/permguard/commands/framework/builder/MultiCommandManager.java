package uz.alex2276564.permguard.commands.framework.builder;

import org.bukkit.plugin.java.JavaPlugin;
import uz.alex2276564.permguard.PermGuardServices;

import java.util.HashMap;
import java.util.Map;

public class MultiCommandManager {
    private final JavaPlugin plugin;
    private final PermGuardServices services;
    private final Map<String, CommandManager> managers = new HashMap<>();

    public MultiCommandManager(JavaPlugin plugin, PermGuardServices services) {
        this.plugin = plugin;
        this.services = services;
    }

    public void registerCommand(BuiltCommand command) {
        CommandManager manager = new CommandManager(plugin, services.messageManager());
        manager.register(command);
        managers.put(command.name().toLowerCase(), manager);
    }

    public void registerCommands(BuiltCommand... commands) {
        for (BuiltCommand command : commands) {
            registerCommand(command);
        }
    }

    public CommandManager getManager(String commandName) {
        return managers.get(commandName.toLowerCase());
    }
}