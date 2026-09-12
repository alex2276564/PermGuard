package uz.alex2276564.permguard.commands.subcommands.reload;

import uz.alex2276564.permguard.PermGuardServices;
import uz.alex2276564.permguard.commands.framework.builder.*;
import uz.alex2276564.permguard.config.configs.messagesconfig.MessagesConfig;
import uz.alex2276564.permguard.utils.SecurityUtils;

public class ReloadSubCommand implements SubCommandProvider {

    private final PermGuardServices services;

    public ReloadSubCommand(PermGuardServices services) {
        this.services = services;
    }

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

                    var configManager = services.configManager();
                    var messageManager = services.messageManager();
                    MessagesConfig msg = configManager.getMessagesConfig();

                    try {
                        configManager.reload();

                        messageManager.sendMessageKeyed(
                                sender,
                                "commands.reload.success",
                                msg.commands.reload.success,
                                "type", type
                        );

                    } catch (Exception e) {
                        String raw = e.getMessage() != null ? e.getMessage() : "unknown";
                        String error = SecurityUtils.sanitize(raw, SecurityUtils.SanitizeType.ERROR_MESSAGE);
                        messageManager.sendMessageKeyed(
                                sender,
                                msg.commands.reload.error,
                                "commands.reload.error",
                                "error", error
                        );
                    }
                });
    }
}