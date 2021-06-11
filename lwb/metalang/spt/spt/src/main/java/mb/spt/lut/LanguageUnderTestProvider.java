package mb.spt.lut;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface LanguageUnderTestProvider {
    Result<LanguageComponent, ?> provide(
        ExecContext context,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        @Nullable String languageIdHint
    );
}
