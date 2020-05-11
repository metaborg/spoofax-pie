package mb.spoofax.compiler.menu;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface MenuRepr extends Serializable {
    class Builder extends ImmutableMenuRepr.Builder {}

    static Builder builder() {
        return new Builder();
    }

    static MenuRepr of(String displayName, String description, List<MenuItemRepr> items) {
        return builder().displayName(displayName).description(description).addAllItems(items).build();
    }

    static MenuRepr of(String displayName, String description, MenuItemRepr... items) {
        return builder().displayName(displayName).description(description).addItems(items).build();
    }

    static MenuRepr of(String displayName, List<MenuItemRepr> items) {
        return builder().displayName(displayName).addAllItems(items).build();
    }

    static MenuRepr of(String displayName, MenuItemRepr... items) {
        return builder().displayName(displayName).addItems(items).build();
    }


    String displayName();

    Optional<String> description();

    List<MenuItemRepr> items();
}
