package mb.spoofax.runtime.impl.cfg;

import mb.spoofax.runtime.impl.term.Terms;
import mb.vfs.path.PPath;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static mb.spoofax.runtime.impl.term.Terms.*;

public class LangSpecConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    private final PPath dir;
    private final ArrayList<String> extensions;
    private final @Nullable String name;
    private final @Nullable PPath syntaxMainFile;
    private final @Nullable String syntaxStartSymbol;
    private final ArrayList<PPath> syntaxSignatureFiles;
    private final @Nullable PPath syntaxBasedStylingFile;
    private final ArrayList<PPath> natsNabl2Files;
    private final @Nullable ImmutableStrategoConfig natsStrategoConfig;
    private final @Nullable String natsStrategoStrategyId;


    public LangSpecConfig(IStrategoTerm root, PPath dir) {
        this.dir = dir;

        // @formatter:off
        // Sections([...])
        final IStrategoTerm rootSections = root.getSubterm(0);
        // Sections([LangSpecSec([...])])
        final List<IStrategoTerm> langSpecSections = Terms
            .stream(rootSections)
            .filter((t) -> Terms.isAppl(t, "LangSpecSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .collect(Collectors.toList());
        this.extensions = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "IdentificationSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "FileExtensions")) // FileExtensions(Exts([Ext("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Ext("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .collect(Collectors.toCollection(ArrayList::new));
        this.name = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "InformationSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "Name")) // Name(String("...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        this.syntaxMainFile = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "SyntaxSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxMainFile")) // SyntaxMainFile(Path("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        this.syntaxStartSymbol = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "SyntaxSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxStartSymbolId")) // SyntaxStartSymbol(Id("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        this.syntaxSignatureFiles = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "SyntaxSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxSignatureFiles")) // SyntaxSignatureFiles(Paths([Path("..."), ...]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toCollection(ArrayList::new));
        this.syntaxBasedStylingFile = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "StylingSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxBasedStylingSubSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxBasedStylingFile")) // SyntaxBasedStylingFile(Path("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map(dir::resolve)
            .findFirst()
            .orElse(null);
        this.natsNabl2Files = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "NaTsSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "NaTsNaBL2Files")) // NaTsNaBL2Files(Paths([Path("...")]))
            .flatMap((t) -> stream(t.getSubterm(0).getSubterm(0))) // [Path("..."), ...]
            .map((t) -> asString(t.getSubterm(0)))
            .map(dir::resolve)
            .collect(Collectors.toCollection(ArrayList::new));
        this.natsStrategoConfig = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "NaTsSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "NaTsStrategoConfig")) // NaTsStrategoConfig(...)
            .map((t) -> StrategoConfig.fromTerm(t.getSubterm(0), dir))
            .filter((c) -> c != null)
            .findFirst()
            .orElse(null);
        this.natsStrategoStrategyId = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "NaTsSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "NaTsStrategoStrategyId")) // NaTsStrategoStrategyId(Id("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .findFirst()
            .orElse(null);
        // @formatter:on
    }


    public PPath dir() {
        return dir;
    }

    public ArrayList<String> extensions() {
        return extensions;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable PPath getSyntaxMainFile() {
        return syntaxMainFile;
    }

    public @Nullable String getSyntaxStartSymbol() {
        return syntaxStartSymbol;
    }

    public ArrayList<PPath> getSyntaxSignatureFiles() {
        return syntaxSignatureFiles;
    }
    
    public @Nullable PPath getSyntaxBasedStylingFile() {
        return syntaxBasedStylingFile;
    }

    public ArrayList<PPath> getNaTsNaBL2Files() {
        return natsNabl2Files;
    }

    public @Nullable ImmutableStrategoConfig getNaTsStrategoConfig() {
        return natsStrategoConfig;
    }

    public @Nullable String getNaTsStrategoStrategyId() {
        return natsStrategoStrategyId;
    }


    @Override public String toString() {
        if(name != null) {
            return name;
        }
        return super.toString();
    }
}
