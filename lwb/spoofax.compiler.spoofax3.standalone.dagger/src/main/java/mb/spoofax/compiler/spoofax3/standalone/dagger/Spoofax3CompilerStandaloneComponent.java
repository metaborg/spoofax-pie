package mb.spoofax.compiler.spoofax3.standalone.dagger;

import dagger.Component;
import mb.pie.api.Pie;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceComponent;

@Spoofax3CompilerStandaloneScope
@Component(
    modules = {Spoofax3CompilerStandaloneModule.class},
    dependencies = {
        ResourceServiceComponent.class,
        PlatformComponent.class,
        SpoofaxCompilerComponent.class,
        Spoofax3CompilerComponent.class,
    }
)
public interface Spoofax3CompilerStandaloneComponent {
    Pie getPie();

    CompileToJavaClassFiles getCompileToJavaClassFiles();
}
