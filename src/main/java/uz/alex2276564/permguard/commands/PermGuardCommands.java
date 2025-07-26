package uz.alex2276564.permguard.commands;

import uz.alex2276564.permguard.commands.framework.builder.*;
import uz.alex2276564.permguard.commands.subcommands.reload.ReloadSubCommand;
import uz.alex2276564.permguard.commands.subcommands.help.HelpSubCommand;

public class PermGuardCommands {

    public static BuiltCommand createPermGuardCommand() {
        CommandBuilder builder = CommandManager.create("permguard")
                .permission("permguard.command")
                .description("Main PermGuard command");

        // Register all subcommands
        new ReloadSubCommand().build(builder);
        new HelpSubCommand().build(builder);

        return builder.build();
    }
}