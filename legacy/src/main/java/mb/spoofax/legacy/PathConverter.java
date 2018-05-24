package mb.spoofax.legacy;

import com.google.inject.Inject;
import mb.pie.vfs.path.PPath;
import mb.pie.vfs.path.PathSrv;
import org.apache.commons.vfs2.FileObject;

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
