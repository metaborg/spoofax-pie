package mb.spt.dynamicloading;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguage;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageRegistry;
import mb.spoofax.lwb.dynamicloading.DynamicLoad;
import mb.spt.model.LanguageUnderTest;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTestImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

public class DynamicLanguageUnderTestProvider implements LanguageUnderTestProvider {
    private final DynamicLanguageRegistry dynamicLanguageRegistry;
    private final DynamicLoad dynamicLoad;
    private final Function<ResourcePath, CompileLanguage.Args> compileLanguageArgsFunction;

    public DynamicLanguageUnderTestProvider(
        DynamicLanguageRegistry dynamicLanguageRegistry,
        DynamicLoad dynamicLoad,
        Function<ResourcePath, CompileLanguage.Args> compileLanguageArgsFunction
    ) {
        this.dynamicLanguageRegistry = dynamicLanguageRegistry;
        this.dynamicLoad = dynamicLoad;
        this.compileLanguageArgsFunction = compileLanguageArgsFunction;
    }

    @Override
    public Result<LanguageUnderTest, DynamicLanguageUnderTestProviderException> provide(
        ExecContext context,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        @Nullable String languageIdHint
    ) {
        if(rootDirectoryHint != null) {
            final CompileLanguage.Args args = compileLanguageArgsFunction.apply(rootDirectoryHint);
            final Result<DynamicLanguage, ?> result = context.require(dynamicLoad, args).getValue();
            return result
                .mapErr(e -> new DynamicLanguageUnderTestProviderException(
                    "Could not provide dynamic language under test for SPT file '" + file + "'" + "; compiling and dynamically loading at '" + rootDirectoryHint + "' failed", e)
                )
                .map(this::toLanguageUnderTest);
        } else if(languageIdHint != null) {
            final @Nullable DynamicLanguage dynamicLanguage = dynamicLanguageRegistry.getLanguageForId(languageIdHint);
            if(dynamicLanguage != null) {
                return Result.ofOk(toLanguageUnderTest(dynamicLanguage));
            } else {
                return Result.ofErr(new DynamicLanguageUnderTestProviderException(
                    "Could not provide dynamic language under test for SPT file '" + file + "'" + "; language with id '" + languageIdHint + "' was not found"
                ));
            }
        } else {
            return Result.ofErr(new DynamicLanguageUnderTestProviderException(
                "Could not provide dynamic language under test for SPT file '" + file + "'" + "; no root directory nor language identifier was given"
            ));
        }
    }

    private LanguageUnderTest toLanguageUnderTest(DynamicLanguage language) {
        return new LanguageUnderTestImpl(language.getResourceServiceComponent(), language.getLanguageComponent(), language.getPieComponent());
    }
}
