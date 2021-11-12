package mb.str.util;

import mb.common.result.Result;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.Supplier;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.pie.task.java.CompileJava;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.spoofax.test.SingleLanguageTestBase;
import mb.str.DaggerStrategoResourcesComponent;
import mb.str.StrategoResourcesComponent;
import mb.str.task.StrategoCheck;
import mb.str.task.StrategoCompileToJava;
import mb.str.task.spoofax.StrategoParseWrapper;
import mb.strategolib.DaggerStrategoLibComponent;
import mb.strategolib.DaggerStrategoLibResourcesComponent;
import mb.strategolib.StrategoLibComponent;
import mb.strategolib.StrategoLibResourcesComponent;
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
        this.strategoLibResourcesComponent = DaggerStrategoLibResourcesComponent.builder()
            .build();
        this.strategoLibComponent = DaggerStrategoLibComponent.builder()
            .loggerComponent(loggerComponent)
            .strategoLibResourcesComponent(strategoLibResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();
    }


    public final StrategoParseWrapper parse = component.getStrategoParseWrapper();
    public final StrategoCompileToJava compile = component.getStrategoCompileToJava();
    public final StrategoCheck check = component.getStrategoCheck();
    public final CompileJava compileJava = component.getCompileJava();
    public final ArchiveToJar archiveToJar = component.getArchiveToJar();
    public final UnarchiveFromJar unarchiveFromJar = component.getUnarchiveFromJar();

    public final StrategoLibResourcesComponent strategoLibResourcesComponent;
    public final StrategoLibComponent strategoLibComponent;


    public Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.inputBuilder().withFile(resourceKey).buildAstSupplier();
    }

    public Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }
}
