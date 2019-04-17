package mb.spoofax.eclipse;

import dagger.Component;
import mb.pie.dagger.PieModule;
import mb.spoofax.core.platform.FSRegistryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    FSRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface EclipseComponent extends PlatformComponent {
}
