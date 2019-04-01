package mb.spoofax.core.platform;

import dagger.Module;
import dagger.Provides;
import mb.fs.api.FileSystem;

import javax.inject.Singleton;

@Module
public class FileSystemResourceServiceModule {
    private final FileSystem fileSystem;

    public FileSystemResourceServiceModule(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Provides ResourceService provideResourceService() {
        return new FileSystemResourceService(fileSystem);
    }
}
