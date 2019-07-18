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
    public static String toString(IStrategoTerm term) {
        if(term instanceof IStrategoString) {
            return ((IStrategoString) term).stringValue();
        } else {
            final @Nullable IStrategoString pp = prettyPrintTerm(term);
            if(pp != null) {
                return pp.stringValue();
            } else {
                return term.toString();
            }
        }
    }

    private static @Nullable IStrategoString prettyPrintTerm(IStrategoTerm term) {
        return prettyPrintTerm(term, 120);
    }

    private static @Nullable IStrategoString prettyPrintTerm(IStrategoTerm term, int maxWidth) {
        final Context context = org.strategoxt.stratego_aterm.Main.init();
        term = aterm_escape_strings_0_0.instance.invoke(context, term);
        term = pp_aterm_box_0_0.instance.invoke(context, term);
        term = box2text_string_0_1.instance.invoke(context, term, context.getFactory().makeInt(maxWidth));
        return (IStrategoString) term;
    }


    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm term, IStrategoTerm ast, String fileString, String dirString) {
        return termFactory.makeTuple(term, termFactory.makeList(), ast, termFactory.makeString(fileString), termFactory.makeString(dirString));
    }

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, String fileString, String dirString) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, fileString, dirString);
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

    public static IStrategoTerm createLegacyBuilderInputTerm(ITermFactory termFactory, IStrategoTerm ast, ResourcePath path) {
        return createLegacyBuilderInputTerm(termFactory, ast, ast, path);
    }
}
