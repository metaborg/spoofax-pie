package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Component;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;

import javax.inject.Singleton;

@Spoofax2CompilerScope @Component(modules = {Spoofax2CompilerModule.class})
public interface Spoofax2CompilerComponent {
    ResourceService getResourceService();

    Pie getPie();


    Spoofax2LanguageProjectCompiler getSpoofax2LanguageProjectCompiler();
}
