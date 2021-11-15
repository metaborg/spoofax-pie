package mb.calc;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.rv32im.DaggerRv32ImComponent;
import mb.rv32im.DaggerRv32ImResourcesComponent;
import mb.rv32im.Rv32ImComponent;
import mb.rv32im.Rv32ImResourcesComponent;
import mb.rv32im.Rv32ImResourcesModule;
import mb.spoofax.test.SingleLanguageTestBase;

class TestBase extends SingleLanguageTestBase<CalcResourcesComponent, CalcComponent> {
    protected TestBase() {
        super(
            DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build(),
            DaggerCalcResourcesComponent::create,
            (loggerComponent, resourcesComponent, resourceServiceComponent, platformComponent) -> {
                final Rv32ImResourcesComponent rv32ImResourcesComponent = DaggerRv32ImResourcesComponent.builder()
                    .rv32ImResourcesModule(new Rv32ImResourcesModule())
                    .build();
                final Rv32ImComponent rv32ImComponent = DaggerRv32ImComponent.builder()
                    .loggerComponent(loggerComponent)
                    .rv32ImResourcesComponent(rv32ImResourcesComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .platformComponent(platformComponent)
                    .build();
                return DaggerCalcComponent.builder()
                    .loggerComponent(loggerComponent)
                    .calcResourcesComponent(resourcesComponent)
                    .resourceServiceComponent(resourceServiceComponent)
                    .platformComponent(platformComponent)
                    .rv32ImComponent(rv32ImComponent)
                    .build();
            }
        );
    }
}
