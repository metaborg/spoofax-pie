package mb.cfg;

import dagger.Module;
import dagger.Provides;

/**
 * Module for providing a {@link CompileLanguageDefinitionInputCustomizer} implementation. Defaults to a {@link
 * NoopCompileLanguageDefinitionInputCustomizer} implementation. To customize the implementation,  pass an instance of this module
 * when building the {@link CfgComponent} via {@link DaggerCfgComponent.Builder#cfgCustomizerModule(CfgCustomizerModule)}.
 */
@Module
public class CfgCustomizerModule {
    private final CompileLanguageDefinitionInputCustomizer customizer;

    public CfgCustomizerModule(CompileLanguageDefinitionInputCustomizer customizer) {
        this.customizer = customizer;
    }

    public CfgCustomizerModule() {
        this(new NoopCompileLanguageDefinitionInputCustomizer());
    }

    @Provides @CfgScope
    CompileLanguageDefinitionInputCustomizer provideCustomizer() {
        return customizer;
    }
}
