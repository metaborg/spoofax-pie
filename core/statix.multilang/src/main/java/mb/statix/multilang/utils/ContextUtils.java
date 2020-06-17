package mb.statix.multilang.utils;

import mb.common.util.Properties;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.LanguageId;

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
}
