package mb.spoofax.lwb.eclipse.dynamicloading;

import dagger.Component;
import mb.cfg.CfgComponent;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.lwb.compiler.dagger.Spoofax3CompilerComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingComponent;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingPieModule;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingScope;

@DynamicLoadingScope
@Component(
    modules = {
        EclipseDynamicLoadingModule.class,
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
public interface EclipseDynamicLoadingComponent extends DynamicLoadingComponent {
}
