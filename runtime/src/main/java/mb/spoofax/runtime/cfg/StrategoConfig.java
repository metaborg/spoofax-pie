package mb.spoofax.runtime.cfg;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.term.Terms;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
@Serial.Version(value = 1L)
public interface StrategoConfig extends Serializable {
    @Value.Parameter PPath mainFile();

    @Value.Parameter List<PPath> includeDirs();

    @Value.Parameter List<PPath> includeFiles();

    @Value.Parameter List<String> includeLibs();

    @Value.Parameter PPath baseDir();

    @Value.Parameter PPath cacheDir();

    @Value.Parameter PPath outputFile();
}
