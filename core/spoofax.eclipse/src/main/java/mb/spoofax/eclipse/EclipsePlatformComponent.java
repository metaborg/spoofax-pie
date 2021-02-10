package mb.spoofax.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.spoofax.core.platform.PlatformScope;
import mb.spoofax.eclipse.command.EnclosingCommandContextProvider;
import mb.spoofax.eclipse.editor.PartClosedCallback;
import mb.spoofax.eclipse.editor.ScopeManager;
import mb.spoofax.eclipse.pie.PieRunner;
import mb.spoofax.eclipse.util.ColorShare;
import mb.spoofax.eclipse.util.ResourceUtil;
import mb.spoofax.eclipse.util.StyleUtil;

@PlatformScope
@Component(
    modules = {
        LoggerFactoryModule.class,
        PlatformPieModule.class
    },
    dependencies = {
        EclipseResourceServiceComponent.class
    }
)
public interface EclipsePlatformComponent extends PlatformComponent {
    PieRunner getPieRunner();

    ResourceUtil getResourceUtil();

    ColorShare getColorShare();

    StyleUtil getStyleUtils();

    ScopeManager getScopeManager();

    PartClosedCallback getPartClosedCallback();

    EnclosingCommandContextProvider getEnclosingCommandContextProvider();
}
