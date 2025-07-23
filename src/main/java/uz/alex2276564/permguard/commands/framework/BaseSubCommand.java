package uz.alex2276564.permguard.commands.framework;

import org.bukkit.command.CommandSender;
import uz.alex2276564.permguard.PermGuard;

public abstract class BaseSubCommand implements SubCommand {

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            PermGuard.getInstance().getMessageManager().sendMessage(sender, "<red>You do not have permission to use this command. Missing permission: <yellow>" + permission);
            return false;
        }
        return true;
    }

    protected void sendSuccessMessage(CommandSender sender, String message) {
        PermGuard.getInstance().getMessageManager().sendMessage(sender, "<green>" + message);
    }

    protected void sendErrorMessage(CommandSender sender, String message) {
        PermGuard.getInstance().getMessageManager().sendMessage(sender, "<red>" + message);
    }
}
