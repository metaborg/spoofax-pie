package mb.spoofax.core.language.command.arg;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class ArgProvider {
    interface Cases<R> {
        R value(Serializable arg);

        R context();

        // TODO: configuration files

        // TODO: environment variables
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
