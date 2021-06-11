package mb.stratego.common;

import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

public class StrategoUtil {
    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm term, IStrategoTerm ast, String fileString, String dirString) {
        return termFactory.makeTuple(term, termFactory.makeList(), ast, termFactory.makeString(fileString), termFactory.makeString(dirString));
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, String fileString, String dirString) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, fileString, dirString);
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, ResourcePath file, ResourcePath dir) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, dir.relativize(file), dir.toString());
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, ResourcePath path) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, path);
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm term, IStrategoTerm ast, ResourcePath path) {
        final @Nullable ResourcePath parent = path.getParent();
        final @Nullable String leaf = path.getLeaf();
        if(parent != null && leaf != null) {
            return createLegacyBuilderInputTerm(termFactory, term, ast, leaf, parent.toString());
        } else if(leaf != null) {
            return createLegacyBuilderInputTerm(termFactory, term, ast, leaf, "");
        } else if(parent != null) {
            return createLegacyBuilderInputTerm(termFactory, term, ast, "", parent.toString());
        } else {
            return createLegacyBuilderInputTerm(termFactory, term, ast, "", "");
        }
    }
}
