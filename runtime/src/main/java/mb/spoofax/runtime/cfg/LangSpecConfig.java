package mb.spoofax.runtime.cfg;

import mb.pie.vfs.path.PPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Serial.Version(value = 1L)
public interface LangSpecConfig extends Serializable {
    @Value.Parameter PPath dir();


    @Value.Parameter List<String> extensions();

    default String firstExtension() {
        return extensions().get(0);
    }


    @Value.Parameter @Nullable String name();


    @Value.Parameter List<PPath> syntaxParseFiles();

    @Value.Parameter @Nullable PPath syntaxParseMainFile();

    @Value.Parameter @Nullable String syntaxParseStartSymbolId();

    @Value.Parameter List<PPath> syntaxSignatureFiles();

    @Value.Parameter @Nullable PPath syntaxStyleFile();


    @Value.Parameter List<PPath> natsNaBL2Files();

    @Value.Parameter @Nullable ImmutableStrategoCompilerConfig natsStrategoConfig();

    @Value.Parameter @Nullable String natsStrategoStrategyId();

    @Value.Parameter boolean natsRootScopePerFile();
}
