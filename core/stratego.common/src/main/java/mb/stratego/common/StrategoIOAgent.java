package mb.stratego.common;

import com.google.common.collect.Maps;
import mb.log.api.Level;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.api.LoggingOutputStream;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.HierarchicalResourceType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.PrintStreamWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

public class StrategoIOAgent extends IOAgent {
    private static class ResourceHandle implements Closeable {
        final WritableResource resource;

        @Nullable Reader reader = null;
        @Nullable Writer writer = null;
        @Nullable InputStream inputStream = null;
        @Nullable OutputStream outputStream = null;

        ResourceHandle(WritableResource resource) {
            this.resource = resource;
        }

        @Override public void close() throws IOException {
            if(reader != null)
                reader.close();
            if(writer != null)
                writer.close();
            if(inputStream != null)
                inputStream.close();
            if(outputStream != null)
                outputStream.close();
            resource.close();
        }
    }

    private final ResourceService resourceService;
    private final HierarchicalResource tempDir;

    private final Map<Integer, ResourceHandle> openFiles = Maps.newHashMap();

    private final OutputStream stdout;
    private final Writer stdoutWriter;

    private final OutputStream stderr;
    private final Writer stderrWriter;

    private HierarchicalResource workingDir;
    private HierarchicalResource definitionDir;
    private boolean acceptDirChanges;


    public static OutputStream defaultStdout(LoggerFactory factory, String... excludePatterns) {
        final Logger logger = factory.create("stdout");
        return new LoggingOutputStream(logger, Level.Info, excludePatterns);
    }

    public static OutputStream defaultStderr(LoggerFactory factory, String... excludePatterns) {
        final Logger logger = factory.create("stderr");
        return new LoggingOutputStream(logger, Level.Info, excludePatterns);
    }


    public StrategoIOAgent(LoggerFactory loggerFactory, ResourceService resourceService) {
        this(loggerFactory, resourceService, FSResource.workingDirectory());
    }

    public StrategoIOAgent(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource initialDir) {
        this(loggerFactory, resourceService, initialDir, defaultStdout(loggerFactory));
    }

    public StrategoIOAgent(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource initialDir, OutputStream stdout) {
        this(loggerFactory, resourceService, initialDir, stdout, defaultStderr(loggerFactory));
    }

    public StrategoIOAgent(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource initialDir, OutputStream stdout, OutputStream stderr) {
        this(resourceService, initialDir, initialDir, stdout, stderr);
    }

    public StrategoIOAgent(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource workingDir, HierarchicalResource definitionDir) {
        this(resourceService, workingDir, definitionDir, defaultStdout(loggerFactory), defaultStderr(loggerFactory));
    }

    public StrategoIOAgent(LoggerFactory loggerFactory, ResourceService resourceService, HierarchicalResource workingDir, HierarchicalResource definitionDir, OutputStream stdout) {
        this(resourceService, workingDir, definitionDir, stdout, defaultStderr(loggerFactory));
    }

    public StrategoIOAgent(ResourceService resourceService, HierarchicalResource workingDir, HierarchicalResource definitionDir, OutputStream stdout,
        OutputStream stderr) {
        super();
        this.acceptDirChanges = true; // Start accepting dir changes after IOAgent constructor call.

        this.resourceService = resourceService;
        this.tempDir = FSResource.temporaryDirectory();
        this.workingDir = workingDir;
        this.definitionDir = definitionDir;

        this.stdout = stdout;
        this.stdoutWriter = new PrintStreamWriter(new PrintStream(stdout));

        this.stderr = stderr;
        this.stderrWriter = new PrintStreamWriter(new PrintStream(stderr));
    }

    public StrategoIOAgent(StrategoIOAgent other) {
        super();
        this.acceptDirChanges = true; // Start accepting dir changes after IOAgent constructor call.

        this.resourceService = other.resourceService;
        this.tempDir = other.tempDir;
        this.stdout = other.stdout;
        this.stdoutWriter = other.stdoutWriter;
        this.stderr = other.stderr;
        this.stderrWriter = other.stderrWriter;
        this.workingDir = other.workingDir;
        this.definitionDir = other.definitionDir;
    }


    @Override public String getWorkingDir() {
        return workingDir.getPath().asString();
    }

