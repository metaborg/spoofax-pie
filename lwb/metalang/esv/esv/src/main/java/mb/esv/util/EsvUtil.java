package mb.esv.util;

import mb.common.util.ListView;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.match.path.string.ExtensionsPathStringMatcher;
import mb.resource.hierarchical.match.path.string.PathStringMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import java.util.List;

public class EsvUtil {
    public static final String displayName = "ESV";

    public static final String fileExtension = "esv";
    public static final ListView<String> fileExtensions = ListView.of(fileExtension);
    public static final String[] fileExtensionsArray = fileExtensions.toArray(new String[0]);
    public static final ExtensionsPathMatcher extensionsPathMatcher = PathMatcher.ofExtensions(fileExtensionsArray);
    public static final ExtensionsPathStringMatcher extensionsPathStringMatcher = PathStringMatcher.ofExtensions(fileExtensionsArray);

    public static final ResourceMatcher fileMatcher = ResourceMatcher
        .ofPath(extensionsPathMatcher)
        .and(ResourceMatcher.ofFile());
    public static final ResourceMatcher directoryMatcher = ResourceMatcher.ofDirectory();
    public static final ResourceWalker resourceWalker = ResourceWalker
        .ofNoHidden()
        .and(ResourceWalker.ofNot(ResourceWalker.ofPath(PathMatcher.ofStartsWith("bin")))); // HACK: ignore bin directory in root directory


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
