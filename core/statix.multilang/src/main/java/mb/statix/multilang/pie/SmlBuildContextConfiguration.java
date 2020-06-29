package mb.statix.multilang.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.MultiLangAnalysisException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

public class SmlBuildContextConfiguration implements TaskDef<SmlBuildContextConfiguration.Input, ContextConfig> {
    public static class Input implements Serializable {
        private final ResourcePath projectDir;
        private final ContextId contextId;

        public Input(ResourcePath projectDir, ContextId contextId) {
            this.projectDir = projectDir;
            this.contextId = contextId;
        }
    }

    private final SmlReadConfigYaml readConfigYaml;
    private final AnalysisContextService analysisContextService;

    public SmlBuildContextConfiguration(SmlReadConfigYaml readConfigYaml, AnalysisContextService analysisContextService) {
        this.readConfigYaml = readConfigYaml;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public String getId() {
        return SmlBuildContextConfiguration.class.getCanonicalName();
    }

    @Override
    public ContextConfig exec(ExecContext context, Input input) throws Exception {
        @Nullable ContextConfig staticConfig = analysisContextService.getContextConfig(input.contextId);
        @Nullable ContextConfig dynamicConfig = context.require(readConfigYaml
            .createTask(new SmlReadConfigYaml.Input(input.projectDir)))
            .getCustomContexts()
            .get(input.contextId);

        return Stream.of(staticConfig, dynamicConfig)
            .filter(Objects::nonNull)
            .reduce(ContextConfig::merge)
            .orElseThrow(() -> new MultiLangAnalysisException("No configuration for context with id: " + input.contextId));
    }
}
