package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Component;
import mb.esv.spoofax.EsvComponent;
import mb.libspoofax2.spoofax.LibSpoofax2Component;
import mb.sdf3.spoofax.Sdf3Component;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.str.spoofax.StrategoComponent;

@Spoofax3CompilerScope
@Component(
    modules = {Spoofax3CompilerModule.class},
    dependencies = {Sdf3Component.class, StrategoComponent.class, EsvComponent.class, LibSpoofax2Component.class}
)
public interface Spoofax3CompilerComponent {
    Spoofax3LanguageProjectCompiler getSpoofax3LanguageProjectCompiler();
}
