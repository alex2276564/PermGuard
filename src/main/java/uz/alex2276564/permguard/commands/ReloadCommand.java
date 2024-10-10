package uz.alex2276564.permguard.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uz.alex2276564.permguard.utils.ConfigManager;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        String permission = "permguard.reload";

        if (!commandSender.hasPermission(permission)) {
            commandSender.sendMessage("§cYou do not have permission to use this command. Missing permission: §e" + permission);
            return true;
        }

        ConfigManager.reload();
        commandSender.sendMessage("PermGuard configuration reloaded.");
        return true;
    }
}
