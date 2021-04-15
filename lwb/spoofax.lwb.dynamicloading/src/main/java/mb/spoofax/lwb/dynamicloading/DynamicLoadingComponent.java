package mb.spoofax.lwb.dynamicloading;

import dagger.Component;
import mb.cfg.CfgComponent;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.TaskDef;
import mb.pie.dagger.TaskDefsProvider;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.dagger.Spoofax3CompilerComponent;

import java.util.Set;

@DynamicLoadingScope
@Component(
    modules = {
        DynamicLoadingModule.class,
        DynamicLoadingPieModule.class
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
    DynamicLanguageRegistry getDynamicLanguageRegistry();

    DynamicLoad getDynamicLoad();


    @Override @DynamicLoadingQualifier Set<TaskDef<?, ?>> getTaskDefs();

    @Override default void close() {
        getDynamicLanguageRegistry().close();
    }
}
