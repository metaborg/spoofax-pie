package mb.spoofax.compiler.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.util.TemplateCompiler;

import java.util.HashSet;
import java.util.Set;

@Module
public class SpoofaxCompilerModule {
    private final TemplateCompiler templateCompiler;

    public SpoofaxCompilerModule(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }

    @Provides @SpoofaxCompilerScope
    TemplateCompiler provideTemplateCompiler() {
        return templateCompiler;
    }

    @Provides @SpoofaxCompilerQualifier @SpoofaxCompilerScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        LanguageProjectCompiler languageProjectCompiler,
        AdapterProjectCompiler adapterProjectCompiler,
        CliProjectCompiler cliProjectCompiler,
        EclipseProjectCompiler eclipseProjectCompiler,
        IntellijProjectCompiler intellijProjectCompiler
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(languageProjectCompiler);
        taskDefs.add(adapterProjectCompiler);
        taskDefs.add(cliProjectCompiler);
        taskDefs.add(eclipseProjectCompiler);
        taskDefs.add(intellijProjectCompiler);
        return taskDefs;
    }
}
