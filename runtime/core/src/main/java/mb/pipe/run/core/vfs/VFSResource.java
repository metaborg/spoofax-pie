package mb.pipe.run.core.vfs;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.google.common.collect.Lists;

import mb.pipe.run.core.PipeRunEx;
import mb.pipe.run.core.StaticPipeFacade;

public class VFSResource implements Serializable, IResource {
    private static final long serialVersionUID = 1L;

    private final String uri;
    private transient FileObject fileObject;


    public static IResource resolveStatic(String uri) {
        return new VFSResource(uri);
    }

    public static List<IResource> resolveAllStatic(List<String> uris) {
        final List<IResource> paths = Lists.newArrayList();
        for(String uri : uris) {
            paths.add(new VFSResource(uri));
        }
        return paths;
    }


    public VFSResource(String uri) {
        this(uri, null);
    }

    public VFSResource(FileObject fileObject) {
        this(fileObject.getName().getURI(), fileObject);
    }

    public VFSResource(String uri, @Nullable FileObject fileObject) {
        this.uri = uri;
        this.fileObject = fileObject;
    }


    @Override public String uri() {
        return uri;
    }

    @Override public IResource resolve(String subUri) {
        init();
        try {
            final FileObject resolved = fileObject.resolveFile(subUri);
            return new VFSResource(resolved);
        } catch(FileSystemException e) {
            throw new PipeRunEx("Resolving uri " + subUri + " in " + uri + " failed", e);
        }
    }


    @Override public FileObject fileObject() {
        init();
        return fileObject;
    }

    private void init() {
        if(fileObject == null) {
            fileObject = StaticPipeFacade.facade().fileObjectSrv.resolveVfs(uri);
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
        final VFSResource other = (VFSResource) obj;
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
