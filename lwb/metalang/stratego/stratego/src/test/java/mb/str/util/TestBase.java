package mb.str.util;

import mb.common.result.Result;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.Supplier;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.spoofax.test.SingleLanguageTestBase;
import mb.str.DaggerStrategoResourcesComponent;
import mb.str.StrategoResourcesComponent;
import mb.str.task.StrategoAnalyze;
import mb.str.task.StrategoCompileToJava;
import mb.str.task.StrategoParse;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TestBase extends SingleLanguageTestBase<StrategoResourcesComponent, StrategoTestComponent> {
    protected TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            DaggerStrategoResourcesComponent::create,
            (loggerComponent, resourcesComponent, resourceServiceComponent, platformComponent) -> DaggerStrategoTestComponent.builder()
                .loggerComponent(loggerComponent)
                .strategoResourcesComponent(resourcesComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent)
                .build()
        );
    }


    public final StrategoParse parse = component.getStrategoParse();
    public final StrategoCompileToJava compile = component.getStrategoCompileToJava();
    public final StrategoAnalyze analyze = component.getStrategoAnalyze();
    public final CompileJava compileJava = component.getCompileJava();
    public final ArchiveToJar archiveToJar = component.getArchiveToJar();


    public Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.createAstSupplier(resourceKey);
    }

    public Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }
}
