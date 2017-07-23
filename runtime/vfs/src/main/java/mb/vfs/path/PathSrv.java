package mb.vfs.path;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import javax.annotation.Nullable;

/**
 * Interface for resolving paths.
 */
public interface PathSrv extends AutoCloseable {
    /**
     * Returns a path for given (absolute or relative to the root) URI as a string. The string will be URI encoded (\,
     * /, and : symbols will not be encoded) in its entirely. If your URI is already encoded, convert it to an
     * {@link URI} and call {@link #resolve(URI)} instead.
     *
     * @param uri
     *            URI as a string to resolve.
     * @return Path for given URI.
     */
    PPath resolve(String uri);

    /**
     * Returns a path for given URI.
     *
     * @param uri
     *            Java URI object to resolve.
     * @return Path for given URI.
     */
    PPath resolve(URI uri);

    /**
     * Returns a path for given Java path.
     *
     * @param path
     *            Java path to resolve.
     * @return Path for given Java path.
     */
    PPath resolve(Path path);


    /**
     * Returns a local path for given path as a string.
     *
     * @param path
     *            Path as a string to resolve.
     * @return Path for given URI.
     */
    PPath resolveLocal(String path);

    /**
     * Returns a path for given local file.
     *
     * @param file
     *            Local file to resolve.
     * @return Path for given local file.
     */
    PPath resolveLocal(File file);


    /**
     * Attempts to get a local file for given path, or copies the path to the local file system if it does not reside on
     * the local file system.
     *
     * @param path
     *            Path to get a local file for.
     * @return Local file.
     * @throws PipeRunEx
     *             When given path does not exist.
     */
    File localFile(PPath path) throws IOException;

    /**
     * Attempts to get a local file for given path, or copies the path to the local file system at given directory if it
     * does not reside on the local file system.
     *
     * @param path
     *            Path to get a local file for.
     * @param dir
     *            Directory to copy the resources to if they are not on a local filesystem. Must be on the local
     *            filesystem.
     * @return Local file.
     * @throws PipeRunEx
     *             When given path does not exist, or when dir is not on the local filesystem.
     */
    File localFile(PPath path, PPath dir) throws IOException;

    /**
     * Attempts to get a local file for given path.
     *
     * @param path
     *            Path to get a local file for.
     * @return Local file, or null if given path does not reside on the local file system.
     */
    @Nullable File localPath(PPath path);


    void close() throws IOException;
}
