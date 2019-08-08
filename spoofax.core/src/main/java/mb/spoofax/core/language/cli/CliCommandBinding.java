package mb.spoofax.core.language.cli;

import mb.spoofax.core.language.command.CommandDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class CliCommandBinding {
    private final CommandDef<?> def;
    private final String name;
    private final CliParamDef paramDef;
    private final @Nullable String description;


    public CliCommandBinding(CommandDef<?> def, String name, CliParamDef paramDef, @Nullable String description) {
        this.def = def;
        this.name = name;
        this.description = description;
        this.paramDef = paramDef;
    }


    public static CliCommandBinding of(CommandDef<?> def, String name, CliParamDef paramDef) {
        return new CliCommandBinding(def, name, paramDef, null);
    }

    public static CliCommandBinding of(CommandDef<?> def, String name, CliParamDef paramDef, @Nullable String description) {
        return new CliCommandBinding(def, name, paramDef, description);
    }


    public CommandDef<?> getDef() {
        return def;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public CliParamDef getParamDef() {
        return paramDef;
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CliCommandBinding other = (CliCommandBinding) obj;
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
