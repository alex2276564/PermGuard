package uz.alex2276564.permguard.commands.subcommands.help;

import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.framework.builder.*;

public class HelpSubCommand implements SubCommandProvider {

    @Override
    public SubCommandBuilder build(CommandBuilder parent) {
        return parent.subcommand("help")
                .permission("permguard.command")
                .description("Show help information")
                .executor((sender, context) -> {
                    PermGuard.getInstance().getMessageManager().sendMessage(sender,
                            "<gold>=== PermGuard Help ===");
                    PermGuard.getInstance().getMessageManager().sendMessage(sender,
                            "<yellow>/permguard reload [type] <gray>- Reload the plugin configuration");
                    PermGuard.getInstance().getMessageManager().sendMessage(sender,
                            "<yellow>/permguard help <gray>- Show this help message");
                });
    }
}