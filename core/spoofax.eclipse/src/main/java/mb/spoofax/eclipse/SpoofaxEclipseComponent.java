package mb.spoofax.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.ResourceRegistriesModule;
import mb.spoofax.core.platform.ResourceServiceModule;
import mb.spoofax.eclipse.command.EnclosingCommandContextProvider;
import mb.spoofax.eclipse.editor.PartClosedCallback;
import mb.spoofax.eclipse.editor.ScopeManager;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.resource.EclipseResourceRegistryModule;
import mb.spoofax.eclipse.util.ColorShare;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.eclipse.util.StyleUtil;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    ResourceRegistriesModule.class,
    EclipseResourceRegistryModule.class,
    ResourceServiceModule.class,
    PlatformPieModule.class,
    SpoofaxEclipseModule.class
})
public interface SpoofaxEclipseComponent extends PlatformComponent {
    PieRunner getPieRunner();

    ResourceUtil getResourceUtil();

    ColorShare getColorShare();

    StyleUtil getStyleUtils();

    ScopeManager getScopeManager();

    PartClosedCallback getPartClosedCallback();

    EnclosingCommandContextProvider getEnclosingCommandContextProvider();
}
