package mb.statix.multilang.utils;

import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class ContextUtils {
    public static MultiLangConfig readYamlConfig(ResourceService resourceService, ResourcePath projectDir) {
        ResourcePath propertiesPath = projectDir.appendRelativePath("multilang.yaml");
        try(InputStream input = resourceService.getReadableResource(propertiesPath).openRead()) {
            return readYamlConfig(input);
        } catch(IOException e) {
            throw new MultiLangAnalysisException("Cannot load " + propertiesPath, e);
        }
    }

    public static MultiLangConfig readYamlConfig(InputStream inputStream) {
        try {
            return new Yaml(new Constructor(MultiLangConfig.class)).load(inputStream);
        } catch(Exception e) {
            throw new MultiLangAnalysisException("Cannot load multilang yaml config", e);
        }
    }
}
