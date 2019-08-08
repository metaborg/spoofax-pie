package mb.spoofax.core.language.cli;

import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class CliCommandList implements CliCommandItem {
    private final String name;
    private final ListView<CliCommandItem> items;
    private final @Nullable String description;


    public CliCommandList(String name, ListView<CliCommandItem> items, @Nullable String description) {
        this.name = name;
        this.items = items;
        this.description = description;
    }

    public static CliCommandList of(String name, CliCommandItem item) {
        return new CliCommandList(name, ListView.of(item), null);
    }

    public static CliCommandList of(String name, CliCommandItem... items) {
        return new CliCommandList(name, ListView.of(items), null);
    }

    public static CliCommandList of(String name, ListView<CliCommandItem> items) {
        return new CliCommandList(name, items, null);
    }

    public static CliCommandList of(String name, @Nullable String description, CliCommandItem item) {
        return new CliCommandList(name, ListView.of(item), description);
    }

    public static CliCommandList of(String name, @Nullable String description, CliCommandItem... items) {
        return new CliCommandList(name, ListView.of(items), description);
    }

    public static CliCommandList of(String name, @Nullable String description, ListView<CliCommandItem> items) {
        return new CliCommandList(name, items, description);
    }


    @Override public String getName() {
        return name;
    }

    @Override public @Nullable String getDescription() {
        return description;
    }

    public ListView<CliCommandItem> getItems() {
        return items;
    }


    @Override public void accept(CliCommandItemVisitor visitor) {
        visitor.commandListPush(name, description);
        for(CliCommandItem item : items) {
            item.accept(visitor);
        }
        visitor.commandListPop();
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final CliCommandList other = (CliCommandList) obj;
        return name.equals(other.name) &&
            items.equals(other.items) &&
            Objects.equals(description, other.description);
    }

    @Override public int hashCode() {
        return Objects.hash(name, items, description);
    }

    @Override public String toString() {
        return "CliCommandList{" +
            "name='" + name + '\'' +
            ", items=" + items +
            ", description='" + description + '\'' +
            '}';
    }
}
