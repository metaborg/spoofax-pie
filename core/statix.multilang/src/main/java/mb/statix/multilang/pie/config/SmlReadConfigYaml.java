package mb.statix.multilang.pie.config;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.MultiLangConfig;
import mb.statix.multilang.MultiLangScope;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.inject.Inject;
import java.io.IOException;

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

            if (!configFileResource.exists() || !configFileResource.isReadable()) {
                return Result.ofOk(new MultiLangConfig());
            }
            return Result.ofOk(new Yaml(new Constructor(MultiLangConfig.class)).load(configFileResource.openRead()));
        } catch(IOException e) {
            return Result.ofErr(e);
        }
    }
}
