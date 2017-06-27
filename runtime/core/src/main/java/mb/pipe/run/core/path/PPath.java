package mb.pipe.run.core.path;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public interface PPath extends Serializable {
    URI getUri();

    Path getJavaPath();


    @Nullable String extension();


    boolean isDir();

    boolean isFile();

    boolean exists();


    @Nullable PPath parent();

    @Nullable PPath leaf();


    PPath resolve(String other);

    default Stream<PPath> list() throws IOException {
        return list(PPaths.allPathMatcher());
    }

    Stream<PPath> list(PathMatcher matcher) throws IOException;

    default Stream<PPath> walk() throws IOException {
        return walk(PPaths.allPathWalker(), null);
    }

    default Stream<PPath> walk(DirAccess access) throws IOException {
        return walk(PPaths.allPathWalker(), access);
    }

    default Stream<PPath> walk(PathWalker walker) throws IOException {
        return walk(walker, null);
    }

    Stream<PPath> walk(PathWalker walker, @Nullable DirAccess access) throws IOException;


    InputStream inputStream() throws IOException;

    OutputStream outputStream() throws IOException;
}
