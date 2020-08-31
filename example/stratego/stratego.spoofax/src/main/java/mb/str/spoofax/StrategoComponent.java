package mb.str.spoofax;

import dagger.Component;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.CreateJar;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.spoofax.incr.StrategoIncrModule;
import mb.str.spoofax.task.StrategoAnalyze;
import mb.str.spoofax.task.StrategoCompileToJava;
import mb.str.spoofax.task.StrategoParse;

@LanguageScope
@Component(modules = {StrategoModule.class, StrategoIncrModule.class, JavaTasksModule.class}, dependencies = PlatformComponent.class)
public interface StrategoComponent extends GeneratedStrategoComponent {
    StrategoParse getStrategoParse();

    StrategoCompileToJava getStrategoCompile();

    StrategoAnalyze getStrategoAnalyze();


    CompileJava getCompileJava();

    CreateJar getCreateJar();
}
