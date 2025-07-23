package uz.alex2276564.permguard.commands.framework;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiSubCommandExecutor implements TabExecutor {

    @Getter(AccessLevel.PROTECTED)
    private final List<SubCommandWrapper> subCommands = new ArrayList<>();

    protected void addSubCommand(SubCommand command, String[] aliases, Permission permission) {
        this.subCommands.add(new SubCommandWrapper(command, Arrays.asList(aliases), permission));
    }

    @Nullable
    protected SubCommandWrapper getWrapperFromLabel(String label) {
        for (SubCommandWrapper wrapper : subCommands) {
            for (String alias : wrapper.aliases()) {
                if (alias.equalsIgnoreCase(label)) {
                    return wrapper;
                }
            }
        }
        return null;
    }

    protected List<String> getFirstAliases() {
        final List<String> result = new ArrayList<>();
        for (final SubCommandWrapper wrapper : subCommands) {
            String alias = wrapper.aliases().get(0);
            result.add(alias);
        }
        return result;
    }

    protected List<String> getAvailableAliases(CommandSender sender) {
        final List<String> result = new ArrayList<>();
        for (final SubCommandWrapper wrapper : subCommands) {
            if (sender.hasPermission(wrapper.permission())) {
                result.add(wrapper.aliases().get(0));
            }
        }
        return result;
    }

    public record SubCommandWrapper(
            SubCommand command,
            List<String> aliases,
            Permission permission
    ) {}
}
