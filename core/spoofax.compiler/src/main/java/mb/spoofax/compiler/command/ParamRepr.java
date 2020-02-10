package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface ParamRepr extends Serializable {
    class Builder extends ImmutableParamRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableParamRepr of(String id, TypeInfo type, boolean required, TypeInfo converter, List<ArgProviderRepr> providers) {
        return ImmutableParamRepr.of(id, type, required, Optional.of(converter), providers);
    }

    static ImmutableParamRepr of(String id, TypeInfo type, boolean required, TypeInfo converter, ArgProviderRepr... providers) {
        return ImmutableParamRepr.of(id, type, required, Optional.of(converter), Arrays.asList(providers));
    }

    static ImmutableParamRepr of(String id, TypeInfo type, boolean required, List<ArgProviderRepr> providers) {
        return ImmutableParamRepr.of(id, type, required, Optional.empty(), providers);
    }

    static ImmutableParamRepr of(String id, TypeInfo type, boolean required, ArgProviderRepr... providers) {
        return ImmutableParamRepr.of(id, type, required, Optional.empty(), Arrays.asList(providers));
    }

    static ImmutableParamRepr of(String id, TypeInfo type, boolean required) {
        return ImmutableParamRepr.of(id, type, required, Optional.empty(), new ArrayList<>());
    }


    @Value.Parameter String id();

    @Value.Parameter TypeInfo type();

    @Value.Parameter @Value.Default default boolean required() {
        return true;
    }

    @Value.Parameter Optional<TypeInfo> converter();

    @Value.Parameter List<ArgProviderRepr> providers();
}
