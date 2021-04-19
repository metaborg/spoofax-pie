package mb.cfg;

import dagger.Module;
import dagger.Provides;

@Module
public class CfgCustomizerModule {
    private final CompileLanguageInputCustomizer customizer;

    public CfgCustomizerModule(CompileLanguageInputCustomizer customizer) {
        this.customizer = customizer;
    }

    public CfgCustomizerModule() {
        this(new NoopCompileLanguageInputCustomizer());
    }

    @Provides @CfgScope
    CompileLanguageInputCustomizer provideCustomizer() {
        return customizer;
    }
}
