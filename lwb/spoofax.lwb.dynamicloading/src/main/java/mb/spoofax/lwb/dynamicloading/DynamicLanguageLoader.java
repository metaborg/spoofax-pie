package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public interface DynamicLanguageLoader {
    DynamicLanguage load(
        ResourcePath rootDirectory,
        CompileLanguageInput compileInput,
        Iterable<ResourcePath> classPath
    ) throws Exception;


    static URL[] classPathToUrl(Iterable<ResourcePath> classPath, ResourceService resourceService) throws IOException {
        final ArrayList<URL> classPathUrls = new ArrayList<>();
        for(ResourcePath path : classPath) {
            final @Nullable File file = resourceService.toLocalFile(path);
            if(file == null) {
                throw new IOException("Cannot dynamically load language; resource at path '" + path + "' is not on the local filesystem, and can therefore not be loaded into a URLClassLoader");
            }
            classPathUrls.add(file.toURI().toURL());
        }
        return classPathUrls.toArray(new URL[0]);
    }
}
