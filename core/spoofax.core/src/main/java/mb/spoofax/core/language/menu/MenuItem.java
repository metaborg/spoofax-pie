package mb.spoofax.core.language.menu;

import mb.common.util.ADT;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class MenuItem implements Serializable {
    interface Cases<R> {
        R commandAction(CommandAction commandAction);

        R menu(String displayName, @Nullable String description, ListView<MenuItem> items);

        R separator(@Nullable String displayName);
    }

    public static MenuItem commandAction(CommandAction commandAction) {
        return MenuItems.commandAction(commandAction);
    }

    public static MenuItem menu(String displayName, String description, ListView<MenuItem> items) {
        return MenuItems.menu(displayName, description, items);
    }

    public static MenuItem menu(String displayName, String description, MenuItem... items) {
        return MenuItems.menu(displayName, description, ListView.of(items));
    }

    public static MenuItem menu(String displayName, ListView<MenuItem> items) {
        return MenuItems.menu(displayName, null, items);
    }

    public static MenuItem menu(String displayName, MenuItem... items) {
        return MenuItems.menu(displayName, null, ListView.of(items));
    }

    public static MenuItem separator(String description) {
        return MenuItems.separator(description);
    }

    public static MenuItem separator() {
        return MenuItems.separator(null);
    }


    public abstract <R> R match(Cases<R> cases);

    public MenuItems.CaseOfMatchers.TotalMatcher_CommandAction caseOf() {
        return MenuItems.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
