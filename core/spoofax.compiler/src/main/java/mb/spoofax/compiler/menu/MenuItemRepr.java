package mb.spoofax.compiler.menu;

import java.io.Serializable;

public interface MenuItemRepr extends Serializable {
    default boolean isCommandAction() {
        return this instanceof MenuCommandActionRepr;
    }

    default boolean isMenu() {
        return this instanceof MenuRepr;
    }

    default boolean isSeparator() {
        return this instanceof MenuSeparatorRepr;
    }
}
