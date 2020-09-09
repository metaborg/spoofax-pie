package mb.spoofax.compiler.dagger;

import dagger.Component;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseExternaldepsProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;

import javax.inject.Singleton;

@Singleton @Component(modules = SpoofaxCompilerModule.class)
public interface SpoofaxCompilerComponent {
    LanguageProjectCompiler getLanguageProjectCompiler();

    AdapterProjectCompiler getAdapterProjectCompiler();


    CliProjectCompiler getCliProjectCompiler();

    EclipseExternaldepsProjectCompiler getEclipseExternaldepsProjectCompiler();

    EclipseProjectCompiler getEclipseProjectCompiler();

    IntellijProjectCompiler getIntellijProjectCompiler();
}
