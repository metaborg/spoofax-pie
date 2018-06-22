package mb.spoofax.runtime.cfg;

import mb.pie.vfs.path.PPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Serial.Version(value = 1L)
public interface StrategoCompilerConfig extends Serializable {
    @Value.Parameter PPath mainFile();

    @Value.Parameter List<PPath> includeDirs();

    @Value.Parameter List<PPath> includeFiles();

    @Value.Parameter List<String> includeLibs();

    @Value.Parameter @Nullable PPath baseDir();

    default @Nullable PPath baseDirOrDefault() {
        final PPath baseDir = baseDir();
        if(baseDir != null) {
            return baseDir;
        } else {
            // Default to parent of main file.
            return mainFile().parent();
        }
    }

    @Value.Parameter @Nullable PPath cacheDir();

    default @Nullable PPath cacheDirOrDefault() {
        final PPath cacheDir = cacheDir();
        if(cacheDir != null) {
            return cacheDir;
        } else {
            final PPath parent = mainFile().parent();
            if(parent != null) {
                // Default to <parent of main file>/target/str-cache.
                return parent.resolve("target/str-cache");
            }
            return null;
        }
    }

    @Value.Parameter @Nullable PPath outputFile();

    default PPath outputFileOrDefault() {
        final PPath outputFile = outputFile();
        if(outputFile != null) {
            return outputFile;
        } else {
            // Default to <main file>.ctree.
            return mainFile().extend(".ctree");
        }
    }
}
