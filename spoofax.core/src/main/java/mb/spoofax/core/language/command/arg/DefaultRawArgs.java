package mb.spoofax.core.language.command.arg;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

public class DefaultRawArgs implements RawArgs {
    private final HashMap<String, Serializable> optionArgs;
    private final HashMap<Integer, Serializable> positionalArgs;

    public DefaultRawArgs(HashMap<String, Serializable> optionArgs, HashMap<Integer, Serializable> positionalArgs) {
        this.optionArgs = optionArgs;
        this.positionalArgs = positionalArgs;
    }

    @Override public <T extends Serializable> Optional<T> getOption(String name) {
        final @Nullable Object obj = optionArgs.get(name);
        if(obj == null) {
            return Optional.empty();
        } else {
            @SuppressWarnings("unchecked") final T arg = (T) obj;
            return Optional.of(arg);
        }
    }

    @Override public <T extends Serializable> Optional<T> getPositional(int index) {
        final @Nullable Object obj = positionalArgs.get(index);
        if(obj == null) {
            return Optional.empty();
        } else {
            @SuppressWarnings("unchecked") final T arg = (T) obj;
            return Optional.of(arg);
        }
    }

    @Override public boolean equals(@Nullable Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        final DefaultRawArgs other = (DefaultRawArgs) obj;
        return optionArgs.equals(other.optionArgs) &&
            positionalArgs.equals(other.positionalArgs);
    }

    @Override public int hashCode() {
        return Objects.hash(optionArgs, positionalArgs);
    }

    @Override public String toString() {
        return "DefaultRawArgs(" +
            "  optionArgs     = " + optionArgs +
            ", positionalArgs = " + positionalArgs +
            ')';
    }
}
