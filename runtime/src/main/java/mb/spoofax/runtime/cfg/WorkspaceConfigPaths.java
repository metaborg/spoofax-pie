package mb.spoofax.runtime.cfg;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.term.Terms;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Value.Immutable
@Serial.Version(value = 1L)
public interface WorkspaceConfigPaths extends Serializable {
    @Value.Parameter List<PPath> langSpecConfigFiles();

    @Value.Parameter List<PPath> spxCoreLangConfigFiles();

    @Value.Parameter List<PPath> spxCoreLangSpecConfigFiles();
}
