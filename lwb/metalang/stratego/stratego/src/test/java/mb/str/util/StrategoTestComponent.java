package mb.str.util;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.task.archive.ArchiveToJar;
import mb.pie.task.java.CompileJava;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.StrategoComponent;
import mb.str.StrategoModule;
import mb.str.StrategoResourcesComponent;
import mb.str.StrategoScope;
import mb.str.config.StrategoConfigModule;
import mb.str.incr.StrategoIncrModule;

@StrategoScope
@Component(
    modules = {
        StrategoModule.class,
        StrategoIncrModule.class,
        StrategoConfigModule.class,
        TestModule.class
    },
    dependencies = {
        LoggerComponent.class,
        StrategoResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface StrategoTestComponent extends StrategoComponent {
    CompileJava getCompileJava();

    ArchiveToJar getArchiveToJar();
}
