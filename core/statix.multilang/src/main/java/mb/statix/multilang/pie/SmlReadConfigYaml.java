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

import javax.inject.Inject;
import java.io.Serializable;

@MultiLangScope
public class SmlReadConfigYaml implements TaskDef<ResourcePath, MultiLangConfig> {
    private static final String configFileName = "multilang.yaml";

    @Inject public SmlReadConfigYaml() {
    }

    @Override
    public String getId() {
        return SmlReadConfigYaml.class.getCanonicalName();
    }

    @Override
    public MultiLangConfig exec(ExecContext context, ResourcePath projectPath) throws Exception {
        ResourcePath configFilePath = projectPath.appendRelativePath(configFileName);
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
