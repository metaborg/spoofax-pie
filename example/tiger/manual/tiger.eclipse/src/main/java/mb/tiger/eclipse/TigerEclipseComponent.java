package mb.tiger.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.ResourceServiceComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;
import mb.spoofax.eclipse.EclipsePlatformComponent;
import mb.tiger.spoofax.TigerComponent;
import mb.tiger.spoofax.TigerModule;
import mb.tiger.spoofax.TigerResourcesComponent;
import mb.tiger.spoofax.TigerScope;

@TigerScope
@Component(
    modules = {
        TigerModule.class,
        TigerEclipseModule.class
    },
    dependencies = {
        TigerResourcesComponent.class,
        ResourceServiceComponent.class,
        EclipsePlatformComponent.class
    }
)
public interface TigerEclipseComponent extends EclipseLanguageComponent, TigerComponent {
    TigerEditorTracker getEditorTracker();
}
