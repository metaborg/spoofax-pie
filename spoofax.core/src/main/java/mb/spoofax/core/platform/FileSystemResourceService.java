package mb.spoofax.core.platform;

import mb.fs.api.FileSystem;
import mb.fs.api.node.FSNode;
import mb.fs.api.path.FSPath;

import javax.inject.Inject;

public class FileSystemResourceService implements ResourceService {
    private final FileSystem fileSystem;

    @Inject public FileSystemResourceService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override public FSNode getNode(FSPath path) {
        return fileSystem.getNode(path);
    }
}
