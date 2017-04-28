package mb.pipe.run.core.vfs;

import java.io.File;
import java.net.URI;

import javax.annotation.Nullable;

import mb.pipe.run.core.PipeRunEx;

/**
 * Interface for access to the virtual file system.
 */
public interface IResourceSrv extends AutoCloseable {
    /**
     * Returns a file system object for given (absolute or relative to the root) URI. The given URI will be encoded (\,
     * /, and : symbols will not be encoded) in its entirely. If your URI is already encoded, convert it to an
     * {@link URI} and call {@link #resolve(URI)} instead.
     * 
     * See <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of
     * URIs.
     * 
     * @param uri
     *            Absolute or relative to the root URI to resolve.
     * @return File system object for given URI.
     * @throws PipeRunEx
     *             When <code>uri</code> is invalid.
     */
    IResource resolve(String uri);

    /**
     * Returns a local file system object for given Java file system object.
     * 
     * @param file
     *            Java file system object to resolve.
     * @return File system object for given Java file system object.
     * @throws PipeRunEx
     *             When file is invalid.
     */
    IResource resolve(File file);

    /**
     * Returns a file system object for given Java URI object.
     * 
     * See <a href="http://commons.apache.org/proper/commons-vfs/filesystems.html">FVS file systems</a> for examples of
     * URIs.
     * 
     * @param uri
     *            Java URI object to resolve.
     * @return File system object for given Java URI object.
     * @throws PipeRunEx
     *             When <code>uri</code> is invalid.
     */
    IResource resolve(URI uri);


    /**
     * Attempts to get a local file for given resource, or copies the resource to the local file system if it does not
     * reside on the local file system.
     * 
     * @param resource
     *            Resource to get a local file for.
     * @return Local file.
     * @throws PipeRunEx
     *             When given resource does not exist.
     */
    File localFile(IResource resource);

    /**
     * Attempts to get a local file for given resource, or copies the resource to the local file system at given
     * directory if it does not reside on the local file system.
     * 
     * @param resource
     *            Resource to get a local file for.
     * @param dir
     *            Directory to copy the resources to if they are not on a local filesystem. Must be on the local
     *            filesystem.
     * @return Local file.
     * @throws PipeRunEx
     *             When given resource does not exist.
     */
    File localFile(IResource resource, IResource dir);

    /**
     * Attempts to get a local file handle for given resource.
     * 
     * @param resource
     *            Resource to get a local file handle for.
     * @return Local file handle, or null if given resource does not reside on the local file system.
     */
    @Nullable File localPath(IResource resource);
}
