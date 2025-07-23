package uz.alex2276564.permguard.commands.framework.builder;

public interface SubCommandProvider {
    SubCommandBuilder build(CommandBuilder parent);
}