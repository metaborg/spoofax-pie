package mb.spoofax.compiler.adapter.data;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@ADT
public abstract class MenuItemRepr implements Serializable {
    interface Cases<R> {
        R commandAction(CommandActionRepr commandAction);

        R menu(MenuRepr menu);

        R separator(SeparatorRepr separator);
    }

    public static MenuItemRepr commandAction(CommandActionRepr commandActionRepr) {
        return MenuItemReprs.commandAction(commandActionRepr);
    }

    public static MenuItemRepr menu(String displayName, String description, List<MenuItemRepr> items) {
        return MenuItemReprs.menu(MenuRepr.of(displayName, description, items));
    }

    public static MenuItemRepr menu(String displayName, String description, MenuItemRepr... items) {
        return MenuItemReprs.menu(MenuRepr.of(displayName, description, items));
    }

    public static MenuItemRepr menu(String displayName, List<MenuItemRepr> items) {
        return MenuItemReprs.menu(MenuRepr.of(displayName, items));
    }

    public static MenuItemRepr menu(String displayName, MenuItemRepr... items) {
        return MenuItemReprs.menu(MenuRepr.of(displayName, items));
    }

    public static MenuItemRepr separator(String displayName) {
        return MenuItemReprs.separator(SeparatorRepr.of(displayName));
    }

    public static MenuItemRepr separator() {
        return MenuItemReprs.separator(SeparatorRepr.of());
    }


    public abstract <R> R match(MenuItemRepr.Cases<R> cases);

    public MenuItemReprs.CaseOfMatchers.TotalMatcher_CommandAction caseOf() {
        return MenuItemReprs.caseOf(this);
    }

    public Optional<CommandActionRepr> getCommandAction() {
        return MenuItemReprs.getCommandAction(this);
    }

    public Optional<MenuRepr> getMenu() {
        return MenuItemReprs.getMenu(this);
    }

    public Optional<SeparatorRepr> getSeparator() {
        return MenuItemReprs.getSeparator(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
