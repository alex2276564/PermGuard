package uz.alex2276564.permguard.commands.subcommands.help;

import uz.alex2276564.permguard.PermGuardServices;
import uz.alex2276564.permguard.commands.framework.builder.CommandBuilder;
import uz.alex2276564.permguard.commands.framework.builder.SubCommandBuilder;
import uz.alex2276564.permguard.commands.framework.builder.SubCommandProvider;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;

public class HelpSubCommand implements SubCommandProvider {

    private final PermGuardServices services;

    public HelpSubCommand(PermGuardServices services) {
        this.services = services;
    }

    @Override
    public SubCommandBuilder build(CommandBuilder parent) {
        return parent.subcommand("help")
                .permission("permguard.command")
                .description("Show help information")
                .executor((sender, context) -> {
                    var configManager = services.configManager();
                    var messageManager = services.messageManager();
                    MessagesConfig msg = configManager.getMessagesConfig();

                    messageManager.sendMessageKeyed(sender, "commands.help.header", msg.commands.help.header);
                    messageManager.sendMessageKeyed(sender, "commands.help.reloadLine", msg.commands.help.reloadLine);
                    messageManager.sendMessageKeyed(sender, "commands.help.helpLine", msg.commands.help.helpLine);
                });
    }
}