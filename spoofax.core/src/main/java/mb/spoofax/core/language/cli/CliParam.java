package mb.spoofax.core.language.cli;

import mb.common.util.ADT;
import mb.common.util.ListView;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CliParam {
    interface Cases<R> {
        R option(String paramId, ListView<String> names, @Nullable String label, @Nullable String description);

        R positional(String paramId, int index, @Nullable String label, @Nullable String description);
    }


    public abstract <R> R match(Cases<R> cases);


    public String getParamId() {
        return CliParams.getParamId(this);
    }

    public @Nullable String getLabel() {
        return CliParams.getLabel(this);
    }

    public @Nullable String getDescription() {
        return CliParams.getDescription(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
