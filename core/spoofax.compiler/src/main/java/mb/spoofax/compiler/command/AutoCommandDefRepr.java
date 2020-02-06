package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Map;

@Value.Immutable
public interface AutoCommandDefRepr extends Serializable {
    class Builder extends ImmutableAutoCommandDefRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    TypeInfo commandDef();

    Map<String, String> rawArgs();
}
