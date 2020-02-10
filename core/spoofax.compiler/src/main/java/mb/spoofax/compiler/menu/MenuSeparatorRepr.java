package mb.spoofax.compiler.menu;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface MenuSeparatorRepr extends MenuItemRepr, Serializable {
    static MenuSeparatorRepr of() {
        return ImmutableMenuSeparatorRepr.builder().build();
    }
}
