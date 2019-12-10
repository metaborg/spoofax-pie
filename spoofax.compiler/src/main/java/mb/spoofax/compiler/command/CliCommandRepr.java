package mb.spoofax.compiler.command;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.cli.CliParam;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface CliCommandRepr {
    class Builder extends ImmutableCliCommandRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    String name();

    Optional<String> description();

    Optional<TypeInfo> commandDefType();

    List<CliParam> params();

    List<CliCommandRepr> subCommands();
}
