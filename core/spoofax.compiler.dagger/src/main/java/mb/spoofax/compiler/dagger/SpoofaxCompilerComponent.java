package mb.spoofax.compiler.dagger;

import dagger.Component;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.compiler.adapter.AdapterProjectCompiler;
import mb.spoofax.compiler.language.LanguageProjectCompiler;
import mb.spoofax.compiler.platform.CliProjectCompiler;
import mb.spoofax.compiler.platform.EclipseProjectCompiler;
import mb.spoofax.compiler.platform.IntellijProjectCompiler;

@SpoofaxCompilerScope @Component(modules = SpoofaxCompilerModule.class)
public interface SpoofaxCompilerComponent {
    ResourceService getResourceService();

    Pie getPie();


    LanguageProjectCompiler getLanguageProjectCompiler();

    AdapterProjectCompiler getAdapterProjectCompiler();


    CliProjectCompiler getCliProjectCompiler();

    EclipseProjectCompiler getEclipseProjectCompiler();

    IntellijProjectCompiler getIntellijProjectCompiler();
}
