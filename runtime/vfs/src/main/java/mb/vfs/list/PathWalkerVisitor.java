package mb.vfs.list;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import mb.vfs.access.DirAccess;
import mb.vfs.path.PPath;
import mb.vfs.path.PPathImpl;

public class PathWalkerVisitor implements FileVisitor<Path> {
    private final PathWalker walker;
    private final PPath root;
    private final @Nullable DirAccess access;
    private final Stream.Builder<PPath> streamBuilder;


    public PathWalkerVisitor(PathWalker walker, PPath root, @Nullable DirAccess access,
        Stream.Builder<PPath> streamBuilder) {
        this.walker = walker;
        this.root = root;
        this.access = access;
        this.streamBuilder = streamBuilder;
    }


    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        final PPath path = new PPathImpl(dir);
        if(walker.matches(path, root)) {
            streamBuilder.add(path);
        }
        if(walker.traverse(path, root)) {
            if(access != null) {
                access.readDir(path);
            }
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        final PPath path = new PPathImpl(file);
        if(walker.matches(path, root)) {
            streamBuilder.add(path);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }
}
