package mb.pipe.run.core.path;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class PPathImpl implements PPath {
    private static final long serialVersionUID = 1L;

    private final URI uri;

    private transient @Nullable Path pathCache;


    public PPathImpl(URI uri, @Nullable Path path) {
        this.uri = uri;
        this.pathCache = path;
    }

    public PPathImpl(URI uri) {
        this(uri, null);
    }

    public PPathImpl(Path path) {
        this(path.toUri(), path);
    }


    @Override public URI getUri() {
        return uri;
    }

    @Override public Path getJavaPath() {
        if(pathCache == null) {
            pathCache = Paths.get(uri);
        }
        return pathCache;
    }


    @Override public @Nullable String extension() {
        final Path filePath = getJavaPath().getFileName();
        if(filePath == null) {
            return null;
        }

        final String fileName = getJavaPath().getFileName().toString();
        final int i = fileName.lastIndexOf('.');
        if(i > 0) {
            return fileName.substring(i + 1);
        }
        return null;
    }


    @Override public boolean exists() {
        return Files.exists(getJavaPath());
    }

    @Override public boolean isFile() {
        return Files.isRegularFile(getJavaPath());
    }

    @Override public boolean isDir() {
        return Files.isDirectory(getJavaPath());
    }


    @Override public @Nullable PPath parent() {
        final Path parent = getJavaPath().getParent();
        if(parent == null) {
            return null;
        }
        return new PPathImpl(parent);
    }

    @Override public @Nullable PPath leaf() {
        final Path filePath = getJavaPath().getFileName();
        if(filePath == null) {
            return null;
        }
        return new PPathImpl(filePath);
    }


    @Override public PPath resolve(String other) {
        return new PPathImpl(getJavaPath().resolve(other));
    }

    @Override public Stream<PPath> list() throws IOException {
        // @formatter:off
        return Files
            .list(getJavaPath())
            .map(PPathImpl::new);
        // @formatter:on
    }

    @Override public Stream<PPath> list(PathMatcher matcher) throws IOException {
        // @formatter:off
        return Files
            .list(getJavaPath())
            .map(path -> (PPath) new PPathImpl(path))
            .filter(matcher::matches);
        // @formatter:on
    }

    @Override public Stream<PPath> walk() throws IOException {
        // @formatter:off
        return Files
            .walk(getJavaPath())
            .map(PPathImpl::new);
        // @formatter:on
    }

    @Override public Stream<PPath> walk(PathWalker walker, @Nullable DirAccess access) throws IOException {
        final Stream.Builder<PPath> streamBuilder = Stream.builder();
        final PathWalkerVisitor visitor = new PathWalkerVisitor(walker, access, streamBuilder);
        Files.walkFileTree(getJavaPath(), visitor);
        return streamBuilder.build();
    }


    @Override public InputStream inputStream() throws IOException {
        return Files.newInputStream(getJavaPath(), StandardOpenOption.READ);
    }

    @Override public OutputStream outputStream() throws IOException {
        return Files.newOutputStream(getJavaPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    
    @Override public String toString() {
        return getJavaPath().toString();
    }
    

    @Override public int hashCode() {
        return uri.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PPathImpl other = (PPathImpl) obj;
        if(!uri.equals(other.uri))
            return false;
        return true;
    }
}
