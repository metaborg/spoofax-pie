package mb.pipe.run.core.vfs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;

import com.google.common.collect.Lists;

public class FileUtils {
    private static Pattern sanitizePattern = Pattern.compile("[^a-zA-Z0-9.-]");


    public static File toFile(FileObject fileObject) {
        final URI uri = toURI(fileObject);
        final File file = new File(uri);
        return file;
    }

    public static Iterable<File> toFiles(Iterable<FileObject> fileObjects) {
        final Collection<File> files = Lists.newLinkedList();
        for(FileObject fileObject : fileObjects) {
            files.add(toFile(fileObject));
        }
        return files;
    }

    public static URI toURI(FileObject fileObject) {
        try {
            final FileName name = fileObject.getName();
            final String uriString = URIEncode.encode(name.getURI());
            return new URI(uriString);
        } catch(URISyntaxException e) {
            throw new RuntimeException("Could not convert FileObject to URI", e);
        }
    }

    public static String sanitize(String path) {
        return sanitizePattern.matcher(path).replaceAll("_");
    }
}