    public HierarchicalResource getWorkingDirResource() {
        return workingDir;
    }

    @Override public void setWorkingDir(@NonNull String newWorkingDir) {
        if(!acceptDirChanges)
            return;

        workingDir = resourceService.appendOrReplaceWithHierarchical(workingDir, newWorkingDir);
    }

    public void setAbsoluteWorkingDir(HierarchicalResource dir) {
        workingDir = dir;
    }


    @Override public String getDefinitionDir() {
        return definitionDir.getPath().asString();
    }

    public HierarchicalResource getDefinitionDirResource() {
        return definitionDir;
    }

    @Override public void setDefinitionDir(@NonNull String newDefinitionDir) {
        if(!acceptDirChanges)
            return;

        definitionDir = resourceService.appendOrReplaceWithHierarchical(definitionDir, newDefinitionDir);
    }

    public void setAbsoluteDefinitionDir(HierarchicalResource dir) {
        definitionDir = dir;
    }


    @Override public String getTempDir() {
        return tempDir.getPath().asString();
    }

    public HierarchicalResource getTempDirResource() {
        return tempDir;
    }


    @Override public Writer getWriter(int fd) {
        if(fd == CONST_STDOUT) {
            return stdoutWriter;
        } else if(fd == CONST_STDERR) {
            return stderrWriter;
        } else {
            final ResourceHandle handle = openFiles.get(fd);
            if(handle.writer == null) {
                assert handle.outputStream == null;
                try {
                    handle.writer =
                        new BufferedWriter(new OutputStreamWriter(internalGetOutputStream(fd), FILE_ENCODING));
                } catch(UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            return handle.writer;
        }
    }


    @Override public OutputStream internalGetOutputStream(int fd) {
        if(fd == CONST_STDOUT) {
            return stdout;
        } else if(fd == CONST_STDERR) {
            return stderr;
        } else {
            final ResourceHandle handle = openFiles.get(fd);
            if(handle.outputStream == null) {
                assert handle.writer == null;
                try {
                    handle.outputStream = handle.resource.openWrite();
                } catch(IOException e) {
                    throw new RuntimeException("Could not get output stream for resource " + handle.resource, e);
                }
            }
            return handle.outputStream;
        }
    }

    @Override public void writeChar(int fd, int c) throws IOException {
        if(fd == CONST_STDOUT || fd == CONST_STDERR) {
            getWriter(fd).append((char)c);
        } else {
            getWriter(fd).append((char)c);
        }
    }

    @Override public boolean closeRandomAccessFile(int fd) throws InterpreterException {
        if(fd == CONST_STDOUT || fd == CONST_STDERR || fd == CONST_STDIN) {
            return true;
        }

        final ResourceHandle handle = openFiles.remove(fd);
        if(handle == null)
            return true; // already closed: be forgiving

        try {
            handle.close();
        } catch(IOException e) {
            throw new RuntimeException("Could not close resource " + handle.resource, e);
        }
        return true;
    }

    @Override public void closeAllFiles() {
        for(ResourceHandle handle : openFiles.values()) {
            try {
                handle.close();
            } catch(IOException e) {
                throw new RuntimeException("Could not close resource " + handle.resource, e);
            }
        }
        openFiles.clear();
    }


    @Override public int openRandomAccessFile(@NonNull String fn, String mode) throws IOException {
        boolean appendMode = mode.indexOf('a') >= 0;
        boolean writeMode = appendMode || mode.indexOf('w') >= 0;
        boolean clearFile = false;

        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, fn);

        if(writeMode) {
            if(!resource.exists()) {
                resource.createFile();
            } else if(!appendMode) {
                clearFile = true;
            }
        }

        if(clearFile) {
            resource.delete();
            resource.createFile();
        }

        openFiles.put(fileCounter, new ResourceHandle(resource));

        return fileCounter++;
    }

    @Override public InputStream internalGetInputStream(int fd) {
        if(fd == CONST_STDIN) {
            return stdin;
        }
        final ResourceHandle handle = openFiles.get(fd);
        if(handle.inputStream == null) {
            try {
                handle.inputStream = handle.resource.openRead();
            } catch(IOException e) {
                throw new RuntimeException("Could not get input stream for resource " + handle.resource, e);
            }
        }
        return handle.inputStream;
    }

    @Override public Reader getReader(int fd) {
        if(fd == CONST_STDIN) {
            return stdinReader;
        }
        final ResourceHandle handle = openFiles.get(fd);
        try {
            if(handle.reader == null)
                handle.reader = new BufferedReader(new InputStreamReader(internalGetInputStream(fd), FILE_ENCODING));
        } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("Could not get reader for resource", e);
        }
        return handle.reader;
    }

