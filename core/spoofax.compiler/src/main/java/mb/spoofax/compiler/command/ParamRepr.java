package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface ParamRepr extends Serializable {
    class Builder extends ImmutableParamRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    @Value.Parameter String id();

    @Value.Parameter TypeInfo type();

    @Value.Parameter @Value.Default default boolean required() {
        return true;
    }

    @Value.Parameter Optional<TypeInfo> converter();

    @Value.Parameter List<ArgProviderRepr> providers();
}
