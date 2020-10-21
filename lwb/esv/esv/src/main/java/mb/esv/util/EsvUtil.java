package mb.esv.util;

import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.resource.hierarchical.walk.TrueResourceWalker;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.List;

public class EsvUtil {
    public static ResourceWalker createResourceWalker() {
        return new TrueResourceWalker();
    }

    public static ResourceMatcher createResourceMatcher() {
        return new PathResourceMatcher(new ExtensionsPathMatcher("esv"));
    }


    public static boolean isModuleTerm(IStrategoTerm term) {
        return TermUtils.isAppl(term, "Module", 3);
    }

    public static String getNameFromModuleTerm(IStrategoTerm term) {
        return TermUtils.toJavaStringAt(term, 0);
    }

    public static List<IStrategoTerm> getSectionsFromModuleTerm(IStrategoTerm term) {
        return term.getSubterm(2).getSubterms();
    }

    public static boolean isImportsTerm(IStrategoTerm term) {
        return TermUtils.isAppl(term, "Imports", 1);
    }

    public static String getNameFromImportTerm(IStrategoTerm term) {
        return TermUtils.toJavaStringAt(term, 0);
    }
}
