package mb.spoofax.compiler.spoofax3.standalone.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.pie.task.java.CompileJava;
import mb.resource.ResourceService;
import mb.spoofax.compiler.dagger.SpoofaxCompilerQualifier;
import mb.spoofax.compiler.spoofax3.dagger.Spoofax3CompilerQualifier;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;

import java.util.HashSet;
import java.util.Set;

@Module
public abstract class Spoofax3CompilerStandaloneModule {
    @Provides
    @Spoofax3CompilerStandaloneScope
    static CompileJava provideCompileJava() {
        return new CompileJava();
    }

    @Provides
    @Spoofax3CompilerStandaloneScope
    @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        CompileJava compileJava,
        CompileToJavaClassFiles compileToJavaClassFiles
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileJava);
        taskDefs.add(compileToJavaClassFiles);
        return taskDefs;
    }

    @Provides
    @Spoofax3CompilerStandaloneScope
    static Pie providePie(
        ResourceService resourceService,
        @SpoofaxCompilerQualifier Pie spoofaxCompilerPie,
        @Spoofax3CompilerQualifier Pie spoofax3CompilerPie,
        Set<TaskDef<?, ?>> taskDefs
    ) {
        return spoofax3CompilerPie.createChildBuilder(spoofaxCompilerPie)
            .withTaskDefs(new MapTaskDefs(taskDefs))
            .withResourceService(resourceService)
            .build();
    }
}
