package mb.statix.multilang.pie.config;

import dagger.Lazy;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.ConfigurationException;
import mb.statix.multilang.metadata.ContextDataManager;
import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.MultiLang;
import mb.statix.multilang.MultiLangScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@MultiLangScope
public class SmlBuildContextConfiguration implements TaskDef<SmlBuildContextConfiguration.Input, Result<ContextConfig, ConfigurationException>> {
    public static class Input implements Serializable {
        private final ResourcePath projectDir;
        private final LanguageId languageId;

        public Input(ResourcePath projectDir, LanguageId languageId) {
            this.projectDir = projectDir;
            this.languageId = languageId;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectDir.equals(input.projectDir) &&
                languageId.equals(input.languageId);
        }

        @Override public int hashCode() {
            return Objects.hash(projectDir, languageId);
        }

        @Override public String toString() {
            return "Input{" +
                "projectDir=" + projectDir +
                ", languageId=" + languageId +
                '}';
        }
    }

    private final SmlReadConfigYaml readConfigYaml;
    private final Lazy<ContextDataManager> contextDataManager;

    @Inject
    public SmlBuildContextConfiguration(SmlReadConfigYaml readConfigYaml, @MultiLang Lazy<ContextDataManager> contextDataManager) {
        this.readConfigYaml = readConfigYaml;
        this.contextDataManager = contextDataManager;
    }

    @Override
    public String getId() {
        return SmlBuildContextConfiguration.class.getCanonicalName();
    }

    @Override
    public Result<ContextConfig, ConfigurationException> exec(ExecContext context, Input input) {
        return context.require(readConfigYaml.createTask(input.projectDir))
            .mapErr(ConfigurationException::new)
            .flatMap(dynamicConfig -> {
                final ContextId contextId = dynamicConfig.languageContexts()
                    .getOrDefault(input.languageId, contextDataManager.get().getDefaultContextId(input.languageId));

                Set<LanguageId> languages = new HashSet<>(contextDataManager.get().getContextLanguages(contextId));
                // Remove all languages with dynamic configurations
                languages.removeAll(dynamicConfig.languageContexts().keySet());
                // Add all languages which have the same context configured
                languages.addAll(dynamicConfig.languageContexts().entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().equals(contextId))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet()));

                final @Nullable ContextSettings settings = dynamicConfig.contextSettings().get(contextId);
                final @Nullable String logLevel = settings == null ? null : settings.logLevel();
                final boolean stripTraces = settings != null && settings.stripTraces();

                final ContextConfig contextConfig = ImmutableContextConfig.builder()
                    .addAllLanguages(languages)
                    .logLevel(logLevel)
                    .stripTraces(stripTraces)
                    .build();

                if(!contextConfig.languages().contains(input.languageId)) {
                    return Result.ofErr(new ConfigurationException("BUG: In project '"
                        + input.projectDir + "', language " + input.languageId
                        + " has configured to do analysis in context " + contextId
                        + ", but it is not included in the configuration for that context. "
                        + "Included languages: " + contextConfig.languages()));
                }
                return Result.ofOk(contextConfig);
            });
    }
}
