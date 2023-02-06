package mb.spt.lut;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.CoordinateRequirement;
import mb.spt.model.LanguageUnderTest;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FailingLanguageUnderTestProvider implements LanguageUnderTestProvider {
    @Override
    public Result<LanguageUnderTest, ?> provide(ExecContext context, ResourceKey file, @Nullable ResourcePath rootDirectoryHint, @Nullable CoordinateRequirement languageCoordinateRequirementHint) {
        return Result.ofErr(new Exception("Cannot provide language under test, as the language under test provider was set to one that always fails"));
    }
}
