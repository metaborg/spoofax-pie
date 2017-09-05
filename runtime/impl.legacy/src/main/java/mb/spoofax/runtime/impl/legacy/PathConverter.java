package mb.spoofax.runtime.impl.legacy;

import org.apache.commons.vfs2.FileObject;

import com.google.inject.Inject;

import mb.vfs.path.PPath;
import mb.vfs.path.PathSrv;

public class PathConverter {
    private final PathSrv pathSrv;


    @Inject public PathConverter(PathSrv pathSrv) {
        this.pathSrv = pathSrv;
    }


    public PPath toPath(FileObject fileObject) {
        return pathSrv.resolve(fileObject.getName().getURI());
    }

    public FileObject toFileObject(PPath path) {
        return StaticSpoofaxCoreFacade.spoofax().resolve(path.getJavaPath().toUri());
    }
}
