package mb.str.util;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;
import mb.str.StrategoQualifier;
import mb.str.StrategoScope;

import java.util.HashSet;
import java.util.Set;

@Module
public class TestModule {
    @Provides @StrategoScope
    public static CompileJava provideCompileJava() {
        return new CompileJava();
    }

    @Provides @StrategoScope
    public static ArchiveToJar provideArchiveToJar() {
        return new ArchiveToJar();
    }

    @Provides @StrategoScope @StrategoQualifier @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefs(
        CompileJava compileJava,
        ArchiveToJar archiveToJar
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(compileJava);
        taskDefs.add(archiveToJar);
        return taskDefs;
    }
}