    @Override public String readString(int fd) throws IOException {
        char[] buffer = new char[2048];
        final StringBuilder result = new StringBuilder();
        final Reader reader = getReader(fd);
        for(int read = 0; read != -1; read = reader.read(buffer)) {
            result.append(buffer, 0, read);
        }
        return result.toString();
    }

    @Override public String[] readdir(@NonNull String dn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, dn);
        try {
            if(!resource.exists() || !resource.isDirectory()) {
                return new String[0];
            }
            return resource.list().map(c -> resource.getKey().relativize(c.getKey())).toArray(String[]::new);
        } catch(IOException e) {
            throw new RuntimeException("Could not list contents of directory " + resource, e);
        }
    }


    @Override public void printError(String error) {
        try {
            getWriter(CONST_STDERR).write(error + "\n");
        } catch(IOException e) {
            // Like System.err.println, we swallow exceptions
        }
    }

    @Override
    public InputStream openInputStream(@NonNull String fn, boolean isDefinitionFile) {
        final HierarchicalResource dir = isDefinitionFile ? definitionDir : workingDir;
        final HierarchicalResource file = resourceService.appendOrReplaceWithHierarchical(dir, fn);
        try {
            return file.openRead();
        } catch(IOException e) {
            throw new RuntimeException("Could not get input stream for resource " + file, e);
        }
    }


    @Override public OutputStream openFileOutputStream(@NonNull String fn) {
        final HierarchicalResource file = resourceService.appendOrReplaceWithHierarchical(workingDir, fn);
        try {
            return file.openWrite();
        } catch(IOException e) {
            throw new RuntimeException("Could not get output stream for resource " + file, e);
        }
    }

    @Override public File openFile(@NonNull String fn) {
        return new File(fn);
    }

    @Override public String createTempFile(String prefix) throws IOException {
        return FSResource.createTemporaryFile(prefix, "").toString();
    }

    @Override public String createTempDir(String prefix) throws IOException {
        return FSResource.createTemporaryDirectory(prefix).toString();
    }

    @Override public boolean mkdir(@NonNull String fn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, fn);
        try {
            if(resource.exists() && resource.isDirectory()) {
                return false;
            }
            resource.createDirectory();
            return true;
        } catch(IOException e) {
            throw new RuntimeException("Could not create directory " + resource, e);
        }
    }

    @Override @Deprecated public boolean mkDirs(@NonNull String dn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, dn);
        try {
            final boolean created = !resource.exists();
            resource.createDirectory(true);
            return created;
        } catch(IOException e) {
            throw new RuntimeException("Could not create directory " + resource, e);
        }
    }

    @Override public boolean rmdir(@NonNull String dn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, dn);
        try {
            resource.delete(true);
            return true;
        } catch(IOException e) {
            throw new RuntimeException("Could not delete directory " + resource, e);
        }
    }

    @Override public boolean exists(@NonNull String fn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, fn);
        try {
            return resource.exists();
        } catch(IOException e) {
            throw new RuntimeException("Could not check if resource " + resource + " exists", e);
        }
    }

    @Override public boolean readable(@NonNull String fn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, fn);
        try {
            return resource.isReadable();
        } catch(IOException e) {
            throw new RuntimeException("Could not check if resource " + resource + " is readable", e);
        }
    }

    @Override public boolean writable(@NonNull String fn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, fn);
        try {
            return resource.isWritable();
        } catch(IOException e) {
            throw new RuntimeException("Could not check if resource " + resource + " is writable", e);
        }
    }

    @Override public boolean isDirectory(@NonNull String dn) {
        final HierarchicalResource resource = resourceService.appendOrReplaceWithHierarchical(workingDir, dn);
        try {
            final HierarchicalResourceType type = resource.getType();
            return type == HierarchicalResourceType.Directory;
        } catch(IOException e) {
            throw new RuntimeException("Could not check if resource " + resource + " is a directory", e);
        }
    }
}
