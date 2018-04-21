package mb.spoofax.runtime.impl.cfg;

import mb.spoofax.runtime.impl.term.Terms;
import mb.vfs.path.PPath;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static mb.spoofax.runtime.impl.term.Terms.*;

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

    @Value.Parameter @Nullable ImmutableStrategoConfig natsStrategoConfig();

    @Value.Parameter @Nullable String natsStrategoStrategyId();

    @Value.Parameter boolean natsRootScopePerFile();


    public static LangSpecConfig fromTerm(IStrategoTerm root, PPath dir) {
        // @formatter:off
        // Sections([...])
        final IStrategoTerm rootSections = root.getSubterm(0);
        // Sections([LangSpecSec([...])])
        final List<IStrategoTerm> langSpecSections = Terms
            .stream(rootSections)
            .filter((t) -> Terms.isAppl(t, "LangSpecSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        // Identification
        final List<String> extensions = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "IdentificationSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "FileExtensions")) // FileExtensions(Exts([Ext("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Ext("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        // Information
        final @Nullable String name = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "InformationSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "Name")) // Name(String("...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        
        // Syntax
        final List<IStrategoTerm> syntaxSubSections = langSpecSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        // Syntax - Parsing
        final List<IStrategoTerm> syntaxParseOpts = syntaxSubSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxParseSubSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        final List<PPath> syntaxFiles = syntaxParseOpts
            .stream()
            .filter((t) -> isAppl(t, "SyntaxParseFiles")) // SyntaxParseFiles([Path("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toList());
        final @Nullable PPath syntaxParseMainFile = syntaxParseOpts
            .stream()
            .filter((t) -> isAppl(t, "SyntaxParseMainFile")) // SyntaxParseMainFile(Path("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        final @Nullable String syntaxParseStartSymbolId = syntaxParseOpts
            .stream()
            .filter((t) -> isAppl(t, "SyntaxParseStartSymbolId")) // SyntaxParseStartSymbolId(Id("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        
        // Syntax - Signatures
        final List<PPath> syntaxSignatureFiles = syntaxSubSections
            .stream()
            .filter((t) -> isAppl(t, "SyntaxSignatureSubSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SignatureSyntaxFiles")) // SignatureSyntaxFiles(Paths([Path("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toList());
        
        // Syntax - Styling
        final @Nullable PPath syntaxStyleFile = syntaxSubSections
            .stream()
            .filter((t) -> isAppl(t, "SyntaxStyleSubSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxStyleFile")) // SyntaxStyleFile(Path("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        
        // NaTs
        final List<IStrategoTerm> natsOpts = langSpecSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "NaTsSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        final List<PPath> natsNaBL2Files = natsOpts
            .stream()
            .filter((t) -> isAppl(t, "NaTsNaBL2Files")) // NaTsNaBL2Files(Paths([Path("...")]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toList());
        final @Nullable ImmutableStrategoConfig natsStrategoConfig = natsOpts
            .stream()
            .filter((t) -> isAppl(t, "NaTsStrategoConfig")) // NaTsStrategoConfig(...)
            .map((t) -> StrategoConfig.fromTerm(t.getSubterm(0), dir))
            .filter((c) -> c != null)
            .findFirst()
            .orElse(null);
        final @Nullable String natsStrategoStrategyId = natsOpts
            .stream()
            .filter((t) -> isAppl(t, "NaTsStrategoStrategyId")) // NaTsStrategoStrategyId(Id("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        final boolean natsRootScopePerFile = natsOpts.stream()
            .filter((t) -> isAppl(t, "NaTsSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "NaTsRootScopePerFile")) // NaTsRootScopePerFile(Bool(True()|False()))
            .map((t) -> asAppl(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .map((t) -> {
                if(hasCons(t, 0, "True")) return true;
                if(hasCons(t, 0, "False")) return false;
                return false;
            })
            .orElse(false);
        // @formatter:on

        return ImmutableLangSpecConfig.of(dir, extensions, name, syntaxFiles, syntaxParseMainFile,
            syntaxParseStartSymbolId, syntaxSignatureFiles, syntaxStyleFile, natsNaBL2Files, natsStrategoConfig,
            natsStrategoStrategyId, natsRootScopePerFile);
    }
}
