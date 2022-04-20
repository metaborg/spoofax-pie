package mb.sdf3.adapter;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.Supplier;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.DaggerSdf3Component;
import mb.sdf3.DaggerSdf3ResourcesComponent;
import mb.sdf3.Sdf3Component;
import mb.sdf3.Sdf3ResourcesComponent;
import mb.sdf3.task.Sdf3AnalyzeMulti;
import mb.sdf3.task.Sdf3Desugar;
import mb.sdf3.task.Sdf3Parse;
import mb.sdf3.task.debug.MultiAstDesugarFunction;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spoofax.Sdf3GetSourceFilesWrapper;
import mb.spoofax.test.SingleLanguageTestBase;
import org.spoofax.interpreter.terms.IStrategoTerm;

class TestBase extends SingleLanguageTestBase<Sdf3ResourcesComponent, Sdf3Component> {
    protected TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            DaggerSdf3ResourcesComponent::create,
            (loggerComponent, resourcesComponent, resourceServiceComponent, platformComponent) -> DaggerSdf3Component.builder()
                .loggerComponent(loggerComponent)
                .sdf3ResourcesComponent(resourcesComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent)
                .build()
        );
    }

    final Sdf3Parse parse = component.getSdf3Parse();
    final Sdf3GetSourceFilesWrapper getSourceFiles = component.getSdf3GetSourceFilesWrapper();
    final Sdf3Desugar desugar = component.getSdf3Desugar();
    final Sdf3AnalyzeMulti analyze = component.getSdf3AnalyzeMulti();


    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.inputBuilder().withFile(resourceKey).buildAstSupplier();
    }

    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }


    Supplier<Result<IStrategoTerm, ?>> desugarSupplier(ResourceKey resourceKey) {
        return desugar.createSupplier(parsedAstSupplier(resourceKey));
    }

    Supplier<Result<IStrategoTerm, ?>> desugarSupplier(Resource resource) {
        return desugar.createSupplier(parsedAstSupplier(resource));
    }


    Supplier<? extends Result<Sdf3AnalyzeMulti.SingleFileOutput, ?>> singleFileAnalysisResultSupplier(ResourcePath project, ResourceKey file) {
        return analyze.createSingleFileOutputSupplier(new Sdf3AnalyzeMulti.Input(project, parse.createRecoverableMultiAstSupplierFunction(getSourceFiles.createFunction()).mapOutput(new MultiAstDesugarFunction(desugar.createFunction()))), file);
    }

    Supplier<? extends Result<Sdf3AnalyzeMulti.SingleFileOutput, ?>> singleFileAnalysisResultSupplier(Resource file) {
        return singleFileAnalysisResultSupplier(rootDirectory.getPath(), file.getKey());
    }


    Sdf3SpecConfig specConfig(ResourcePath rootDirectory, ResourcePath mainSourceDirectory, ResourcePath mainFile) {
        return new Sdf3SpecConfig(rootDirectory, mainSourceDirectory, mainFile, ListView.of(), ListView.of(), Sdf3SpecConfig.createDefaultParseTableConfiguration());
    }

    Sdf3SpecConfig specConfig(ResourcePath rootDirectory) {
        return Sdf3SpecConfig.createDefault(rootDirectory);
    }
}
