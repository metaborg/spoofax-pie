package mb.spoofax.core.language.command.arg;

import mb.common.util.ListView;
import mb.common.util.MapView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class RawArgsCollection implements Serializable {
    public final MapView<String, Serializable> optionArgs;
    public final ListView<Serializable> positionalArgs;

    public RawArgsCollection(MapView<String, Serializable> optionArgs, ListView<Serializable> positionalArgs) {
        this.optionArgs = optionArgs;
        this.positionalArgs = positionalArgs;
    }

    public RawArgsCollection(MapView<String, Serializable> optionArgs) {
        this.optionArgs = optionArgs;
        this.positionalArgs = ListView.of();
    }

    public RawArgsCollection(ListView<Serializable> positionalArgs) {
        this.optionArgs = MapView.of();
        this.positionalArgs = positionalArgs;
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final RawArgsCollection other = (RawArgsCollection) obj;
        return optionArgs.equals(other.optionArgs) &&
            positionalArgs.equals(other.positionalArgs);
    }

    @Override public int hashCode() {
        return Objects.hash(optionArgs, positionalArgs);
    }

    @Override public String toString() {
        return "RawArgsCollection(" +
            "  optionArgs     = " + optionArgs +
            ", positionalArgs = " + positionalArgs +
            ')';
    }
}
