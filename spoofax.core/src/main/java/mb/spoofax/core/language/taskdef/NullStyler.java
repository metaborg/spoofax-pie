package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class NullStyler implements TaskDef<ResourceKey, @Nullable Styling> {
    @Inject public NullStyler() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @SuppressWarnings("NullableProblems") @Override
    public @Nullable Styling exec(ExecContext context, ResourceKey input) throws Exception {
        return null;
    }
}
