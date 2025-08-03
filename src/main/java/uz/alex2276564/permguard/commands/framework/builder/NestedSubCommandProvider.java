package uz.alex2276564.permguard.commands.framework.builder;

public interface NestedSubCommandProvider {
    SubCommandBuilder build(SubCommandBuilder parent);
}