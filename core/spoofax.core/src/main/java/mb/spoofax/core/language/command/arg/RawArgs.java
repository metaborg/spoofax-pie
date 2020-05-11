package mb.spoofax.core.language.command.arg;

import mb.common.util.MapView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class RawArgs implements Serializable {
    final MapView<String, Serializable> args;


    public RawArgs(MapView<String, Serializable> args) {
        this.args = args;
    }


    public <T extends Serializable> Optional<T> get(String id) {
        final @Nullable Serializable obj = args.get(id);
        if(obj == null) {
            return Optional.empty();
        } else {
            @SuppressWarnings("unchecked") final T arg = (T) obj;
            return Optional.of(arg);
        }
    }

    public <T extends Serializable> @Nullable T getOrNull(String id) {
        return this.<T>get(id).orElse(null);
    }

    public <T extends Serializable> T getOrThrow(String id) {
        return this.<T>get(id).orElseThrow(() -> new RuntimeException("No argument with ID '" + id + "'"));
    }

    public boolean getOrFalse(String id) {
        return this.<Boolean>get(id).orElse(false);
    }

    public boolean getOrTrue(String id) {
        return this.<Boolean>get(id).orElse(true);
    }


    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final RawArgs other = (RawArgs) obj;
        return args.equals(other.args);
    }

    @Override public int hashCode() {
        return Objects.hash(args);
    }

    @Override public String toString() {
        return args.toString();
    }
}
