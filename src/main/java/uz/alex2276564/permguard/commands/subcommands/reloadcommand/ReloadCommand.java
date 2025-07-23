package uz.alex2276564.permguard.commands.subcommands.reloadcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.framework.MultiSubCommandExecutor;
import uz.alex2276564.permguard.commands.subcommands.reloadcommand.list.ReloadCommandExecutor;

import java.util.List;

public class ReloadCommand extends MultiSubCommandExecutor {

    public ReloadCommand() {
        addSubCommand(new ReloadCommandExecutor(), new String[]{"config", "all"}, new Permission("permguard.reload"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 2) {
            if (!commandSender.hasPermission("permguard.reload")) {
                PermGuard.getInstance().getMessageManager().sendMessage(commandSender, "<red>You do not have permission to use this command. Missing permission: <yellow>permguard.reload");
                return true;
            }

            new ReloadCommandExecutor().onExecute(commandSender, new String[0]);
            return true;
        }

        final SubCommandWrapper wrapper = getWrapperFromLabel(args[1]);
        if (wrapper == null) {
            PermGuard.getInstance().getMessageManager().sendMessage(commandSender, "<red>Unknown reload option. Available: <yellow>" + String.join(", ", getAvailableAliases(commandSender)));
            return true;
        }

        if (!commandSender.hasPermission(wrapper.permission())) {
            commandSender.sendMessage("§cYou do not have permission to use this command. Missing permission: §e" + wrapper.permission().getName());
            return true;
        }

        String[] newArgs = new String[args.length - 2];
        System.arraycopy(args, 2, newArgs, 0, args.length - 2);
        wrapper.command().onExecute(commandSender, newArgs);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 2) {
            return getAvailableAliases(commandSender);
        }

        if (args.length >= 3) {
            final SubCommandWrapper wrapper = getWrapperFromLabel(args[1]);
            if (wrapper == null) return null;

            String[] newArgs = new String[args.length - 2];
            System.arraycopy(args, 2, newArgs, 0, args.length - 2);
            return wrapper.command().onTabComplete(commandSender, newArgs);
        }

        return null;
    }
}
