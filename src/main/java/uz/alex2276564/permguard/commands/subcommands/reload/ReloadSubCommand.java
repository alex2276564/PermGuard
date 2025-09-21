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

                    MessagesConfig msg = PermGuard.getInstance().getConfigManager().getMessagesConfig();
                    try {
                        PermGuard.getInstance().getConfigManager().reload();

                        PermGuard.getInstance().getMessageManager().sendMessageKeyed(sender, "commands.reload.success", msg.commands.reload.success, "type", type);

                    } catch (Exception e) {
                        PermGuard.getInstance().getMessageManager().sendMessageKeyed(sender, msg.commands.reload.error, "commands.reload.error", "error", e.getMessage());
                    }
                });
    }
}