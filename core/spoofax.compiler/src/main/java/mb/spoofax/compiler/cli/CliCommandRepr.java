package mb.spoofax.compiler.cli;

import mb.spoofax.compiler.util.TypeInfo;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface CliCommandRepr extends Serializable {
    class Builder extends ImmutableCliCommandRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    String name();

    Optional<String> description();

    Optional<TypeInfo> commandDefType();

    List<CliParamRepr> params();

    List<CliCommandRepr> subCommands();
}
