package mb.tiger.spoofax;

import dagger.Component;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.core.platform.PlatformComponent;

import javax.inject.Singleton;

@Singleton @Component(modules = TigerModule.class, dependencies = PlatformComponent.class)
public interface TigerComponent extends LanguageComponent {
}
