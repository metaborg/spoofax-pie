package mb.spt.dynamicloading;

import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.SetView;
import mb.pie.api.ExecContext;
import mb.pie.api.SerializableFunction;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.CoordinateRequirement;
import mb.spoofax.core.component.Component;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;
import mb.spoofax.lwb.dynamicloading.DynamicLoad;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;
import mb.spt.lut.LanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTest;
import mb.spt.model.LanguageUnderTestImpl;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

public class DynamicLanguageUnderTestProvider implements LanguageUnderTestProvider {
    private final DynamicComponentManager dynamicComponentManager;
    private final DynamicLoad dynamicLoad;
    private final CompileLanguage compileLanguage;
    private final Function<ResourcePath, CompileLanguage.Args> compileLanguageArgsFunction;

    public DynamicLanguageUnderTestProvider(
        DynamicComponentManager dynamicComponentManager,
        DynamicLoad dynamicLoad,
        CompileLanguage compileLanguage,
        Function<ResourcePath, CompileLanguage.Args> compileLanguageArgsFunction
    ) {
        this.dynamicComponentManager = dynamicComponentManager;
        this.dynamicLoad = dynamicLoad;
        this.compileLanguage = compileLanguage;
        this.compileLanguageArgsFunction = compileLanguageArgsFunction;
    }

    @Override
    public Result<LanguageUnderTest, DynamicLanguageUnderTestProviderException> provide(
        ExecContext context,
        ResourceKey file,
        @Nullable ResourcePath rootDirectoryHint,
        @Nullable CoordinateRequirement languageCoordinateRequirementHint
    ) {
        if(rootDirectoryHint != null) {
            final CompileLanguage.Args args = compileLanguageArgsFunction.apply(rootDirectoryHint);
            final Supplier<Result<DynamicLoad.SupplierOutput, ?>> supplier = compileLanguage.createSupplier(args).map(new DynamicLoadSupplierOutputMapper(args));
            final Result<DynamicComponent, ?> result = context.require(dynamicLoad, supplier).getValue();
            return result
                .mapErr(e -> new DynamicLanguageUnderTestProviderException(
                    "Could not provide dynamic language under test for SPT file '" + file + "'" + "; compiling and dynamically loading at '" + rootDirectoryHint + "' failed", e)
                )
                .map(this::toLanguageUnderTest)
                .flatMap(o -> o.mapOrElse(Result::ofOk, () -> Result.ofErr(new DynamicLanguageUnderTestProviderException("Could not provide dynamic language under test for SPT file '" + file + "'" + "; compiling and dynamically loading at '" + rootDirectoryHint + "' succeeded, but it does not have a language component"))));
        } else if(languageCoordinateRequirementHint != null) {
            final Option<? extends Component> component = dynamicComponentManager.getOneComponent(languageCoordinateRequirementHint);
            return component.mapOrElse(c -> toLanguageUnderTest(c, file, languageCoordinateRequirementHint), () -> Result.ofErr(new DynamicLanguageUnderTestProviderException(
                "Could not provide dynamic language under test for SPT file '" + file + "'" + "; component with coordinate requirement '" + languageCoordinateRequirementHint + "' was not found"
            )));
        } else {
            return Result.ofErr(new DynamicLanguageUnderTestProviderException(
                "Could not provide dynamic language under test for SPT file '" + file + "'" + "; no root directory nor coordinate requirement hints were given"
            ));
        }
    }

    private Option<LanguageUnderTest> toLanguageUnderTest(DynamicComponent component) {
        return component.getLanguageComponent().map(l -> new LanguageUnderTestImpl(component.getResourceServiceComponent(), l, component.getPieComponent()));
    }

    private Result<LanguageUnderTest, DynamicLanguageUnderTestProviderException> toLanguageUnderTest(
        Component component,
        ResourceKey file,
        CoordinateRequirement languageCoordinateRequirementHint
    ) {
        final Option<LanguageComponent> languageComponent = component.getLanguageComponent();
        return Result.ofOptionOrElse(languageComponent, () -> new DynamicLanguageUnderTestProviderException("Could not provide dynamic language under test for SPT file '" + file + "'" + "; language with coordinate requirement '" + languageCoordinateRequirementHint + "' was not found"))
            .map(lc -> new LanguageUnderTestImpl(component.getResourceServiceComponent(), lc, component.getPieComponent()));
    }


    private static class DynamicLoadSupplierOutputMapper implements SerializableFunction<Result<CompileLanguage.Output, CompileLanguageException>, Result<DynamicLoad.SupplierOutput, CompileLanguageException>> {
        private final CompileLanguage.Args args;

        public DynamicLoadSupplierOutputMapper(CompileLanguage.Args args) {this.args = args;}

        @Override
        public Result<DynamicLoad.SupplierOutput, CompileLanguageException> apply(Result<CompileLanguage.Output, CompileLanguageException> result) {
            return result.map(o -> new DynamicLoad.SupplierOutput(args.rootDirectory(), SetView.of(o.javaClassPaths()), o.participantClassQualifiedId()));
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final DynamicLoadSupplierOutputMapper that = (DynamicLoadSupplierOutputMapper)o;
            return args.equals(that.args);
        }

        @Override public int hashCode() {
            return args.hashCode();
        }

        @Override public String toString() {
            return "DynamicLoadSupplierOutputMapper{" +
                "args=" + args +
                '}';
        }
    }
}
