package uz.alex2276564.permguard.commands.framework.builder;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public record BuiltCommand(String name, String permission, String description,
                           BiConsumer<CommandSender, CommandContext> executor, Map<String, BuiltSubCommand> subCommands,
                           List<ArgumentBuilder<?>> arguments) {
}