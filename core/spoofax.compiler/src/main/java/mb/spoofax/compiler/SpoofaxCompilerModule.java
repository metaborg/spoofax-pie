package mb.spoofax.compiler;

import dagger.Module;
import dagger.Provides;
import mb.spoofax.compiler.util.TemplateCompiler;

import javax.inject.Singleton;

@Module
public class SpoofaxCompilerModule {
    private final TemplateCompiler templateCompiler;

    public SpoofaxCompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }


    @Provides @Singleton public TemplateCompiler provideTemplateCompiler() { return templateCompiler; }
}
