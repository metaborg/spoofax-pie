package mb.str;

import mb.common.result.Result;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.Supplier;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.spoofax.test.SingleLanguageTestBase;
import mb.str.task.StrategoAnalyze;
import mb.str.task.StrategoCompileToJava;
import mb.str.task.StrategoParse;
import org.spoofax.interpreter.terms.IStrategoTerm;

class TestBase extends SingleLanguageTestBase<StrategoResourcesComponent, StrategoComponent> {
    protected TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            DaggerStrategoResourcesComponent::create,
            (loggerComponent, resourcesComponent, resourceServiceComponent, platformComponent) -> DaggerStrategoComponent.builder()
                .loggerComponent(loggerComponent)
                .strategoResourcesComponent(resourcesComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent)
                .build()
        );
    }


    final StrategoParse parse = component.getStrategoParse();
    final StrategoCompileToJava compile = component.getStrategoCompileToJava();
    final StrategoAnalyze analyze = component.getStrategoAnalyze();
    final CompileJava compileJava = component.getCompileJava();
    final ArchiveToJar archiveToJar = component.getArchiveToJar();


    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.createAstSupplier(resourceKey);
    }

    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }
}
