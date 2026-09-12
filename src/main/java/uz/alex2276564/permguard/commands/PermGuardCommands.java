package uz.alex2276564.permguard.commands;

import uz.alex2276564.permguard.PermGuardServices;
import uz.alex2276564.permguard.commands.framework.builder.BuiltCommand;
import uz.alex2276564.permguard.commands.framework.builder.CommandBuilder;
import uz.alex2276564.permguard.commands.framework.builder.CommandManager;
import uz.alex2276564.permguard.commands.subcommands.help.HelpSubCommand;
import uz.alex2276564.permguard.commands.subcommands.reload.ReloadSubCommand;

public class PermGuardCommands {

    public static BuiltCommand createPermGuardCommand(PermGuardServices services) {
        CommandBuilder builder = CommandManager.create("permguard")
                .permission("permguard.command")
                .description("Main PermGuard command");

        // Register all subcommands
        new ReloadSubCommand(services).build(builder);
        new HelpSubCommand(services).build(builder);

        return builder.build();
    }
}