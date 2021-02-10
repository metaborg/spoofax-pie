package mb.spoofax.compiler.spoofax3.dagger;

import dagger.Component;
import mb.esv.EsvComponent;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2ResourcesComponent;
import mb.libstatix.LibStatixComponent;
import mb.libstatix.LibStatixResourcesComponent;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.sdf3.Sdf3Component;
import mb.spoofax.compiler.spoofax3.language.Spoofax3LanguageProjectCompiler;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.statix.StatixComponent;
import mb.str.StrategoComponent;

@Spoofax3CompilerScope
@Component(
    modules = {Spoofax3CompilerModule.class},
    dependencies = {
        ResourceServiceComponent.class,
        PlatformComponent.class,

        Sdf3Component.class,
        StrategoComponent.class,
        EsvComponent.class,
        StatixComponent.class,
        LibSpoofax2Component.class,
        LibSpoofax2ResourcesComponent.class, // Required because we inject the definition directory.
        LibStatixComponent.class,
        LibStatixResourcesComponent.class // Required because we inject the definition directory.
    }
)
public interface Spoofax3CompilerComponent {
    @Spoofax3CompilerQualifier Pie getPie();

    Spoofax3LanguageProjectCompiler getSpoofax3LanguageProjectCompiler();
}
