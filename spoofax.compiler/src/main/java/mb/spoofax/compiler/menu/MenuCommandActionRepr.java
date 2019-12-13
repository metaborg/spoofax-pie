package mb.spoofax.compiler.menu;

import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.command.CommandExecutionType;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@Value.Immutable
public interface MenuCommandActionRepr extends MenuItemRepr {
    class Builder extends ImmutableMenuCommandActionRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    TypeInfo commandDefType();

    CommandExecutionType executionType();

    Map<String, String> initialArgs();

    Optional<String> displayName();
}
