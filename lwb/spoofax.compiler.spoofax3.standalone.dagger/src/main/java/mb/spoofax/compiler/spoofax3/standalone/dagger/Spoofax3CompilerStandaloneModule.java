package mb.spoofax.compiler.spoofax3.standalone.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.task.java.CompileJava;
import mb.spoofax.compiler.spoofax3.standalone.CompileToJavaClassFiles;

import java.util.HashSet;
import java.util.Set;

@Module
public class Spoofax3CompilerStandaloneModule {
    @Provides @Spoofax3CompilerStandaloneScope
    static CompileJava provideCompileJava() {
        return new CompileJava();
    }

    @Provides @Spoofax3CompilerStandaloneQualifier @Spoofax3CompilerStandaloneScope @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefsSet(
        CompileJava compileJava,
        CompileToJavaClassFiles compileToJavaClassFiles
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileJava);
        taskDefs.add(compileToJavaClassFiles);
        return taskDefs;
    }
}
