package mb.pipe.run.core.util;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.IProject;
import org.metaborg.util.file.FileUtils;

public class Path implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String uri;
    private transient FileObject fileObject;


    public static Path resolveStatic(String uri) {
        return new Path(uri);
    }

    public static Path projectPath(IProject project) {
        return new Path(project.location());
    }


    public Path(String uri) {
        this(uri, null);
    }

    public Path(FileObject fileObject) {
        this(fileObject.getName().getURI(), fileObject);
    }

    public Path(String uri, @Nullable FileObject fileObject) {
        this.uri = uri;
        this.fileObject = fileObject;
    }


    public String uri() {
        return uri;
    }

    public FileObject fileObject() {
        init();
        return fileObject;
    }

    public Path resolve(String subUri) throws FileSystemException {
        init();
        final FileObject resolved = fileObject.resolveFile(subUri);
        return new Path(resolved);
    }

    public Path resolveWithExt(String subUri, String ext) throws FileSystemException {
        return resolve(subUri + "." + ext);
    }


    private void init() {
        if(fileObject == null) {
            fileObject = StaticFacade.spoofax().resourceService.resolve(uri);
        }
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + uri.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final Path other = (Path) obj;
        if(!uri.equals(other.uri))
            return false;
        return true;
    }

    @Override public String toString() {
        return uri;
    }

    public String toSanitizedString() {
        return FileUtils.sanitize(uri);
    }
}
