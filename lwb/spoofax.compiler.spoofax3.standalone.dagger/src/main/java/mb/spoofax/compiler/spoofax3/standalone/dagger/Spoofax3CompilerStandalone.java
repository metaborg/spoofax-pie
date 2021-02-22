package mb.spoofax.compiler.spoofax3.standalone.dagger;

import mb.pie.dagger.DaggerPieComponent;
import mb.pie.dagger.PieComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3Compiler;

public class Spoofax3CompilerStandalone implements AutoCloseable {
    public final Spoofax3Compiler spoofax3Compiler;
    public final Spoofax3CompilerStandaloneComponent component;
    public final PieComponent pieComponent;

    public Spoofax3CompilerStandalone(Spoofax3Compiler spoofax3Compiler) {
        this.spoofax3Compiler = spoofax3Compiler;
        component = DaggerSpoofax3CompilerStandaloneComponent.builder()
            .loggerComponent(spoofax3Compiler.loggerComponent)
            .resourceServiceComponent(spoofax3Compiler.resourceServiceComponent)
            .spoofaxCompilerComponent(spoofax3Compiler.spoofaxCompilerComponent)
            .spoofax3CompilerComponent(spoofax3Compiler.component)
            .build();
        pieComponent = DaggerPieComponent.builder()
            // TODO: check if this copies all task definitions from dependencies as well?
            .pieModule(spoofax3Compiler.pieComponent.createChildModule(component))
            .loggerComponent(spoofax3Compiler.loggerComponent)
            .resourceServiceComponent(spoofax3Compiler.resourceServiceComponent)
            .build();
    }

    @Override public void close() {
        pieComponent.close();
        spoofax3Compiler.close();
    }
}
