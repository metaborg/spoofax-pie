package mb.statix.multilang;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;

@MultiLangScope
@Component(dependencies = PlatformComponent.class)
public interface MultiLangComponent {
    AnalysisContextService getAnalysisContextService();
}
