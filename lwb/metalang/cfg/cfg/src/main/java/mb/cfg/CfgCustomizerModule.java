package mb.cfg;

import dagger.Module;
import dagger.Provides;

/**
 * Module for providing a {@link CompileLanguageInputCustomizer} implementation. Defaults to a {@link
 * NoopCompileLanguageInputCustomizer} implementation. To customize the implementation,  pass an instance of this module
 * when building the {@link CfgComponent} via {@link DaggerCfgComponent.Builder#cfgCustomizerModule(CfgCustomizerModule)}.
 */
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
