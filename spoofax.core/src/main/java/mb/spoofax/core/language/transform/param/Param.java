package mb.spoofax.core.language.transform.param;

import mb.common.util.ADT;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class Param {
    interface Cases<R> {
        R option(String name, Class<?> type, boolean required, ListView<ArgProvider> providers);

        R positional(int index, Class<?> type, boolean required, ListView<ArgProvider> providers);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
