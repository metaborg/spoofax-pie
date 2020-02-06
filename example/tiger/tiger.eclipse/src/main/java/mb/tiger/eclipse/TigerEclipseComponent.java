package mb.tiger.eclipse;

import dagger.Component;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.eclipse.EclipseLanguageComponent;

@LanguageScope
@Component(modules = {TigerModule.class, TigerEclipseModule.class}, dependencies = PlatformComponent.class)
public interface TigerEclipseComponent extends EclipseLanguageComponent, TigerComponent {
    TigerEditorTracker getEditorTracker();
}
