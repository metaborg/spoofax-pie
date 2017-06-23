package mb.pipe.run.spoofax.cfg;

import static mb.pipe.run.spoofax.term.Terms.*;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pipe.run.core.path.PPath;

public class LangSpecConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final IStrategoTerm root;
    private final PPath location;


    public LangSpecConfig(IStrategoTerm root, PPath location) {
        this.root = root;
        this.location = location;
    }


    public ArrayList<String> extensions() {
        final ArrayList<String> extensions = new ArrayList<>();
        // Sections([IdentificationSec([...])])
        for(IStrategoAppl option : getOptions(getSectionContents("IdentificationSec"), 1, "FileExtension")) {
            // FileExtension("...")
            extensions.add(asString(option.getSubterm(0)));
        }
        return extensions;
    }

    public @Nullable String getName() {
        // Sections([InformationSec([...])])
        final IStrategoAppl option = getOption(getSectionContents("InformationSec"), 1, "Name");
        if(option == null) {
            return null;
        }
        // Name(String("...))
        return asString(option.getSubterm(0).getSubterm(0));
    }

    public @Nullable PPath getSyntaxMainFile() {
        // Sections([SyntaxSec([...])])
        final IStrategoAppl option = getOption(getSectionContents("SyntaxSec"), 1, "SyntaxMainFile");
        if(option == null) {
            return null;
        }
        // SyntaxMainFile(Path("...))
        final String str = asString(option.getSubterm(0).getSubterm(0));
        return location.resolve(str);
    }
    
    public @Nullable String getSyntaxStartSymbol() {
        // Sections([SyntaxSec([...])])
        final IStrategoAppl option = getOption(getSectionContents("SyntaxSec"), 1, "SyntaxStartSymbol");
        if(option == null) {
            return null;
        }
        // SyntaxStartSymbol("...")
        return asString(option.getSubterm(0));
    }

    public @Nullable PPath getSyntaxBasedStylingFile() {
        // Sections([StylingSec([SyntaxBasedStylingSubSec([...])])])
        final IStrategoAppl option =
            getOption(getSubSectionContents("StylingSec", "SyntaxBasedStylingSubSec"), 1, "SyntaxBasedStylingFile");
        if(option == null) {
            return null;
        }
        // SyntaxBasedStylingFile(Path("..."))
        final String str = asString(option.getSubterm(0).getSubterm(0));
        return location.resolve(str);
    }


    private ArrayList<IStrategoTerm> getSectionContents(String secConsName) {
        final ArrayList<IStrategoTerm> contents = new ArrayList<>();
        // Sections([...])
        for(IStrategoTerm section : root.getSubterm(0)) {
            if(isAppl(section, secConsName)) {
                for(IStrategoTerm content : section.getSubterm(0)) {
                    contents.add(content);
                }
            }
        }
        return contents;
    }

    private ArrayList<IStrategoTerm> getSubSectionContents(String secConsName, String subSecConsName) {
        final ArrayList<IStrategoTerm> contents = new ArrayList<>();
        for(IStrategoTerm section : root.getSubterm(0)) {
            if(isAppl(section, secConsName)) {
                for(IStrategoTerm subSection : section.getSubterm(0)) {
                    if(isAppl(subSection, subSecConsName)) {
                        for(IStrategoTerm content : subSection.getSubterm(0)) {
                            contents.add(content);
                        }
                    }
                }
            }
        }
        return contents;
    }


    private static @Nullable IStrategoAppl getOption(Iterable<IStrategoTerm> contents, int arity, String cons) {
        for(IStrategoTerm option : contents) {
            if(isAppl(option, arity, cons)) {
                return asAppl(option);
            }
        }
        return null;
    }

    private static ArrayList<IStrategoAppl> getOptions(Iterable<IStrategoTerm> contents, int arity, String cons) {
        final ArrayList<IStrategoAppl> options = new ArrayList<>();
        for(IStrategoTerm option : contents) {
            if(isAppl(option, arity, cons)) {
                options.add(asAppl(option));
            }
        }
        return options;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + root.hashCode();
        result = prime * result + location.hashCode();
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
        if(!location.equals(other.location))
            return false;
        if(!root.equals(other.root))
            return false;
        return true;
    }

    @Override public String toString() {
        final String name = getName();
        if(name != null) {
            return name;
        }
        return super.toString();
    }
}
