package mb.str.spoofax;

import dagger.Component;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.spoofax.task.StrategoParse;

@LanguageScope
@Component(modules = {StrategoModule.class}, dependencies = PlatformComponent.class)
public interface StrategoComponent extends GeneratedStrategoComponent {
    StrategoParse getStrategoParse();
}
