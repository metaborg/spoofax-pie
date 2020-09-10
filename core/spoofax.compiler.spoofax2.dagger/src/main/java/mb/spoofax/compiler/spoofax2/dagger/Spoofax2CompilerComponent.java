package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Component;
import mb.spoofax.compiler.dagger.*;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;

import javax.inject.Singleton;

@Singleton @Component(modules = {SpoofaxCompilerModule.class, Spoofax2CompilerModule.class})
public interface Spoofax2CompilerComponent extends SpoofaxCompilerComponent {
    Spoofax2LanguageProjectCompiler getSpoofax2LanguageProjectCompiler();
}
