package mb.spoofax.test;

import mb.log.dagger.LoggerComponent;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.junit.jupiter.api.AfterEach;

import java.util.function.Supplier;

public class SingleLanguageTestBase<R extends ResourceRegistriesProvider, L extends LanguageComponent> extends TestBase {
    public final R resourcesComponent;
    public final RootResourceServiceComponent resourceServiceComponent;

    public final PlatformComponent platformComponent;
    public final L component;

    public final RootPieComponent pieComponent;
    public final Pie pie;

    @FunctionalInterface
    public interface ComponentFactory<R extends ResourceRegistriesProvider, L extends LanguageComponent> {
        L create(
            LoggerComponent loggerComponent,
            R resourcesComponent,
            ResourceServiceComponent resourceServiceComponent,
            PlatformComponent platformComponent
        );
    }

    public SingleLanguageTestBase(
        LoggerComponent loggerComponent,
        Supplier<R> resourcesComponentSupplier,
        ComponentFactory<R, L> languageComponentFactory
    ) {
        super(loggerComponent);
        resourcesComponent = resourcesComponentSupplier.get();
        final RootResourceServiceModule resourceServiceModule = new RootResourceServiceModule()
            .addRegistry(textResourceRegistry)
            .addRegistriesFrom(resourcesComponent);
        resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(resourceServiceModule)
            .loggerComponent(loggerComponent)
            .build();
        platformComponent = DaggerPlatformComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        component = languageComponentFactory.create(
            loggerComponent,
            resourcesComponent,
            resourceServiceComponent,
            platformComponent
        );
        pieComponent = DaggerRootPieComponent.builder()
            .rootPieModule(new RootPieModule(PieBuilderImpl::new, component))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();
        this.pie = pieComponent.getPie();
    }

    public MixedSession newSession() {
        return pie.newSession();
    }


    @AfterEach void closeComponents() throws Exception {
        pieComponent.close();
        component.close();
        platformComponent.close();
        resourceServiceComponent.close();
    }
}
