package mb.spoofax.compiler.cli;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface CliCommandRepr extends Serializable {
    class Builder extends ImmutableCliCommandRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableCliCommandRepr of(String name, String description, List<CliCommandRepr> subCommands) {
        return ImmutableCliCommandRepr.of(name, Optional.of(description), Optional.empty(), new ArrayList<>(), subCommands);
    }

    static ImmutableCliCommandRepr of(String name, String description, CliCommandRepr... subCommands) {
        return ImmutableCliCommandRepr.of(name, Optional.of(description), Optional.empty(), new ArrayList<>(), Arrays.asList(subCommands));
    }

    static ImmutableCliCommandRepr of(String name, List<CliCommandRepr> subCommands) {
        return ImmutableCliCommandRepr.of(name, Optional.empty(), Optional.empty(), new ArrayList<>(), subCommands);
    }

    static ImmutableCliCommandRepr of(String name, CliCommandRepr... subCommands) {
        return ImmutableCliCommandRepr.of(name, Optional.empty(), Optional.empty(), new ArrayList<>(), Arrays.asList(subCommands));
    }

    static ImmutableCliCommandRepr of(String name, String description, TypeInfo commandDefType, List<CliParamRepr> params) {
        return ImmutableCliCommandRepr.of(name, Optional.of(description), Optional.of(commandDefType), params, new ArrayList<>());
    }

    static ImmutableCliCommandRepr of(String name, String description, TypeInfo commandDefType, CliParamRepr... params) {
        return ImmutableCliCommandRepr.of(name, Optional.of(description), Optional.of(commandDefType), Arrays.asList(params), new ArrayList<>());
    }

    static ImmutableCliCommandRepr of(String name, TypeInfo commandDefType, List<CliParamRepr> params) {
        return ImmutableCliCommandRepr.of(name, Optional.empty(), Optional.of(commandDefType), params, new ArrayList<>());
    }

    static ImmutableCliCommandRepr of(String name, TypeInfo commandDefType, CliParamRepr... params) {
        return ImmutableCliCommandRepr.of(name, Optional.empty(), Optional.of(commandDefType), Arrays.asList(params), new ArrayList<>());
    }


    @Value.Parameter String name();

    @Value.Parameter Optional<String> description();

    @Value.Parameter Optional<TypeInfo> commandDefType();

    @Value.Parameter List<CliParamRepr> params();

    @Value.Parameter List<CliCommandRepr> subCommands();
}
