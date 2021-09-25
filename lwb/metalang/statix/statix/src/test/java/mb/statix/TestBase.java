package mb.statix;

import mb.common.result.Result;
import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.Supplier;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.spoofax.test.SingleLanguageTestBase;
import mb.statix.task.StatixCompileModule;
import mb.statix.task.spoofax.StatixParseWrapper;
import org.spoofax.interpreter.terms.IStrategoTerm;

class TestBase extends SingleLanguageTestBase<StatixResourcesComponent, StatixComponent> {
    protected TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            DaggerStatixResourcesComponent::create,
            (loggerComponent, resourcesComponent, resourceServiceComponent, platformComponent) -> DaggerStatixComponent.builder()
                .loggerComponent(loggerComponent)
                .statixResourcesComponent(resourcesComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent)
                .build()
        );
    }


    final StatixParseWrapper parse = component.getStatixParseWrapper();
    final StatixCompileModule compileModule = component.getStatixCompileModule();


    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(ResourceKey resourceKey) {
        return parse.inputBuilder().withFile(resourceKey).buildAstSupplier();
    }

    Supplier<? extends Result<IStrategoTerm, ?>> parsedAstSupplier(Resource resource) {
        return parsedAstSupplier(resource.getKey());
    }
}
