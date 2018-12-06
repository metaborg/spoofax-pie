package mb.spoofax.legacy;

import mb.fs.java.JavaFSNode;
import mb.fs.java.JavaFSPath;
import org.apache.commons.vfs2.FileObject;

public class PathConverter {
    public JavaFSPath toPath(FileObject fileObject) {
        return new JavaFSPath(fileObject.getName().getURI());
    }

    public JavaFSNode toNode(FileObject fileObject) {
        return new JavaFSNode(fileObject.getName().getURI());
    }

    public FileObject toFileObject(JavaFSPath path) {
        return StaticSpoofaxCoreFacade.spoofax().resolve(path.getURI());
    }

    public FileObject toFileObject(JavaFSNode node) {
        return StaticSpoofaxCoreFacade.spoofax().resolve(node.getURI());
    }
}
