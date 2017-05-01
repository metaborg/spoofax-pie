package mb.pipe.run.core.vfs;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;

public interface IResource {
    String uri();

    IResource resolve(String subUri);

    FileObject fileObject(); // HACK: getter for Apache VFS FileObjects
}
