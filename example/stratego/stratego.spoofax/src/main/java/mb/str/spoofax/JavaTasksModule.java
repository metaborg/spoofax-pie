package mb.str.spoofax;

import dagger.Module;
import dagger.Provides;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.CreateJar;
import mb.spoofax.core.language.LanguageScope;

@Module
public class JavaTasksModule {
    @Provides @LanguageScope
    public static CompileJava provideCompileJava() {
        return new CompileJava();
    }

    @Provides @LanguageScope
    public static CreateJar provideCreateJar() {
        return new CreateJar();
    }
}
