package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class NullStyler implements TaskDef<Supplier<?>, @Nullable Styling> {
    @Inject public NullStyler() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @SuppressWarnings("NullableProblems") @Override
    public @Nullable Styling exec(ExecContext context, Supplier<?> input) throws Exception {
        return null;
    }
}
