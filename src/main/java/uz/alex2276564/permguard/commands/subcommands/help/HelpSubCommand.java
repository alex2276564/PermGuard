package uz.alex2276564.permguard.commands.subcommands.help;

import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.framework.builder.*;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;

public class HelpSubCommand implements SubCommandProvider {

    @Override
    public SubCommandBuilder build(CommandBuilder parent) {
        return parent.subcommand("help")
                .permission("permguard.command")
                .description("Show help information")
                .executor((sender, context) -> {
                    PermGuard plugin = PermGuard.getInstance();
                    MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();

                    plugin.getMessageManager().sendMessage(sender, msg.commands.help.header);
                    plugin.getMessageManager().sendMessage(sender, msg.commands.help.reloadLine);
                    plugin.getMessageManager().sendMessage(sender, msg.commands.help.helpLine);
                });
    }
}