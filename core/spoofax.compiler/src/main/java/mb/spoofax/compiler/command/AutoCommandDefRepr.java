package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Value.Immutable
public interface AutoCommandDefRepr extends Serializable {
    class Builder extends ImmutableAutoCommandDefRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableAutoCommandDefRepr of(TypeInfo commandDef, Map<String, String> rawArgs) {
        return ImmutableAutoCommandDefRepr.of(commandDef, rawArgs);
    }

    static ImmutableAutoCommandDefRepr of(TypeInfo commandDef) {
        return ImmutableAutoCommandDefRepr.of(commandDef, new HashMap<>());
    }


    @Value.Parameter TypeInfo commandDef();

    @Value.Parameter Map<String, String> rawArgs();
}
