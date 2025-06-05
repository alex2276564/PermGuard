package uz.alex2276564.permguard.commands.reloadcommand.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.SubCommand;

import java.util.Collections;
import java.util.List;

public class ReloadCommandExecutor implements SubCommand {

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String[] args) {
        String permission = "permguard.reload";

        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou do not have permission to use this command. Missing permission: §e" + permission);
            return;
        }

        PermGuard.getInstance().getConfigManager().reload();
        sender.sendMessage("§aPermGuard configuration successfully reloaded.");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String[] args) {
        // Bukkit does not give out the list of players
        return Collections.emptyList();
    }
}
