package mb.statix.multilang.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.MultiLangScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@MultiLangScope
public class SmlBuildContextConfiguration implements TaskDef<SmlBuildContextConfiguration.Input, ContextConfig> {
    public static class Input implements Serializable {
        private final ResourcePath projectDir;
        private final LanguageId languageId;

        public Input(ResourcePath projectDir, LanguageId languageId) {
            this.projectDir = projectDir;
            this.languageId = languageId;
        }
    }

    private final SmlReadConfigYaml readConfigYaml;
    private final AnalysisContextService analysisContextService;

    @Inject public SmlBuildContextConfiguration(SmlReadConfigYaml readConfigYaml, AnalysisContextService analysisContextService) {
        this.readConfigYaml = readConfigYaml;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public String getId() {
        return SmlBuildContextConfiguration.class.getCanonicalName();
    }

    @Override
    public ContextConfig exec(ExecContext context, Input input) {
        final MultiLangConfig config = context.require(readConfigYaml
            .createTask(new SmlReadConfigYaml.Input(input.projectDir)));

        ContextId contextId = config.getLanguageContexts()
            .getOrDefault(input.languageId, analysisContextService.getDefaultContextId(input.languageId));

        Set<LanguageId> staticLanguages = analysisContextService.getContextLanguages(contextId);
        @Nullable ContextConfig dynamicConfig = config
            .getCustomContexts()
            .get(contextId);

        if(dynamicConfig == null) {
            ContextConfig result = new ContextConfig();
            result.setLanguages(new ArrayList<>(staticLanguages));
            return result;
        } else if(staticLanguages.isEmpty()) {
            return dynamicConfig;
        } else {
            ContextConfig result = new ContextConfig();
            HashSet<LanguageId> languages = new HashSet<>(dynamicConfig.getLanguages());
            languages.addAll(staticLanguages);
            result.setLanguages(new ArrayList<>(languages));
            result.setLogLevel(dynamicConfig.getLogLevel());
            return result;
        }
    }
}
