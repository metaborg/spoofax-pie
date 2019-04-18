package mb.spoofax.eclipse;

import dagger.Component;
import mb.pie.dagger.PieModule;
import mb.spoofax.core.platform.FSRegistryModule;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.ResourceServiceModule;
import mb.spoofax.eclipse.util.ColorShare;
import mb.spoofax.eclipse.util.FileUtil;
import mb.spoofax.eclipse.util.StyleUtil;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
    LoggerFactoryModule.class,
    FSRegistryModule.class,
    ResourceServiceModule.class,
    PieModule.class
})
public interface SpoofaxEclipseComponent extends PlatformComponent {
    ColorShare getColorShare();

    StyleUtil getStyleUtils();

    FileUtil getFileUtils();
}
