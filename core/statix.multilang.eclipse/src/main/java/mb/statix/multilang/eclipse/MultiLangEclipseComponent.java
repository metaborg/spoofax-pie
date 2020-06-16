package mb.statix.multilang.eclipse;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.MultiLangScope;

@MultiLangScope
@Component(dependencies = PlatformComponent.class)
public interface MultiLangEclipseComponent {
    AnalysisContextService getAnalysisContextService();
}
