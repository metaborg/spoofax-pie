package mb.pipe.run.core.vfs;

import java.io.File;
import java.net.URI;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.local.LocalFile;

import com.google.inject.Inject;

import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.log.ILogger;

public class VFSResourceSrv implements IResourceSrv, IVfsSrv {
    private final ILogger logger;
    private final FileSystemManager fileSystemManager;
    private final FileSystemOptions fileSystemOptions;


    @Inject public VFSResourceSrv(ILogger logger, FileSystemManager fileSystemManager, FileSystemOptions options) {
        this.logger = logger.forContext(getClass());
        this.fileSystemManager = fileSystemManager;
        this.fileSystemOptions = options;
    }

    @Override public void close() throws Exception {
        if(fileSystemManager instanceof DefaultFileSystemManager) {
            final DefaultFileSystemManager defaultFileSystemManager = (DefaultFileSystemManager) fileSystemManager;
            final FileReplicator replicator = defaultFileSystemManager.getReplicator();
            if(replicator instanceof DefaultFileReplicator) {
                final DefaultFileReplicator defaultFileReplicator = (DefaultFileReplicator) replicator;
                defaultFileReplicator.close();
            } else {
                logger.warn("File replicator {} does not support cleaning up generated temporary files", replicator);
            }
        } else {
            logger.warn("File system manager {} does not support cleaning up generated temporary files",
                fileSystemManager);
        }
    }



    @Override public IResource resolve(String uri) {
        try {
            final String uriEncoded = URIEncode.encode(uri);
            final FileObject vfsFile = fileSystemManager.resolveFile(uriEncoded, fileSystemOptions);
            return new VFSResource(vfsFile);
        } catch(FileSystemException e) {
            throw new PipeRunEx(e);
        }
    }

    @Override public IResource resolve(File file) {
        try {
            final FileObject vfsFile = fileSystemManager.toFileObject(file);
            return new VFSResource(vfsFile);
        } catch(FileSystemException e) {
            throw new PipeRunEx(e);
        }
    }

    @Override public IResource resolve(URI uri) {
        try {
            final FileObject vfsFile = fileSystemManager.resolveFile(uri.toString());
            return new VFSResource(vfsFile);
        } catch(FileSystemException e) {
            throw new PipeRunEx(e);
        }
    }


    @Override public File localFile(IResource resource) {
        // TODO: remove conversion to FileObject
        final FileObject vfsFile = resource.fileObject();

        if(resource instanceof LocalFile) {
            return FileUtils.toFile(vfsFile);
        }

        try {
            return vfsFile.getFileSystem().replicateFile(vfsFile, new AllFileSelector());
        } catch(FileSystemException e) {
            throw new PipeRunEx("Could not get local file for " + resource, e);
        }
    }

    @Override public File localFile(IResource resource, IResource dir) {
        // TODO: remove conversion to FileObject
        final FileObject vfsFile = resource.fileObject();
        final FileObject vfsDir = dir.fileObject();

        if(vfsFile instanceof LocalFile) {
            return FileUtils.toFile(vfsFile);
        }

        final File localDir = localPath(dir);
        if(localDir == null) {
            throw new PipeRunEx("Replication directory " + dir
                + " is not on the local filesystem, cannot get local file for " + resource);
        }
        try {
            vfsDir.createFolder();

            final FileObject copyLoc;
            if(vfsFile.getType() == FileType.FOLDER) {
                copyLoc = vfsDir;
            } else {
                copyLoc = vfsDir.resolveFile(vfsFile.getName().getBaseName());
            }
            copyLoc.copyFrom(vfsFile, new AllFileSelector());

            return localDir;
        } catch(FileSystemException e) {
            throw new PipeRunEx("Could not get local file for " + resource, e);
        }
    }

    @Override public File localPath(IResource resource) {
        // TODO: remove conversion to FileObject
        final FileObject vfsFile = resource.fileObject();

        if(vfsFile instanceof LocalFile) {
            return FileUtils.toFile(vfsFile);
        }
        return null;
    }


    @Override public FileObject resolveVfs(String uri) {
        try {
            final String uriEncoded = URIEncode.encode(uri);
            final FileObject vfsFile = fileSystemManager.resolveFile(uriEncoded, fileSystemOptions);
            return vfsFile;
        } catch(FileSystemException e) {
            throw new PipeRunEx(e);
        }
    }
}
