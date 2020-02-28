package mb.spoofax.compiler.command;

import mb.common.util.ADT;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.EnclosingCommandContextType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@ADT
public abstract class ArgProviderRepr implements Serializable {
    interface Cases<R> {
        R value(String code);

        R context(CommandContextType contextType);

        R enclosingContext(EnclosingCommandContextType enclosingContextType);
    }

    public static ArgProviderRepr value(String code) {
        return ArgProviderReprs.value(code);
    }

    public static ArgProviderRepr context(CommandContextType contextType) {
        return ArgProviderReprs.context(contextType);
    }

    public static ArgProviderRepr enclosingContext(EnclosingCommandContextType enclosingContextType) {
        return ArgProviderReprs.enclosingContext(enclosingContextType);
    }


    public abstract <R> R match(Cases<R> cases);

    public ArgProviderReprs.CaseOfMatchers.TotalMatcher_Value caseOf() {
        return ArgProviderReprs.caseOf(this);
    }

    public String toJavaCode() {
        return caseOf()
            .value((code) -> "ArgProvider.value(" + code + ")")
            .context((t) -> "ArgProvider.context(mb.spoofax.core.language.command.CommandContextType." + t + ")")
            .enclosingContext((t) -> "ArgProvider.enclosingContext(mb.spoofax.core.language.command.EnclosingCommandContextType." + t + ")")
            ;
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
