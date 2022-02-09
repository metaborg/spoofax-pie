package mb.spt.lut;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.CoordinateRequirement;
import mb.spt.model.LanguageUnderTest;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StaticLanguageUnderTestProvider implements LanguageUnderTestProvider {
    private final LanguageUnderTest languageUnderTest;

    public StaticLanguageUnderTestProvider(LanguageUnderTest languageUnderTest) {
        this.languageUnderTest = languageUnderTest;
    }

    @Override
    public Result<LanguageUnderTest, ?> provide(ExecContext context, ResourceKey file, @Nullable ResourcePath rootDirectoryHint, @Nullable CoordinateRequirement languageCoordinateRequirementHint) {
        return Result.ofOk(languageUnderTest);
    }
}
