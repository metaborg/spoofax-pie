package mb.spoofax.compiler.menu;

import org.immutables.value.Value;

@Value.Immutable
public interface MenuSeparatorRepr extends MenuItemRepr {
    static MenuSeparatorRepr of() {
        return ImmutableMenuSeparatorRepr.builder().build();
    }
}
