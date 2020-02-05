package mb.spoofax.core.language.cli;

import mb.common.util.ListView;
import mb.spoofax.core.language.command.CommandDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class CliCommand {
    private final String name;
    private final @Nullable String description;
    private final @Nullable CommandDef<?> commandDef;
    private final ListView<CliParam> params;
    private final ListView<CliCommand> subCommands;


    public CliCommand(String name, @Nullable String description, @Nullable CommandDef<?> commandDef, ListView<CliParam> params, ListView<CliCommand> subCommands) {
        this.commandDef = commandDef;
        this.name = name;
        this.params = params;
        this.subCommands = subCommands;
        this.description = description;
    }

    public static CliCommand of(String name) {
        return new CliCommand(name, null, null, ListView.of(), ListView.of());
    }

    public static CliCommand of(String name, String description) {
        return new CliCommand(name, description, null, ListView.of(), ListView.of());
    }

    public static CliCommand of(String name, CliCommand... subCommands) {
        return new CliCommand(name, null, null, ListView.of(), ListView.of(subCommands));
    }

    public static CliCommand of(String name, ListView<CliCommand> subCommands) {
        return new CliCommand(name, null, null, ListView.of(), subCommands);
    }

    public static CliCommand of(String name, String description, CliCommand... subCommands) {
        return new CliCommand(name, description, null, ListView.of(), ListView.of(subCommands));
    }

    public static CliCommand of(String name, String description, ListView<CliCommand> subCommands) {
        return new CliCommand(name, description, null, ListView.of(), subCommands);
    }

    public static CliCommand of(String name, CommandDef<?> def, CliParam... params) {
        return new CliCommand(name, null, def, ListView.of(params), ListView.of());
    }

    public static CliCommand of(String name, CommandDef<?> def, ListView<CliParam> params) {
        return new CliCommand(name, null, def, params, ListView.of());
    }

    public static CliCommand of(String name, String description, CommandDef<?> def, CliParam... params) {
        return new CliCommand(name, description, def, ListView.of(params), ListView.of());
    }

    public static CliCommand of(String name, String description, CommandDef<?> def, ListView<CliParam> params) {
        return new CliCommand(name, description, def, params, ListView.of());
    }

    public static CliCommand of(String name, CommandDef<?> def, ListView<CliParam> params, ListView<CliCommand> subCommands) {
        return new CliCommand(name, null, def, params, subCommands);
    }

    public static CliCommand of(String name, String description, CommandDef<?> def, ListView<CliParam> params, ListView<CliCommand> subCommands) {
        return new CliCommand(name, description, def, params, subCommands);
    }


    public String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable CommandDef<?> getCommandDef() {
        return commandDef;
    }

    public ListView<CliParam> getParams() {
        return params;
    }

    public ListView<CliCommand> getSubCommands() {
        return subCommands;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final CliCommand that = (CliCommand)o;
        return name.equals(that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(commandDef, that.commandDef) &&
            params.equals(that.params) &&
            subCommands.equals(that.subCommands);
    }

    @Override public int hashCode() {
        return Objects.hash(name, description, commandDef, params, subCommands);
    }

    @Override public String toString() {
        return "CliCommand{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", commandDef=" + commandDef +
            ", params=" + params +
            ", subCommands=" + subCommands +
            '}';
    }
}
