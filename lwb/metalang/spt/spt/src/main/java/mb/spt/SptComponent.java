package mb.spt;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spt.fromterm.ExpectationFromTermsModule;
import mb.spt.lut.LanguageUnderTestProviderWrapper;

@SptScope
@Component(
    modules = {
        SptModule.class,
        ExpectationFromTermsModule.class,
    },
    dependencies = {
        LoggerComponent.class,
        SptResourcesComponent.class,
        ResourceServiceComponent.class,
        PlatformComponent.class,
    }
)
public interface SptComponent extends BaseSptComponent {
    LanguageUnderTestProviderWrapper getLanguageUnderTestProviderWrapper();
}
