package mb.pipe.run.core.path;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Override public PPath resolve(String other) {
        final Path newPath = getJavaPath().resolve(other);
        return new PPathImpl(newPath);
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

    @Override public String toString() {
        return uri.toString();
    }
}
