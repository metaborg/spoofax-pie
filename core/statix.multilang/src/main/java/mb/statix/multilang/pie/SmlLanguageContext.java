package mb.statix.multilang.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.MultiLangScope;

import javax.inject.Inject;
import java.io.Serializable;

@MultiLangScope
public class SmlLanguageContext implements TaskDef<SmlLanguageContext.Input, ContextId> {

    public static class Input implements Serializable {
        private final ResourcePath resourcePath;
        private final LanguageId languageId;

        public Input(ResourcePath resourcePath, LanguageId languageId) {
            this.resourcePath = resourcePath;
            this.languageId = languageId;
        }
    }

    private final SmlReadConfigYaml readConfigYaml;
    private final AnalysisContextService analysisContextService;

    @Inject public SmlLanguageContext(SmlReadConfigYaml readConfigYaml, AnalysisContextService analysisContextService) {
        this.readConfigYaml = readConfigYaml;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public String getId() {
        return SmlLanguageContext.class.getCanonicalName();
    }

    @Override
    public ContextId exec(ExecContext context, SmlLanguageContext.Input input) throws Exception {
        final MultiLangConfig config = context.require(readConfigYaml
            .createTask(new SmlReadConfigYaml.Input(input.resourcePath)));

        if (config.getLanguageContexts().containsKey(input.languageId)) {
            return config.getLanguageContexts().get(input.languageId);
        }

        return analysisContextService.getDefaultContextId(input.languageId);
    }
}
