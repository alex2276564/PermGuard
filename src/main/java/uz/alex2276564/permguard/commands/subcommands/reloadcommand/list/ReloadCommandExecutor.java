package uz.alex2276564.permguard.commands.subcommands.reloadcommand.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.framework.BaseSubCommand;

import java.util.Collections;
import java.util.List;

public class ReloadCommandExecutor extends BaseSubCommand {

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String[] args) {
        PermGuard.getInstance().getConfigManager().reload();
        sendSuccessMessage(sender, "PermGuard configuration successfully reloaded.");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String[] args) {
        // Bukkit does not give out the list of players
        return Collections.emptyList();
    }
}
