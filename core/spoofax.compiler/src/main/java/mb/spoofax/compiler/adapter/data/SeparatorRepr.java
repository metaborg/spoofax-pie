package mb.spoofax.compiler.adapter.data;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface SeparatorRepr {
    class Builder extends ImmutableSeparatorRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static SeparatorRepr of() {
        return ImmutableSeparatorRepr.of(Optional.empty());
    }

    static SeparatorRepr of(String displayName) {
        return ImmutableSeparatorRepr.of(Optional.of(displayName));
    }


    @Value.Parameter Optional<String> displayName();
}
