package mb.spoofax.compiler.spoofax3.standalone.dagger;

import mb.esv.DaggerEsvComponent;
import mb.libspoofax2.DaggerLibSpoofax2Component;
import mb.libstatix.DaggerLibStatixComponent;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.sdf3.DaggerSdf3Component;
import mb.spoofax.compiler.dagger.DaggerSpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerModule;
import mb.spoofax.compiler.spoofax3.dagger.DaggerSpoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerModule;
import mb.spoofax.compiler.util.TemplateCompiler;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.DaggerStatixComponent;
import mb.str.DaggerStrategoComponent;

import java.nio.charset.StandardCharsets;

public class Spoofax3CompilerStandalone {
    public final TemplateCompiler templateCompiler;
    public final SpoofaxCompilerComponent spoofaxCompilerComponent;
    public final Spoofax3CompilerComponent spoofax3CompilerComponent;
    public final Spoofax3CompilerStandaloneComponent component;

    public Spoofax3CompilerStandalone(PlatformComponent platformComponent) {
        templateCompiler =
            new TemplateCompiler(StandardCharsets.UTF_8);
        spoofax3CompilerComponent = DaggerSpoofax3CompilerComponent.builder()
            .spoofax3CompilerModule(new Spoofax3CompilerModule(templateCompiler))
            .platformComponent(platformComponent)
            .sdf3Component(DaggerSdf3Component.builder().platformComponent(platformComponent).build())
            .strategoComponent(DaggerStrategoComponent.builder().platformComponent(platformComponent).build())
            .esvComponent(DaggerEsvComponent.builder().platformComponent(platformComponent).build())
            .statixComponent(DaggerStatixComponent.builder().platformComponent(platformComponent).build())
            .libSpoofax2Component(DaggerLibSpoofax2Component.builder().platformComponent(platformComponent).build())
            .libStatixComponent(DaggerLibStatixComponent.builder().platformComponent(platformComponent).build())
            .build();
        spoofaxCompilerComponent = DaggerSpoofaxCompilerComponent.builder()
            .spoofaxCompilerModule(new SpoofaxCompilerModule(templateCompiler, PieBuilderImpl::new))
            .build();
        component = DaggerSpoofax3CompilerStandaloneComponent.builder()
            .spoofaxCompilerComponent(spoofaxCompilerComponent)
            .spoofax3CompilerComponent(spoofax3CompilerComponent)
            .build();
    }
}
