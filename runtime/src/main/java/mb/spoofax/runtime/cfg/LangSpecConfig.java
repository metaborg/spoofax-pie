package mb.spoofax.runtime.cfg;

import mb.pie.vfs.path.PPath;
import mb.spoofax.runtime.term.Terms;
import org.immutables.serial.Serial;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

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


    static LangSpecConfig fromTerm(IStrategoTerm root, PPath dir) {
        // @formatter:off
        // Sections([...])
        final IStrategoTerm rootSections = root.getSubterm(0);
        // Sections([LangSpecSec([...])])
        final List<IStrategoTerm> langSpecSections = Terms
            .stream(rootSections)
            .filter((t) -> Terms.isAppl(t, "LangSpecSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        // Identification
        final List<String> extensions = langSpecSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "IdentificationSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .filter((t) -> Terms.isAppl(t, "FileExtensions")) // FileExtensions(Exts([Ext("..."), ...]))
            .flatMap((t) -> Terms.stream(t.getSubterm(0).getSubterm(0))) // [Ext("..."), ...]
            .map((t) -> Terms.asString(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        // Information
        final @Nullable String name = langSpecSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "InformationSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .filter((t) -> Terms.isAppl(t, "Name")) // Name(String("...))
            .map((t) -> Terms.asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        
        // Syntax
        final List<IStrategoTerm> syntaxSubSections = langSpecSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        // Syntax - Parsing
        final List<IStrategoTerm> syntaxParseOpts = syntaxSubSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxParseSubSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        final List<PPath> syntaxFiles = syntaxParseOpts
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxParseFiles")) // SyntaxParseFiles([Path("..."), ...]))
            .flatMap((t) -> Terms.stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> Terms.asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toList());
        final @Nullable PPath syntaxParseMainFile = syntaxParseOpts
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxParseMainFile")) // SyntaxParseMainFile(Path("..."))
            .map((t) -> Terms.asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        final @Nullable String syntaxParseStartSymbolId = syntaxParseOpts
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxParseStartSymbolId")) // SyntaxParseStartSymbolId(Id("..."))
            .map((t) -> Terms.asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        
        // Syntax - Signatures
        final List<PPath> syntaxSignatureFiles = syntaxSubSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxSignatureSubSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .filter((t) -> Terms.isAppl(t, "SignatureSyntaxFiles")) // SignatureSyntaxFiles(Paths([Path("..."), ...]))
            .flatMap((t) -> Terms.stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> Terms.asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toList());
        
        // Syntax - Styling
        final @Nullable PPath syntaxStyleFile = syntaxSubSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "SyntaxStyleSubSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .filter((t) -> Terms.isAppl(t, "SyntaxStyleFile")) // SyntaxStyleFile(Path("..."))
            .map((t) -> Terms.asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        
        // NaTs
        final List<IStrategoTerm> natsOpts = langSpecSections
            .stream()
            .filter((t) -> Terms.isAppl(t, "NaTsSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        
        final List<PPath> natsNaBL2Files = natsOpts
            .stream()
            .filter((t) -> Terms.isAppl(t, "NaTsNaBL2Files")) // NaTsNaBL2Files(Paths([Path("...")]))
            .flatMap((t) -> Terms.stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> Terms.asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toList());
        final @Nullable ImmutableStrategoConfig natsStrategoConfig = natsOpts
            .stream()
            .filter((t) -> Terms.isAppl(t, "NaTsStrategoConfig")) // NaTsStrategoConfig(...)
            .map((t) -> StrategoConfig.fromTerm(t.getSubterm(0), dir))
            .filter((c) -> c != null)
            .findFirst()
            .orElse(null);
        final @Nullable String natsStrategoStrategyId = natsOpts
            .stream()
            .filter((t) -> Terms.isAppl(t, "NaTsStrategoStrategyId")) // NaTsStrategoStrategyId(Id("..."))
            .map((t) -> Terms.asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        final boolean natsRootScopePerFile = natsOpts.stream()
            .filter((t) -> Terms.isAppl(t, "NaTsSec"))
            .flatMap((t) -> Terms.stream(t.getSubterm(0)))
            .filter((t) -> Terms.isAppl(t, "NaTsRootScopePerFile")) // NaTsRootScopePerFile(Bool(True()|False()))
            .map((t) -> Terms.asAppl(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .map((t) -> {
                if(Terms.hasCons(t, 0, "True")) return true;
                if(Terms.hasCons(t, 0, "False")) return false;
                return false;
            })
            .orElse(false);
        // @formatter:on

        return ImmutableLangSpecConfig.of(dir, extensions, name, syntaxFiles, syntaxParseMainFile,
            syntaxParseStartSymbolId, syntaxSignatureFiles, syntaxStyleFile, natsNaBL2Files, natsStrategoConfig,
            natsStrategoStrategyId, natsRootScopePerFile);
    }
}
