package mb.spoofax.compiler.command;

import mb.common.util.ADT;
import mb.common.util.ListView;
import mb.spoofax.compiler.util.TypeInfo;
import mb.spoofax.core.language.cli.CliParams;
import mb.spoofax.core.language.command.arg.ArgConverter;
import org.checkerframework.checker.nullness.qual.Nullable;

@ADT
public abstract class CliParamRepr {
    interface Cases<R> {
        R option(String paramId, ListView<String> names, boolean negatable, @Nullable String label, @Nullable String description, @Nullable TypeInfo converter);

        R positional(String paramId, int index, @Nullable String label, @Nullable String description, @Nullable TypeInfo converter);
    }


    public abstract <R> R match(Cases<R> cases);


    public CliParamReprs.CaseOfMatchers.TotalMatcher_Option caseOf() {
        return CliParamReprs.caseOf(this);
    }

    public String getParamId() {
        return CliParamReprs.getParamId(this);
    }

    public @Nullable String getLabel() {
        return CliParamReprs.getLabel(this);
    }

    public @Nullable String getDescription() {
        return CliParamReprs.getDescription(this);
    }

    public @Nullable TypeInfo getConverter() {
        return CliParamReprs.getConverter(this);
    }


    @Override public abstract int hashCode();

    @Override public abstract boolean equals(@Nullable Object obj);

    @Override public abstract String toString();
}
