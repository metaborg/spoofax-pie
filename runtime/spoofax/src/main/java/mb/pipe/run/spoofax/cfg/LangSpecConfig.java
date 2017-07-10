package mb.pipe.run.spoofax.cfg;

import static mb.pipe.run.spoofax.term.Terms.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.path.PPath;
import mb.pipe.run.spoofax.term.Terms;

public class LangSpecConfig implements Serializable {
    private static final long serialVersionUID = 2L;

    private final ArrayList<String> extensions;
    private final String name;
    private final PPath syntaxMainFile;
    private final String syntaxStartSymbol;
    private final PPath syntaxBasedStylingFile;


    public LangSpecConfig(IStrategoTerm root, PPath dir) {
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
            .filter((t) -> isAppl(t, "FileExtension")) // FileExtension("...")
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
            .filter((t) -> isAppl(t, "SyntaxMainFile")) // SyntaxMainFile(Path("...))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map((s) -> dir.resolve(s))
            .findFirst()
            .orElse(null);
        this.syntaxStartSymbol = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "SyntaxSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxStartSymbol")) // SyntaxStartSymbol("...")
            .map((t) -> asString(t.getSubterm(0)))
            .findFirst()
            .orElse(null);
        this.syntaxBasedStylingFile = langSpecSections
            .stream()
            .filter((t) -> isAppl(t, "StylingSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxBasedStylingSubSec"))
            .flatMap((t) -> stream(t.getSubterm(0)))
            .filter((t) -> isAppl(t, "SyntaxBasedStylingFile")) // SyntaxBasedStylingFile(Path("..."))
            .map((t) -> asString(t.getSubterm(0).getSubterm(0)))
            .map((s) -> dir.resolve(s))
            .findFirst()
            .orElse(null);
        // @formatter:on
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

    public @Nullable PPath getSyntaxBasedStylingFile() {
        return syntaxBasedStylingFile;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + extensions.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((syntaxMainFile == null) ? 0 : syntaxMainFile.hashCode());
        result = prime * result + ((syntaxStartSymbol == null) ? 0 : syntaxStartSymbol.hashCode());
        result = prime * result + ((syntaxBasedStylingFile == null) ? 0 : syntaxBasedStylingFile.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final LangSpecConfig other = (LangSpecConfig) obj;
        if(!extensions.equals(other.extensions))
            return false;
        if(name == null) {
            if(other.name != null)
                return false;
        } else if(!name.equals(other.name))
            return false;
        if(syntaxMainFile == null) {
            if(other.syntaxMainFile != null)
                return false;
        } else if(!syntaxMainFile.equals(other.syntaxMainFile))
            return false;
        if(syntaxStartSymbol == null) {
            if(other.syntaxStartSymbol != null)
                return false;
        } else if(!syntaxStartSymbol.equals(other.syntaxStartSymbol))
            return false;
        if(syntaxBasedStylingFile == null) {
            if(other.syntaxBasedStylingFile != null)
                return false;
        } else if(!syntaxBasedStylingFile.equals(other.syntaxBasedStylingFile))
            return false;
        return true;
    }

    @Override public String toString() {
        if(name != null) {
            return name;
        }
        return super.toString();
    }
}
