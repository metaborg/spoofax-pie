package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.ClassInfo;
import mb.spoofax.core.language.command.arg.ArgProvider;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface ParamRepr {
    class Builder extends ImmutableParamRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    @Value.Parameter String id();

    @Value.Parameter ClassInfo type();

    @Value.Parameter @Value.Default default boolean required() {
        return true;
    }

    @Value.Parameter Optional<ClassInfo> converter();

    @Value.Parameter List<ArgProviderRepr> providers();
}
