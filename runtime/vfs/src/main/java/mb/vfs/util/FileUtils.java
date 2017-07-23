package mb.vfs.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Pattern;

public class FileUtils {
    private static Pattern sanitizePattern = Pattern.compile("[^a-zA-Z0-9.-]");


    public static String sanitize(String path) {
        return sanitizePattern.matcher(path).replaceAll("_");
    }

    public static void deleteDirectory(Path path) throws IOException {
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
}
