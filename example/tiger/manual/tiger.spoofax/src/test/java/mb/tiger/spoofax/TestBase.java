package mb.tiger.spoofax;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.spoofax.test.SingleLanguageTestBase;

class TestBase extends SingleLanguageTestBase<TigerResourcesComponent, TigerComponent> {
    protected TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            DaggerTigerResourcesComponent::create,
            (loggerComponent, resourcesComponent, resourceServiceComponent, platformComponent) -> DaggerTigerComponent.builder()
                .loggerComponent(loggerComponent)
                .tigerResourcesComponent(resourcesComponent)
                .resourceServiceComponent(resourceServiceComponent)
                .platformComponent(platformComponent)
                .build()
        );
    }
}
