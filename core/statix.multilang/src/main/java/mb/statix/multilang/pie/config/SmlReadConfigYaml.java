package mb.statix.multilang.pie.config;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.MultiLangScope;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@MultiLangScope
public class SmlReadConfigYaml implements TaskDef<ResourcePath, Result<MultiLangConfig, IOException>> {
    private static final String configFileName = "multilang.yaml";

    @Inject public SmlReadConfigYaml() {
    }

    @Override
    public String getId() {
        return SmlReadConfigYaml.class.getCanonicalName();
    }

    @Override
    public Result<MultiLangConfig, IOException> exec(ExecContext context, ResourcePath projectPath) {
        try {
            ResourcePath configFilePath = projectPath.appendRelativePath(configFileName);
            ReadableResource configFileResource = context.require(configFilePath, context.getDefaultRequireReadableResourceStamper());

            if(!configFileResource.exists() || !configFileResource.isReadable()) {
                return Result.ofOk(ImmutableMultiLangConfig.builder().build());
            }
            Yaml reader = new Yaml(new Constructor(MutableMultilangConfig.class));
            reader.setBeanAccess(BeanAccess.FIELD); // Needed to instantiate private class
            MutableMultilangConfig config = reader.load(configFileResource.openRead());
            return Result.ofOk(config.asImmutable());
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }

    // Local mutable bean used to parse yaml config
    private static class MutableMultilangConfig {
        private HashMap<LanguageId, ContextId> languageContexts;
        private HashMap<ContextId, MutableContextConfig> contextConfigs;

        public MutableMultilangConfig(HashMap<LanguageId, ContextId> languageContexts,
                                      HashMap<ContextId, MutableContextConfig> contextConfigs) {
            this.languageContexts = languageContexts;
            this.contextConfigs = contextConfigs;
        }

        public MutableMultilangConfig() {
            this(new HashMap<>(), new HashMap<>());
        }

        public Map<LanguageId, ContextId> getLanguageContexts() {
            return Collections.unmodifiableMap(languageContexts);
        }

        public void setLanguageContexts(HashMap<LanguageId, ContextId> languageContexts) {
            this.languageContexts = languageContexts;
        }

        public Map<ContextId, MutableContextConfig> getContextConfigs() {
            return Collections.unmodifiableMap(contextConfigs);
        }

        public void setLogging(HashMap<ContextId, MutableContextConfig> contextConfigs) {
            this.contextConfigs = contextConfigs;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            MutableMultilangConfig that = (MutableMultilangConfig)o;
            return Objects.equals(languageContexts, that.languageContexts) &&
                Objects.equals(contextConfigs, that.contextConfigs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(languageContexts, contextConfigs);
        }

        @Override public String toString() {
            return "MultiLangConfig{" +
                "languageContexts=" + languageContexts +
                ", contextConfigs=" + contextConfigs +
                '}';
        }

        public MultiLangConfig asImmutable() {
            Map<ContextId, ContextSettings> contextSettings = contextConfigs
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, c -> ImmutableContextSettings.builder()
                        .stripTraces(c.getValue().stripTraces())
                        .logLevel(c.getValue().getLogging())
                        .build()));

            return ImmutableMultiLangConfig
                .builder()
                .languageContexts(languageContexts)
                .contextSettings(contextSettings)
                .build();
        }
    }

    private static class MutableContextConfig {
        private String logging;
        private boolean stripTraces;

        public MutableContextConfig(String logging, boolean stripTraces) {
            this.logging = logging;
            this.stripTraces = stripTraces;
        }

        public MutableContextConfig() { }

        public String getLogging() {
            return logging;
        }

        public boolean stripTraces() {
            return stripTraces;
        }

        public void setLogging(String logging) {
            this.logging = logging;
        }

        public void setStripTraces(boolean stripTraces) {
            this.stripTraces = stripTraces;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            MutableContextConfig that = (MutableContextConfig)o;
            return stripTraces == that.stripTraces &&
                logging.equals(that.logging);
        }

        @Override
        public int hashCode() {
            return Objects.hash(logging, stripTraces);
        }

        @Override public String toString() {
            return "ContextConfig{" +
                "logging='" + logging + '\'' +
                ", stripTraces=" + stripTraces +
                '}';
        }
    }
}
