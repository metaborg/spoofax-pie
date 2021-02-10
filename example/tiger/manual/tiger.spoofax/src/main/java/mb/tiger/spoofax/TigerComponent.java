package mb.tiger.spoofax;

import dagger.Component;
import mb.pie.api.Pie;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerShowScopeGraph;

/**
 * A {@link LanguageComponent} that contributes Tiger task definitions. All objects are provided by a {@link
 * TigerModule}, which may inject objects from a {@link PlatformComponent}. Task definitions are overridden to concrete
 * types so that Dagger can use constructor injection on those types.
 */
@TigerScope
@Component(
    modules = TigerModule.class,
    dependencies = {
        TigerResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface TigerComponent extends LanguageComponent {
    @Override TigerInstance getLanguageInstance();

    @Override @TigerQualifier Pie getPie();


    TigerShowScopeGraph getShowScopeGraph();

    TigerShowScopeGraphCommand getShowScopeGraphCommand();
}
