package mb.spoofax.compiler.spoofax3.standalone.dagger;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.compiler.dagger.SpoofaxCompilerComponent;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerComponent;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;

import java.util.Set;

@Spoofax3CompilerStandaloneScope
@Component(
    modules = {
        Spoofax3CompilerStandaloneModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        SpoofaxCompilerComponent.class,
        Spoofax3CompilerComponent.class,
    }
)
public interface Spoofax3CompilerStandaloneComponent extends TaskDefsProvider {
    CompileToJavaClassFiles getCompileToJavaClassFiles();

    @Override @Spoofax3CompilerStandaloneQualifier Set<TaskDef<?, ?>> getTaskDefs();
}
