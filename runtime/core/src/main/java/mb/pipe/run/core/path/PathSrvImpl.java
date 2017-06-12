package mb.pipe.run.core.path;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nullable;

import mb.pipe.run.core.PipeRunEx;

public class PathSrvImpl implements PathSrv {
    private @Nullable Path tempDir = null;


    @Override public void close() throws IOException {
        if(tempDir != null) {
            FileUtils.deleteDirectory(tempDir);
        }
        tempDir = null;
    }


    @Override public PPath resolve(String uriStr) {
        final String uriEncoded = URIEncode.encode(uriStr);
        try {
            URI uri = new URI(uriEncoded);
            if(uri.getScheme() == null) {
                uri = new URI("file", uri.getSchemeSpecificPart(), uri.getFragment());
            }
            return resolve(uri);
        } catch(URISyntaxException e) {
            throw new PipeRunEx(e);
        }
    }

    @Override public PPath resolve(URI uri) {
        final Path javaPath = Paths.get(uri);
        return new PPathImpl(uri, javaPath);
    }

    @Override public PPath resolve(Path path) {
        return new PPathImpl(path);
    }


    @Override public PPath resolveLocal(String path) {
        final Path javaPath = FileSystems.getDefault().getPath(path);
        return new PPathImpl(javaPath.toUri(), javaPath);
    }

    @Override public PPath resolveLocal(File file) {
        return new PPathImpl(file.toPath());
    }


    @Override public File localFile(PPath path) throws IOException {
        return localFile(path, new PPathImpl(tempDir()));
    }

    @Override public File localFile(PPath path, PPath dir) throws IOException {
        final Path javaPath = path.getJavaPath();
        if(javaPath.getFileSystem().equals(FileSystems.getDefault())) {
            return javaPath.toFile();
        }

        final Path javaDir = dir.getJavaPath();
        if(!javaDir.getFileSystem().equals(FileSystems.getDefault())) {
            throw new IOException("Cannot replicate path " + path + " to the local filesystem: replication directory "
                + dir + " does not reside on the local filesystem");
        }

        final Path dest = Files.copy(javaPath, javaDir);
        return dest.toFile();
    }

    @Override public @Nullable File localPath(PPath path) {
        try {
            return path.getJavaPath().toFile();
        } catch(@SuppressWarnings("unused") UnsupportedOperationException e) {
            return null;
        }
    }


    private Path tempDir() throws IOException {
        if(tempDir == null) {
            tempDir = Files.createTempDirectory(null);
        }
        return tempDir;
    }
}
