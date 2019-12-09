package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public interface AutoCommandDefRepr {
    class Builder extends ImmutableAutoCommandDefRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    TypeInfo commandDefType();

    Map<String, String> rawArgs();
}
