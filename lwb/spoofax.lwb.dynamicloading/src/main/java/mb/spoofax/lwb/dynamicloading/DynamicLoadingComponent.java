package mb.spoofax.lwb.dynamicloading;

import dagger.Component;
import mb.cfg.CfgComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.dagger.Spoofax3CompilerComponent;

import java.io.IOException;
import java.util.Set;

@DynamicLoadingScope
@Component(
    modules = {
        DynamicLoadingModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class,
        CfgComponent.class,
        Spoofax3CompilerComponent.class
    }
)
public interface DynamicLoadingComponent extends TaskDefsProvider, AutoCloseable {
    DynamicLoader getDynamicLoader();


    @Override @DynamicLoadingQualifier Set<TaskDef<?, ?>> getTaskDefs();

    @Override default void close() throws IOException {
        getDynamicLoader().close();
    }
}
