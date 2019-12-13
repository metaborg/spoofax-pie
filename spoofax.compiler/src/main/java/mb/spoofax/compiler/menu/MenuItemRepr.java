package mb.spoofax.compiler.menu;

public interface MenuItemRepr {
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
