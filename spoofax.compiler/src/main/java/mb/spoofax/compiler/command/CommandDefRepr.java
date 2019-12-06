package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.ClassInfo;
import mb.spoofax.compiler.util.TaskDefRef;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;

@Value.Immutable
public interface CommandDefRepr {
    class Builder extends ImmutableCommandDefRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    ClassInfo commandDefClass();

    TaskDefRef taskDef();

    ClassInfo argClass();

    String displayName();

    Set<CommandExecutionType> supportedExecutionTypes();

    Set<CommandContextType> requiredContextTypes();

    List<ParamRepr> params();
}
