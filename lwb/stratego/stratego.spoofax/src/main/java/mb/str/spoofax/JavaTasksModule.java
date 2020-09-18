package mb.str.spoofax;

import dagger.Module;
import dagger.Provides;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.CreateJar;

@Module
public class JavaTasksModule {
    @Provides @StrategoScope
    public static CompileJava provideCompileJava() {
        return new CompileJava();
    }

    @Provides @StrategoScope
    public static CreateJar provideCreateJar() {
        return new CreateJar();
    }
}
