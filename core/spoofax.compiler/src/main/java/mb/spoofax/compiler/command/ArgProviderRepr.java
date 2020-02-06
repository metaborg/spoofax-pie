package mb.spoofax.compiler.command;

import mb.common.util.ADT;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class ArgProviderRepr implements Serializable {
    interface Cases<R> {
        R value(String code);

        R context();
    }

    public static ArgProviderRepr value(String code) {
        return ArgProviderReprs.value(code);
    }

    public static ArgProviderRepr context() {
        return ArgProviderReprs.context();
    }


    public abstract <R> R match(Cases<R> cases);

    public ArgProviderReprs.CaseOfMatchers.TotalMatcher_Value caseOf() {
        return ArgProviderReprs.caseOf(this);
    }

    public String toJavaCode() {
        return caseOf()
            .value((code) -> "ArgProvider.value(" + code + ")")
            .context_("ArgProvider.context()")
            ;
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
