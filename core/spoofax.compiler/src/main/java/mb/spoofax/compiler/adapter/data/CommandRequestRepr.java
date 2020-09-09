package mb.spoofax.compiler.adapter.data;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Value.Immutable
public interface CommandRequestRepr extends Serializable {
    class Builder extends ImmutableCommandRequestRepr.Builder {}

    static Builder builder() { return new Builder(); }

    static CommandRequestRepr of(TypeInfo commandDefType, CommandExecutionType executionType, Map<String, String> initialArgs) {
        return ImmutableCommandRequestRepr.of(commandDefType, executionType, initialArgs);
    }

    static CommandRequestRepr of(TypeInfo commandDefType, CommandExecutionType executionType) {
        return ImmutableCommandRequestRepr.of(commandDefType, executionType, new HashMap<>());
    }


    @Value.Parameter TypeInfo commandDefType();

    @Value.Parameter CommandExecutionType executionType();

    @Value.Parameter Map<String, String> initialArgs();
}
