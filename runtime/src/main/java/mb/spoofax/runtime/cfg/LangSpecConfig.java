package mb.spoofax.runtime.cfg;

import mb.fs.api.path.FSPath;
import mb.fs.java.JavaFSPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

@Value.Immutable
@Serial.Version(value = 1L)
public interface LangSpecConfig extends Serializable {
    @Value.Parameter JavaFSPath dir();


    @Value.Parameter LangId id();


    @Value.Parameter List<String> extensions();

    default String firstExtension() {
        return extensions().get(0);
    }


    @Value.Parameter @Nullable String name();


    @Value.Parameter List<JavaFSPath> syntaxParseFiles();

    @Value.Parameter @Nullable JavaFSPath syntaxParseMainFile();

    @Value.Parameter @Nullable String syntaxParseStartSymbolId();

    @Value.Parameter List<JavaFSPath> syntaxSignatureFiles();

    @Value.Parameter @Nullable JavaFSPath syntaxStyleFile();


    @Value.Parameter List<JavaFSPath> natsNaBL2Files();

    @Value.Parameter @Nullable ImmutableStrategoCompilerConfig natsStrategoConfig();

    @Value.Parameter @Nullable String natsStrategoStrategyId();

    @Value.Parameter boolean natsRootScopePerFile();
}
