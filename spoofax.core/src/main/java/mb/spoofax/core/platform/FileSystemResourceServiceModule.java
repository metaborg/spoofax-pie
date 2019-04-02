package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import mb.fs.api.FileSystem;
import mb.fs.java.JavaFileSystem;

@Module
public class FileSystemResourceServiceModule {
    private final FileSystem fileSystem;

    public FileSystemResourceServiceModule() {
        this(JavaFileSystem.instance);
    }

    public FileSystemResourceServiceModule(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Provides ResourceService provideResourceService() {
        return new FileSystemResourceService(fileSystem);
    }
}
