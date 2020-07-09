package mb.statix.multilang.pie;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.ConfigurationException;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@MultiLangScope
public class SmlBuildContextConfiguration implements TaskDef<SmlBuildContextConfiguration.Input,
    Result<SmlBuildContextConfiguration.Output, ConfigurationException>> {
    public static class Input implements Serializable {
        private final ResourcePath projectDir;
        private final LanguageId languageId;

        public Input(ResourcePath projectDir, LanguageId languageId) {
            this.projectDir = projectDir;
            this.languageId = languageId;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectDir.equals(input.projectDir) &&
                languageId.equals(input.languageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectDir, languageId);
        }

        @Override public String toString() {
            return "Input{" +
                "projectDir=" + projectDir +
                ", languageId=" + languageId +
                '}';
        }
    }

    public static class Output implements Serializable {
        private final ContextId contextId;
        private final ContextConfig contextConfig;

        public Output(ContextId contextId, ContextConfig contextConfig) {
            this.contextId = contextId;
            this.contextConfig = contextConfig;
        }

        public ContextId getContextId() {
            return contextId;
        }

        public ContextConfig getContextConfig() {
            return contextConfig;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Output output = (Output)o;
            return contextId.equals(output.contextId) &&
                contextConfig.equals(output.contextConfig);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contextId, contextConfig);
        }

        @Override public String toString() {
            return "Output{" +
                "contextId=" + contextId +
                ", contextConfig=" + contextConfig +
                '}';
        }
    }

    private final SmlReadConfigYaml readConfigYaml;
    private final AnalysisContextService analysisContextService;

    @Inject
    public SmlBuildContextConfiguration(SmlReadConfigYaml readConfigYaml, AnalysisContextService analysisContextService) {
        this.readConfigYaml = readConfigYaml;
        this.analysisContextService = analysisContextService;
    }

    @Override
    public String getId() {
        return SmlBuildContextConfiguration.class.getCanonicalName();
    }

    @Override
    public Result<Output, ConfigurationException> exec(ExecContext context, Input input) {
        return context.require(readConfigYaml.createTask(input.projectDir))
            .mapErr(ConfigurationException::new)
            .flatMap(config -> {
                ContextId contextId = config.getLanguageContexts()
                    .getOrDefault(input.languageId, analysisContextService.getDefaultContextId(input.languageId));

                Set<LanguageId> staticLanguages = analysisContextService.getContextLanguages(contextId);
                @Nullable ContextConfig dynamicConfig = config
                    .getCustomContexts()
                    .get(contextId);

                final ContextConfig contextConfig;
                if(dynamicConfig == null) {
                    contextConfig = new ContextConfig();
                    contextConfig.setLanguages(new ArrayList<>(staticLanguages));
                } else if(staticLanguages.isEmpty()) {
                    contextConfig = dynamicConfig;
                } else {
                    contextConfig = new ContextConfig();
                    HashSet<LanguageId> languages = new HashSet<>(dynamicConfig.getLanguages());
                    languages.addAll(staticLanguages);
                    contextConfig.setLanguages(new ArrayList<>(languages));
                    contextConfig.setLogLevel(dynamicConfig.getLogLevel());
                }

                if(!contextConfig.getLanguages().contains(input.languageId)) {
                    return Result.ofErr(new ConfigurationException("Invalid configuration. In project '"
                        + input.projectDir
                        + "', language " + input.languageId
                        + "has configured to do analysis in context " + contextId
                        + ", but it is not included in the configuration for that context. "
                        + "Included languages: " + contextConfig.getLanguages()));
                }
                return Result.ofOk(new Output(contextId, contextConfig));
            });
    }
}
