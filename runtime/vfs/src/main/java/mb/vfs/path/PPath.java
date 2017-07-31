package mb.vfs.path;

import mb.vfs.access.DirAccess;
import mb.vfs.list.PathMatcher;
import mb.vfs.list.PathWalker;

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


    boolean isDir();

    boolean isFile();

    boolean exists();
    
    long lastModifiedTimeMs() throws IOException;

    
    PPath normalized();
    
    PPath relativizeFrom(PPath other);
    
    @Nullable PPath parent();

    @Nullable PPath leaf();
    
    @Nullable String extension();


    PPath resolve(PPath other);
    
    PPath resolve(String other);
    
    PPath replaceExtension(String extension);
    

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
    
    byte[] readAllBytes() throws IOException;

    OutputStream outputStream() throws IOException;
    
    
    String toString();
}
