package mb.statix.multilang;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;

@MultiLangScope
@Component(
    modules = MultiLangModule.class,
    dependencies = PlatformComponent.class)
public interface MultiLangComponent {
    AnalysisContextService getAnalysisContextService();
}
