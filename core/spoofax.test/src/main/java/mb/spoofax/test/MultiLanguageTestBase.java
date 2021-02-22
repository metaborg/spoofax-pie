package mb.spoofax.test;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.PlatformComponent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collection;

public abstract class MultiLanguageTestBase extends TestBase {
    public @MonotonicNonNull RootResourceServiceComponent resourceServiceComponent;
    public @MonotonicNonNull PlatformComponent platformComponent;

    public @MonotonicNonNull RootPieComponent pieComponent;
    public @MonotonicNonNull Pie pie;


    public MultiLanguageTestBase(LoggerComponent loggerComponent) {
        super(loggerComponent);
    }

    public MultiLanguageTestBase() {
        this(DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build());
    }


    protected abstract Collection<ResourceRegistriesProvider> createResourcesComponents();

    protected abstract Collection<LanguageComponent> createComponents();


    @BeforeEach void beforeAll() {
        final Collection<ResourceRegistriesProvider> resourcesComponents = createResourcesComponents();
        final RootResourceServiceModule resourceServiceModule = new RootResourceServiceModule()
            .addRegistry(textResourceRegistry);
        for(final ResourceRegistriesProvider resourceRegistriesProvider : resourcesComponents) {
            resourceServiceModule.addRegistriesFrom(resourceRegistriesProvider);
        }
        resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(resourceServiceModule)
            .loggerComponent(loggerComponent)
            .build();
        platformComponent = DaggerPlatformComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();

        final Collection<LanguageComponent> components = createComponents();
        final RootPieModule pieModule = new RootPieModule(PieBuilderImpl::new);
        for(final LanguageComponent component : components) {
            pieModule.addTaskDefsFrom(component);
        }
        pieComponent = DaggerRootPieComponent.builder()
            .rootPieModule(pieModule)
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
        platformComponent.close();
        resourceServiceComponent.close();
    }
}
