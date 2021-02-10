package mb.spoofax.compiler.spoofax3.standalone.dagger;

import mb.spoofax.compiler.spoofax3.dagger.Spoofax3Compiler;

public class Spoofax3CompilerStandalone {
    public final Spoofax3Compiler spoofax3Compiler;
    public final Spoofax3CompilerStandaloneComponent component;

    public Spoofax3CompilerStandalone(Spoofax3Compiler spoofax3Compiler) {
        this.spoofax3Compiler = spoofax3Compiler;
        component = DaggerSpoofax3CompilerStandaloneComponent.builder()
            .resourceServiceComponent(spoofax3Compiler.resourceServiceComponent)
            .platformComponent(spoofax3Compiler.platformComponent)
            .spoofaxCompilerComponent(spoofax3Compiler.spoofaxCompilerComponent)
            .spoofax3CompilerComponent(spoofax3Compiler.component)
            .build();
    }
}
