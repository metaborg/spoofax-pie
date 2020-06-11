package mb.statix.multilang;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;

@MultilangScope
@Component(dependencies = PlatformComponent.class)
public interface MultilangComponent {
    AnalysisContextService getAnalysisContextService();
}
