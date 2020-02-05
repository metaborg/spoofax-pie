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

    public static ArgProvider value(Serializable arg) {
        return ArgProviders.value(arg);
    }

    public static ArgProvider context() {
        return ArgProviders.context();
    }


    public abstract <R> R match(Cases<R> cases);

    public ArgProviders.CaseOfMatchers.TotalMatcher_Value caseOf() {
        return ArgProviders.caseOf(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
