package mb.tiger.spoofax;

import dagger.Component;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.task.TigerShowScopeGraph;

/**
 * A {@link LanguageComponent} that contributes Tiger task definitions. All objects are provided by a {@link
 * TigerModule}, which may inject objects from a {@link PlatformComponent}. Task definitions are overridden to concrete
 * types so that Dagger can use constructor injection on those types.
 */
@TigerScope @Component(modules = TigerModule.class, dependencies = PlatformComponent.class)
public interface TigerComponent extends LanguageComponent {
    @Override TigerInstance getLanguageInstance();

    @Override @TigerQualifier ResourceService getResourceService();

    @Override @TigerQualifier Pie getPie();


    TigerShowScopeGraph getTigerShowScopeGraph();
}
