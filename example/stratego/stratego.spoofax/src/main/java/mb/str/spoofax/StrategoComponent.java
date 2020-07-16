package mb.str.spoofax;

import dagger.Component;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.spoofax.incr.StrategoIncrModule;
import mb.str.spoofax.task.StrategoParse;
import mb.stratego.build.strincr.Analysis;

@LanguageScope
@Component(modules = {StrategoModule.class, StrategoIncrModule.class}, dependencies = PlatformComponent.class)
public interface StrategoComponent extends GeneratedStrategoComponent {
    StrategoParse getStrategoParse();

    @LanguageScope Analysis getAnalysis();
}
