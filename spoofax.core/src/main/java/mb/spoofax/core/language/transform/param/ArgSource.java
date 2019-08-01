package mb.spoofax.core.language.transform.param;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class ArgSource {
    interface Cases<R> {
        R context();

        // TODO: configuration files

        // TODO: environment variables
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
