package mb.tiger.spoofax.util;

import dagger.Component;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;
import mb.tiger.spoofax.command.TigerShowScopeGraphCommand;
import mb.tiger.spoofax.task.TigerShowScopeGraph;

@LanguageScope @Component(modules = {TigerModule.class}, dependencies = PlatformComponent.class)
public interface TigerTestComponent extends TigerComponent {
    TigerShowScopeGraph getShowScopeGraph();

    TigerShowScopeGraphCommand getShowScopeGraphCommand();
}
