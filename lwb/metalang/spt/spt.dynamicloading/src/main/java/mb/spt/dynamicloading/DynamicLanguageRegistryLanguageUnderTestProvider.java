package mb.spt.dynamicloading;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spt.lut.LanguageUnderTestProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DynamicLanguageRegistryLanguageUnderTestProvider implements LanguageUnderTestProvider {
    private final DynamicLanguageRegistry dynamicLanguageRegistry;

    public DynamicLanguageRegistryLanguageUnderTestProvider(DynamicLanguageRegistry dynamicLanguageRegistry) {
        this.dynamicLanguageRegistry = dynamicLanguageRegistry;
    }

    @Override
    public Result<LanguageComponent, DynamicLanguageUnderTestProviderException> provide(ExecContext context, ResourceKey file, @Nullable ResourcePath rootDirectoryHint, @Nullable String languageIdHint) {
        if(languageIdHint != null) {
            final @Nullable DynamicLanguage dynamicLanguage = dynamicLanguageRegistry.getLanguageForId(languageIdHint);
            if(dynamicLanguage != null) {
                return Result.ofOk(dynamicLanguage.getLanguageComponent());
            }
        }
        if(rootDirectoryHint != null) {
            final @Nullable DynamicLanguage dynamicLanguage = dynamicLanguageRegistry.getLanguageForRootDirectory(rootDirectoryHint);
            if(dynamicLanguage != null) {
                return Result.ofOk(dynamicLanguage.getLanguageComponent());
            }
        }
        return Result.ofErr(new DynamicLanguageUnderTestProviderException(
            "Could not provide language under test from dynamic language registry for SPT file '" + file + "'" +
                (rootDirectoryHint != null ? (" with root directory '" + rootDirectoryHint) + "'" : "") +
                (languageIdHint != null ? (" with language identifier '" + languageIdHint + "'") : "") +
                "; no matching language was found in the dynamic language registry"
        ));
    }
}
