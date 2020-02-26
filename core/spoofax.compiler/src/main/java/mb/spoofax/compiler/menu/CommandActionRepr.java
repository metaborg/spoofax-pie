package mb.spoofax.compiler.menu;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Value.Immutable
public interface CommandActionRepr extends Serializable {
    class Builder extends ImmutableCommandActionRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableCommandActionRepr of(TypeInfo commandDefType, CommandExecutionType executionType, String displayName, Map<String, String> initialArgs) {
        return ImmutableCommandActionRepr.of(commandDefType, executionType, Optional.of(displayName), initialArgs);
    }

    static ImmutableCommandActionRepr of(TypeInfo commandDefType, CommandExecutionType executionType, Map<String, String> initialArgs) {
        return ImmutableCommandActionRepr.of(commandDefType, executionType, Optional.empty(), initialArgs);
    }

    static ImmutableCommandActionRepr of(TypeInfo commandDefType, CommandExecutionType executionType, String displayName) {
        return ImmutableCommandActionRepr.of(commandDefType, executionType, Optional.of(displayName), new HashMap<>());
    }

    static ImmutableCommandActionRepr of(TypeInfo commandDefType, CommandExecutionType executionType) {
        return ImmutableCommandActionRepr.of(commandDefType, executionType, Optional.empty(), new HashMap<>());
    }


    @Value.Parameter TypeInfo commandDefType();

    @Value.Parameter CommandExecutionType executionType();

    @Value.Parameter Optional<String> displayName();

    @Value.Parameter Map<String, String> initialArgs();
}
