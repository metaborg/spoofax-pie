package mb.spoofax.core.language.cli;

import mb.spoofax.core.language.command.CommandDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class CliCommand implements CliCommandItem {
    private final CommandDef<?> def;
    private final String name;
    private final CliParamDef paramDef;
    private final @Nullable String description;


    public CliCommand(CommandDef<?> def, String name, CliParamDef paramDef, @Nullable String description) {
        this.def = def;
        this.name = name;
        this.paramDef = paramDef;
        this.description = description;
    }

    public static CliCommand of(CommandDef<?> def, String name, CliParamDef paramDef) {
        return new CliCommand(def, name, paramDef, null);
    }

    public static CliCommand of(CommandDef<?> def, String name, CliParamDef paramDef, @Nullable String description) {
        return new CliCommand(def, name, paramDef, description);
    }


    public CommandDef<?> getDef() {
        return def;
    }

    @Override public String getName() {
        return name;
    }

    public CliParamDef getParamDef() {
        return paramDef;
    }

    @Override public @Nullable String getDescription() {
        return description;
    }


    @Override public void accept(CliCommandItemVisitor visitor) {
        visitor.command(def, name, paramDef, description);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CliCommand other = (CliCommand) obj;
        return def.equals(other.def) &&
            name.equals(other.name) &&
            paramDef.equals(other.paramDef) &&
            Objects.equals(description, other.description);
    }

    @Override public int hashCode() {
        return Objects.hash(def, name, paramDef, description);
    }

    @Override public String toString() {
        return "CliCommandBinding{" +
            "def=" + def +
            ", name='" + name + '\'' +
            ", paramDef=" + paramDef +
            ", description='" + description + '\'' +
            '}';
    }
}
