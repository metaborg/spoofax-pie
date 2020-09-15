package mb.spoofax.compiler.spoofax2.dagger;

import dagger.Component;
import mb.spoofax.compiler.spoofax2.language.Spoofax2LanguageProjectCompiler;

import javax.inject.Singleton;

@Singleton @Component(modules = {Spoofax2CompilerModule.class})
public interface Spoofax2CompilerComponent {
    Spoofax2LanguageProjectCompiler getSpoofax2LanguageProjectCompiler();
}
