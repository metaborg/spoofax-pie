package mb.pipe.run.eclipse.vfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import rx.functions.Action1;

public class EclipseFileObject extends AbstractFileObject<EclipseFileSystem> {
    private final AbstractFileName name;
    private final IWorkspaceRoot root;

    private boolean attached = false;
    private IResource resource;
    private IFileInfo info;


    public EclipseFileObject(AbstractFileName name, IWorkspaceRoot root, EclipseFileSystem fs) {
        super(name, fs);
        this.name = name;
        this.root = root;
    }


    public IResource resource() throws CoreException {
        if(resource == null)
            update();

        return resource;
    }


    private void update() throws CoreException {
        updateResource();
        updateFileInfo();
    }

    private void updateResource() {
        final String path = name.getPath();
        resource = root.findMember(path);
        if(resource != null) {
            return;
        }

        final int depth = name.getDepth();
        switch(depth) {
            case 0:
            case 1:
                resource = root;
                break;
            case 2:
                resource = root.getProject(name.getBaseName());
                break;
            default:
                switch(name.getType()) {
                    case FILE:
                        resource = root.getFile(new Path(path));
                        break;
                    case FILE_OR_FOLDER:
                        resource = root.getFile(new Path(path));
                        break;
                    case FOLDER:
                        resource = root.getFolder(new Path(path));
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void updateFileInfo() throws CoreException {
        if(resource == null) {
            return;
        }
        final URI locationURI = resource.getLocationURI();
        if(locationURI == null) {
            return;
        }
        final IFileStore store = EFS.getStore(locationURI);
        info = store.fetchInfo();
    }

    private IPath getPath() {
        return root.getFullPath().append(name.getPath());
    }


    @Override protected void doAttach() throws CoreException {
        if(attached)
            return;
        update();
        attached = true;
    }

    @Override protected void onChange() throws CoreException {
        update();
    }

    @Override protected void doDetach() {
        info = null;
        resource = null;
        attached = false;
    }

    @Override protected FileType doGetType() {
        if(resource == null || !resource.exists())
            return FileType.IMAGINARY;

        switch(resource.getType()) {
            case IResource.FILE:
                return FileType.FILE;
            case IResource.FOLDER:
            case IResource.PROJECT:
            case IResource.ROOT:
                return FileType.FOLDER;
            default:
                return FileType.IMAGINARY;
        }
    }

    @Override protected boolean doIsHidden() {
        return info.getAttribute(EFS.ATTRIBUTE_HIDDEN);
    }

    @Override protected boolean doIsReadable() {
        return info.getAttribute(EFS.ATTRIBUTE_OWNER_READ) || info.getAttribute(EFS.ATTRIBUTE_GROUP_READ)
            || info.getAttribute(EFS.ATTRIBUTE_OTHER_READ);
    }

    @Override protected boolean doIsWriteable() {
        return info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE) || info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE)
            || info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE);
    }

    @Override protected String[] doListChildren() throws CoreException {
        final IContainer container = (IContainer) resource;
        final IResource[] members = container.members();
        final String[] memberNames = new String[members.length];
        for(int i = 0; i < members.length; ++i) {
            memberNames[i] = members[i].getFullPath().toPortableString();
        }
        return memberNames;
    }

    @Override protected FileObject[] doListChildrenResolved() throws CoreException, FileSystemException {
        final String[] children = doListChildren();
        final FileSystem fileSystem = getFileSystem();
        final FileObject[] files = new FileObject[children.length];
        for(int i = 0; i < children.length; ++i) {
            files[i] = fileSystem.resolveFile(children[i]);
        }
        return files;
    }

    @Override protected long doGetContentSize() {
        return info.getLength();
    }

    @Override protected void doDelete() throws CoreException {
        resource.delete(true, null);
    }

    @Override protected void doRename(FileObject newfile) {
        throw new UnsupportedOperationException();
    }

    @Override protected void doCreateFolder() throws CoreException, FileSystemException {
        final IPath path = getPath();
        if(path.segmentCount() == 1) {
            final IProject project = root.getProject(path.segment(0));
            project.create(null);
            project.open(null);
        } else {
            getParent().createFolder();
            final IFolder folder = root.getFolder(path);
            folder.create(true, true, null);
        }
    }

    @Override protected OutputStream doGetOutputStream(boolean bAppend) throws CoreException, FileSystemException {
        final IFile file;
        if(resource == null) {
            final IPath path = getPath();
            if(path.segmentCount() == 1) {
                throw new FileSystemException("Cannot create a file under the workspace root");
            }
            getParent().createFolder();
            file = root.getFile(path);
            file.create(null, true, null);
        } else {
            file = (IFile) resource;
        }

        return new OnCloseByteArrayOutputStream(new Action1<ByteArrayOutputStream>() {
            @Override public void call(ByteArrayOutputStream out) {
                try {
                    if(!file.exists()) {
                        file.create(new ByteArrayInputStream(out.toByteArray()), true, null);
                    } else {
                        file.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, null);
                    }
                } catch(CoreException e) {
                    throw new RuntimeException("Could not set file contents for file " + name, e);
                }
            }
        });
    }

    @Override protected InputStream doGetInputStream() throws CoreException {
        final IFile file = (IFile) resource;
        return file.getContents();
    }

    @Override protected long doGetLastModifiedTime() {
        return resource.getModificationStamp();
    }
}
