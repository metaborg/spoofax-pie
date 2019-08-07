package mb.spoofax.core.language.command.arg;

import mb.common.util.ADT;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class Param {
    interface Cases<R> {
        R option(String name, Class<? extends Serializable> type, boolean required, ListView<ArgProvider> providers);

        R positional(int index, Class<? extends Serializable> type, boolean required, ListView<ArgProvider> providers);
    }

    public abstract <R> R match(Cases<R> cases);

    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
