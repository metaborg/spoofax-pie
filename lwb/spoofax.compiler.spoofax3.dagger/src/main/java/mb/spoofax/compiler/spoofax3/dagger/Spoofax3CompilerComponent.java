package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Component;
import mb.esv.EsvComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libstatix.LibStatixComponent;
import mb.sdf3.Sdf3Component;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;

@Spoofax3CompilerScope
@Component(
    modules = {Spoofax3CompilerModule.class},
    dependencies = {
        Sdf3Component.class,
        StrategoComponent.class,
        EsvComponent.class,
        StatixComponent.class,
        LibSpoofax2Component.class,
        LibStatixComponent.class
    }
)
public interface Spoofax3CompilerComponent {
    Spoofax3LanguageProjectCompiler getSpoofax3LanguageProjectCompiler();
}
