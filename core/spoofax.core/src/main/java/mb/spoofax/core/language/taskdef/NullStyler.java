package mb.spoofax.core.language.taskdef;

import mb.common.style.Styling;
import mb.common.token.Token;
import mb.pie.api.ExecContext;
import mb.pie.api.Provider;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;

@LanguageScope
public class NullStyler implements TaskDef<Provider<?>, @Nullable Styling> {
    @Inject public NullStyler() {}

    @Override public String getId() {
        return getClass().getName();
    }

    @SuppressWarnings("NullableProblems") @Override
    public @Nullable Styling exec(ExecContext context, Provider<?> input) throws Exception {
        return null;
    }
}
