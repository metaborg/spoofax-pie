package mb.spoofax.compiler.spoofax3.standalone.dagger;

import mb.esv.DaggerEsvComponent;
import mb.esv.EsvComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libspoofax2.LibSpoofax2Component;
import mb.libstatix.DaggerLibStatixComponent;
import mb.libstatix.LibStatixComponent;
import mb.pie.runtime.PieBuilderImpl;
import mb.sdf3.DaggerSdf3Component;
import mb.sdf3.Sdf3Component;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.spoofax3.dagger.DaggerSpoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.DaggerStatixComponent;
import mb.statix.StatixComponent;
import mb.str.DaggerStrategoComponent;
import mb.str.StrategoComponent;

import java.nio.charset.StandardCharsets;

public class Spoofax3CompilerStandalone {
    public final PlatformComponent platformComponent;
    public final TemplateCompiler templateCompiler;
    public final SpoofaxCompilerComponent spoofaxCompilerComponent;
    public final Spoofax3CompilerComponent spoofax3CompilerComponent;
    public final Spoofax3CompilerStandaloneComponent component;

    public Spoofax3CompilerStandalone(
        PlatformComponent platformComponent,
        Sdf3Component sdf3Component,
        StrategoComponent strategoComponent,
        EsvComponent esvComponent,
        StatixComponent statixComponent,
        LibSpoofax2Component libSpoofax2Component,
        LibStatixComponent libStatixComponent
    ) {
        this.platformComponent = platformComponent;
        templateCompiler =
            new TemplateCompiler(StandardCharsets.UTF_8);
        spoofax3CompilerComponent = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
            .platformComponent(platformComponent)
            .sdf3Component(sdf3Component)
            .strategoComponent(strategoComponent)
            .esvComponent(esvComponent)
            .statixComponent(statixComponent)
            .libSpoofax2Component(libSpoofax2Component)
            .libStatixComponent(libStatixComponent)
            .build();
        spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler, PieBuilderImpl::new))
            .build();
        component = DaggerSpoofax3CompilerStandaloneComponent.builder()
            .spoofaxCompilerComponent(spoofaxCompilerComponent)
            .spoofax3CompilerComponent(spoofax3CompilerComponent)
            .build();
    }

    public Spoofax3CompilerStandalone(PlatformComponent platformComponent) {
        this(
            platformComponent,
            DaggerSdf3Component.builder().platformComponent(platformComponent).build(),
            DaggerStrategoComponent.builder().platformComponent(platformComponent).build(),
            DaggerEsvComponent.builder().platformComponent(platformComponent).build(),
            DaggerStatixComponent.builder().platformComponent(platformComponent).build(),
            DaggerLibSpoofax2Component.builder().platformComponent(platformComponent).build(),
            DaggerLibStatixComponent.builder().platformComponent(platformComponent).build()
        );
    }
}
