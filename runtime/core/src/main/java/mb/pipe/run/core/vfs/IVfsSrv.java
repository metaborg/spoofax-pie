package mb.pipe.run.core.vfs;

import org.apache.commons.vfs2.FileObject;

public interface IVfsSrv {
    FileObject resolveVfs(String uri);
}
