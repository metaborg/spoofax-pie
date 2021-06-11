package mb.spt.lut;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FailingLanguageUnderTestProvider implements LanguageUnderTestProvider {
    @Override
    public Result<LanguageComponent, ?> provide(ExecContext context, ResourceKey file, @Nullable ResourcePath rootDirectoryHint, @Nullable String languageIdHint) {
        return Result.ofErr(new Exception("Cannot provide language under test, as the language under test provider was set to one that always fails"));
    }
}
