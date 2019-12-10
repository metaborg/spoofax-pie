package mb.spoofax.compiler.menu;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface MenuRepr extends MenuItemRepr {
    class Builder extends ImmutableMenuRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }


    List<MenuItemRepr> items();

    String displayName();
}
