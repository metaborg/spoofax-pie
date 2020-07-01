package mb.statix.multilang;

import dagger.Module;
import dagger.Provides;

@Module
public class MultiLangModule {
    private final AnalysisContextService analysisContextService;

    public MultiLangModule(AnalysisContextService analysisContextService) {
        this.analysisContextService = analysisContextService;
    }

    @Provides public AnalysisContextService provideAnalysisContextService() {
        return analysisContextService;
    }
}
