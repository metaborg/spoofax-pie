package mb.spoofax.compiler.spoofax3.standalone.dagger;

import dagger.Component;
import mb.pie.api.Pie;
import mb.resource.ResourceService;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;

@Spoofax3CompilerStandaloneScope
@Component(
    modules = {Spoofax3CompilerStandaloneModule.class},
    dependencies = {
        SpoofaxCompilerComponent.class,
        Spoofax3CompilerComponent.class,
    }
)
public interface Spoofax3CompilerStandaloneComponent {
    ResourceService getResourceService();

    Pie getPie();

    CompileToJavaClassFiles getCompileToJavaClassFiles();
}
