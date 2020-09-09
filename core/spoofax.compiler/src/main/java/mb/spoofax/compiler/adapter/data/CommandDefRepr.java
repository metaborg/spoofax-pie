package mb.spoofax.compiler.adapter.data;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value.Immutable
public interface CommandDefRepr extends Serializable {
    class Builder extends ImmutableCommandDefRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    TypeInfo type();

    TypeInfo taskDefType();

    TypeInfo argType();

    String displayName();

    String description();

    Set<CommandExecutionType> supportedExecutionTypes();

    List<ParamRepr> params();


    default CommandRequestRepr request(CommandExecutionType executionType, Map<String, String> initialArgs) {
        return CommandRequestRepr.of(type(), executionType, initialArgs);
    }

    default CommandRequestRepr request(CommandExecutionType executionType) {
        return CommandRequestRepr.of(type(), executionType);
    }
}
