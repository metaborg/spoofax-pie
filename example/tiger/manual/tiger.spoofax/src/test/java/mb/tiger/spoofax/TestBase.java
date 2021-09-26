package mb.tiger.spoofax;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.spoofax.test.SingleLanguageTestBase;
import mb.tego.strategies.DaggerTegoComponent;

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
                .tegoComponent(DaggerTegoComponent.builder()
                    .loggerComponent(loggerComponent)
                    .build())
                .build()
        );
    }
}
