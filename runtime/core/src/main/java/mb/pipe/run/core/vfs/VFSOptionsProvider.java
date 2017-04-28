package mb.pipe.run.core.vfs;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;

import com.google.inject.Provider;
import com.google.inject.name.Named;

public class VFSOptionsProvider implements Provider<FileSystemOptions> {
    private final ClassLoader resourcesClassLoader;


    public VFSOptionsProvider(@Named("ResourceClassLoader") ClassLoader resourcesClassLoader) {
        this.resourcesClassLoader = resourcesClassLoader;
    }


    @Override public FileSystemOptions get() {
        final FileSystemOptions options = new FileSystemOptions();

        final ClassLoader classLoader;
        if(resourcesClassLoader != null) {
            classLoader = resourcesClassLoader;
        } else {
            classLoader = this.getClass().getClassLoader();
        }

        ResourceFileSystemConfigBuilder.getInstance().setClassLoader(options, classLoader);

        return options;
    }
}
