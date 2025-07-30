package uz.alex2276564.permguard.commands.subcommands.reload;

import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.framework.builder.*;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;

public class ReloadSubCommand implements SubCommandProvider {

    @Override
    public SubCommandBuilder build(CommandBuilder parent) {
        return parent.subcommand("reload")
                .permission("permguard.reload")
                .description("Reload plugin configuration")
                .argument(new ArgumentBuilder<>("type", ArgumentType.STRING)
                        .optional("config")
                        .suggestions("config", "all"))
                .executor((sender, context) -> {
                    String type = context.getArgument("type");

                    try {
                        PermGuard.getInstance().getConfigManager().reload();

                        MessagesConfig msg = PermGuard.getInstance().getConfigManager().getMessagesConfig();
                        String successMessage = msg.commands.reload.success.replace("{type}", type);

                        PermGuard.getInstance().getMessageManager().sendMessage(sender, successMessage);

                    } catch (Exception e) {
                        MessagesConfig msg = PermGuard.getInstance().getConfigManager().getMessagesConfig();
                        String errorMessage = msg.commands.reload.error.replace("{error}", e.getMessage());

                        PermGuard.getInstance().getMessageManager().sendMessage(sender, errorMessage);
                    }
                });
    }
}