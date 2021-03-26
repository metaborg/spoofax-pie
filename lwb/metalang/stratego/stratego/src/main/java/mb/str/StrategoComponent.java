package mb.str;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.str.incr.StrategoIncrModule;
import mb.str.task.spoofax.StrategoAnalyzeConfigFunctionWrapper;

@StrategoScope
@Component(
    modules = {
        StrategoModule.class,
        StrategoIncrModule.class,
    },
    dependencies = {
        LoggerComponent.class,
        StrategoResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class
    }
)
public interface StrategoComponent extends BaseStrategoComponent {
    StrategoAnalyzeConfigFunctionWrapper getStrategoAnalyzeConfigFunctionWrapper();
}
