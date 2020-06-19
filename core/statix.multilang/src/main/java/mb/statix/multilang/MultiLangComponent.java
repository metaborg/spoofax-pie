package mb.statix.multilang;

import dagger.Component;
import mb.log.api.LoggerFactory;
import mb.spoofax.core.platform.PlatformComponent;

@MultiLangScope
@Component(dependencies = PlatformComponent.class)
public interface MultiLangComponent {
    AnalysisContextService getAnalysisContextService();
    LoggerFactory getLoggerFactory();
}
