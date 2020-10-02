package mb.str.spoofax;

import dagger.Module;
import dagger.Provides;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;

@Module
public class JavaTasksModule {
    @Provides @StrategoScope
    public static CompileJava provideCompileJava() {
        return new CompileJava();
    }

    @Provides @StrategoScope
    public static ArchiveToJar provideArchiveToJar() {
        return new ArchiveToJar();
    }
}
