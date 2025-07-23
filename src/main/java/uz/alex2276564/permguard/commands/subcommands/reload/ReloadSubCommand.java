package uz.alex2276564.permguard.commands.subcommands.reload;

import uz.alex2276564.permguard.PermGuard;
import uz.alex2276564.permguard.commands.framework.builder.*;

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

                    PermGuard.getInstance().getConfigManager().reload();

                    PermGuard.getInstance().getMessageManager().sendMessage(sender,
                            "<green>PermGuard configuration successfully reloaded (" + type + ").");
                });
    }
}