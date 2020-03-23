package mb.sdf3.spoofax.util;

import dagger.Component;
import mb.sdf3.spoofax.Sdf3Component;
import mb.sdf3.spoofax.Sdf3Module;
import mb.sdf3.spoofax.task.Sdf3Parse;
import mb.sdf3.spoofax.task.Sdf3ToCompletionColorer;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;

@LanguageScope @Component(modules = {Sdf3Module.class}, dependencies = PlatformComponent.class)
public interface Sdf3TestComponent extends Sdf3Component {
    Sdf3Parse getParse();

    Sdf3ToCompletionColorer getToCompletionColorer();
}
