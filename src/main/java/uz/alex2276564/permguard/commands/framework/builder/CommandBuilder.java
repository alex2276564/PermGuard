package uz.alex2276564.permguard.commands.framework.builder;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Getter
public class CommandBuilder {
    private final String name;
    private String permission;
    private String description;
    private BiConsumer<CommandSender, CommandContext> executor;
    private final Map<String, SubCommandBuilder> subCommands = new HashMap<>();
    private final List<ArgumentBuilder<?>> arguments = new ArrayList<>();

    public CommandBuilder(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be null or empty");
        }
        this.name = name.toLowerCase();
    }

    public CommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder executor(BiConsumer<CommandSender, CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public SubCommandBuilder subcommand(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subcommand name cannot be null or empty");
        }
        String lowerName = name.toLowerCase();
        if (subCommands.containsKey(lowerName)) {
            throw new IllegalArgumentException("Subcommand '" + name + "' already exists");
        }

        SubCommandBuilder sub = new SubCommandBuilder(name, this);
        subCommands.put(lowerName, sub);
        return sub;
    }

    public <T> CommandBuilder argument(ArgumentBuilder<T> argument) {
        if (argument == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
        this.arguments.add(argument);
        return this;
    }

    public BuiltCommand build() {
        Map<String, BuiltSubCommand> builtSubCommands = new HashMap<>();
        for (Map.Entry<String, SubCommandBuilder> entry : subCommands.entrySet()) {
            SubCommandBuilder sub = entry.getValue();
            builtSubCommands.put(entry.getKey(), sub.buildSubCommand());
        }

        return new BuiltCommand(name, permission, description, executor, builtSubCommands, arguments);
    }
}