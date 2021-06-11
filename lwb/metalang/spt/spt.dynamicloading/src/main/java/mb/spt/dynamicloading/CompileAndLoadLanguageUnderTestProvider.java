package mb.spt.dynamicloading;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLoad;
import mb.spt.lut.LanguageUnderTestProvider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

public class CompileAndLoadLanguageUnderTestProvider implements LanguageUnderTestProvider {
    private final DynamicLoad dynamicLoad;
    private final Function<ResourcePath, CompileLanguage.Args> compileLanguageArgsFunction;

    public CompileAndLoadLanguageUnderTestProvider(
        DynamicLoad dynamicLoad,
        Function<ResourcePath, CompileLanguage.Args> compileLanguageArgsFunction
    ) {
        this.dynamicLoad = dynamicLoad;
        this.compileLanguageArgsFunction = compileLanguageArgsFunction;
    }

    @Override
    public Result<LanguageComponent, DynamicLanguageUnderTestProviderException> provide(
        ExecContext context,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        @Nullable String languageIdHint
    ) {
        if(rootDirectoryHint == null) {
            return Result.ofErr(new DynamicLanguageUnderTestProviderException(
                "Could not provide language under test from compiling and dynamically loading for SPT file '" + file + "'" + "; no root directory was given"
            ));
        }
        final CompileLanguage.Args args = compileLanguageArgsFunction.apply(rootDirectoryHint);
        final Result<DynamicLanguage, ?> result = context.require(dynamicLoad, args).getValue();
        return result
            .mapErr(e -> new DynamicLanguageUnderTestProviderException(
                "Could not provide language under test from compiling and dynamically loading for SPT file '" + file + "'" + "; compiling and dynamically loading failed", e)
            )
            .map(DynamicLanguage::getLanguageComponent);
    }
}
