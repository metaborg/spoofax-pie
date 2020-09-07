package mb.spoofax.compiler.dagger;

import dagger.Component;
import mb.pie.api.TaskDef;
import mb.spoofax.compiler.spoofaxcore.AdapterProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.CliProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.EclipseProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.IntellijProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.LanguageProjectCompiler;
import mb.spoofax.compiler.spoofaxcore.RootProjectCompiler;

import javax.inject.Singleton;
import java.util.Set;

@Singleton @Component(modules = SpoofaxCompilerModule.class)
public interface SpoofaxCompilerComponent {
    LanguageProjectCompiler getLanguageProjectCompiler();

    AdapterProjectCompiler getAdapterProjectCompiler();


    RootProjectCompiler getRootProjectCompiler();

    CliProjectCompiler getCliProjectCompiler();

    EclipseExternaldepsProjectCompiler getEclipseExternaldepsProjectCompiler();

    EclipseProjectCompiler getEclipseProjectCompiler();

    IntellijProjectCompiler getIntellijProjectCompiler();


    Set<TaskDef<?, ?>> getTaskDefs();
}
