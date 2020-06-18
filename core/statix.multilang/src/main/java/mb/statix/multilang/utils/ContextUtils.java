package mb.statix.multilang.utils;

import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.MultiLangConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class ContextUtils {
    public static MultiLangConfig readYamlConfig(ResourceService resourceService, ResourcePath projectDir) {
        try {
            ResourcePath propertiesPath = projectDir.appendRelativePath("multilang.yaml");
            try (InputStream input = resourceService.getReadableResource(propertiesPath).openRead()) {
                return readYamlConfig(input);
            }
        } catch(IOException e) {
            e.printStackTrace();
            return new MultiLangConfig();
        }
    }

    public static MultiLangConfig readYamlConfig(InputStream inputStream) {
        try {
            return new Yaml(new Constructor(MultiLangConfig.class)).load(inputStream);
        } catch(Exception e) {
            e.printStackTrace();
            return new MultiLangConfig();
        }
    }
}
