package mb.spoofax.runtime.cfg;

import mb.fs.java.JavaFSPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Serial.Version(value = 1L)
public interface StrategoCompilerConfig extends Serializable {
    @Value.Parameter JavaFSPath mainFile();

    @Value.Parameter List<JavaFSPath> includeDirs();

    @Value.Parameter List<JavaFSPath> includeFiles();

    @Value.Parameter List<String> includeLibs();

    @Value.Parameter @Nullable JavaFSPath baseDir();

    default @Nullable JavaFSPath baseDirOrDefault() {
        final JavaFSPath baseDir = baseDir();
        if(baseDir != null) {
            return baseDir;
        } else {
            // Default to parent of main file.
            return mainFile().getParent();
        }
    }

    @Value.Parameter @Nullable JavaFSPath cacheDir();

    default @Nullable JavaFSPath cacheDirOrDefault() {
        final JavaFSPath cacheDir = cacheDir();
        if(cacheDir != null) {
            return cacheDir;
        } else {
            final JavaFSPath parent = mainFile().getParent();
            if(parent != null) {
                // Default to <parent of main file>/target/str-cache.
                return parent.appendSegments("target", "str-cache");
            }
            return null;
        }
    }

    @Value.Parameter @Nullable JavaFSPath outputFile();

    default JavaFSPath outputFileOrDefault() {
        final JavaFSPath outputFile = outputFile();
        if(outputFile != null) {
            return outputFile;
        } else {
            // Default to <main file>.ctree.
            return mainFile().appendToLeaf(".ctree");
        }
    }
}
