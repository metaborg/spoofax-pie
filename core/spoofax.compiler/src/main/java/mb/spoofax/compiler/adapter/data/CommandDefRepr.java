package mb.spoofax.compiler.adapter.data;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashSet;
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

    @Value.Default default TypeInfo argType() {
        return taskDefType().appendToId(".Args");
    }

    String displayName();

    @Value.Default default String description() { return ""; }

    @Value.Default default Set<CommandExecutionType> supportedExecutionTypes() {
        final Set<CommandExecutionType> types = new HashSet<>();
        types.add(CommandExecutionType.ManualOnce);
        types.add(CommandExecutionType.ManualContinuous);
        types.add(CommandExecutionType.AutomaticContinuous);
        return types;
    }

    List<ParamRepr> params();


    default CommandRequestRepr request(CommandExecutionType executionType, Map<String, String> initialArgs) {
        return CommandRequestRepr.of(type(), executionType, initialArgs);
    }

    default CommandRequestRepr request(CommandExecutionType executionType) {
        return CommandRequestRepr.of(type(), executionType);
    }
}
