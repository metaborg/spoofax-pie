package mb.spoofax.compiler.menu;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Value.Immutable
public interface MenuRepr extends MenuItemRepr, Serializable {
    class Builder extends ImmutableMenuRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static ImmutableMenuRepr of(String displayName, List<MenuItemRepr> items) {
        return ImmutableMenuRepr.of(displayName, items);
    }

    static ImmutableMenuRepr of(String displayName, MenuItemRepr... items) {
        return ImmutableMenuRepr.of(displayName, Arrays.asList(items));
    }


    @Value.Parameter String displayName();

    @Value.Parameter List<MenuItemRepr> items();
}
