package mb.statix.multilang.utils;

import mb.common.util.Properties;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class ContextUtils {
    public static String readContextIdFromProperties(ResourceService resourceService, ResourcePath projectDir,
                                                     LanguageId languageId, String defaultValue) {

        Properties properties = new Properties();
        try {
            ResourcePath propertiesPath = projectDir.appendRelativePath("multilang.properties");
            try (InputStream input = resourceService.getReadableResource(propertiesPath).openRead()) {
                if (input != null) {
                    properties.load(input);
                }
            }
        } catch(IOException e) {
            // Ignore and try with default props
        }

        return properties.getProperty(String.format("%s.context.id", languageId.getId()), defaultValue);
    }

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
