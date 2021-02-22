package mb.tiger.spoofax;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerShowScopeGraph;

import java.util.Set;

/**
 * A {@link LanguageComponent} that contributes Tiger task definitions. All objects are provided by a {@link
 * TigerModule}, which may inject objects from a {@link PlatformComponent}. Task definitions are overridden to concrete
 * types so that Dagger can use constructor injection on those types.
 */
@TigerScope
@Component(
    modules = TigerModule.class,
    dependencies = {
        LoggerComponent.class,
        TigerResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface TigerComponent extends LanguageComponent {
    @Override TigerInstance getLanguageInstance();

    @Override @TigerQualifier Set<TaskDef<?, ?>> getTaskDefs();


    TigerShowScopeGraph getShowScopeGraph();

    TigerShowScopeGraphCommand getShowScopeGraphCommand();
}
