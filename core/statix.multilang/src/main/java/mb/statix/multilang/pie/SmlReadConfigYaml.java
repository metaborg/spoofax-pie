package mb.statix.multilang.pie;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.MultiLangScope;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.Serializable;

@MultiLangScope
public class SmlReadConfigYaml implements TaskDef<SmlReadConfigYaml.Input, MultiLangConfig> {
    public static class Input implements Serializable {
        private final ResourcePath projectDir;

        public Input(ResourcePath projectDir) {
            this.projectDir = projectDir;
        }
    }

    private static final String configFileName = "multilang.yaml";

    @Override
    public String getId() {
        return SmlReadConfigYaml.class.getCanonicalName();
    }

    @Override
    public MultiLangConfig exec(ExecContext context, Input input) throws Exception {
        ResourcePath configFilePath = input.projectDir.appendRelativePath(configFileName);
        ReadableResource configFileResource = context.require(configFilePath, context.getDefaultRequireReadableResourceStamper());

        if (!configFileResource.exists() || !configFileResource.isReadable()) {
            return new MultiLangConfig();
        }
        try {
            return new Yaml(new Constructor(MultiLangConfig.class)).load(configFileResource.openRead());
        } catch(Exception e) {
            throw new MultiLangAnalysisException("Cannot load multilang yaml config", e);
        }
    }
}
