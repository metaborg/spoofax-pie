package mb.aterm.common;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.stratego_aterm.aterm_escape_strings_0_0;
import org.strategoxt.stratego_aterm.pp_aterm_box_0_0;
import org.strategoxt.stratego_gpp.box2text_string_0_1;

public class TermToString {
    private static final int ppWidthDefault = 120;

    public static String toString(IStrategoTerm term) {
        return toString(term, ppWidthDefault);
    }

    public static String toString(IStrategoTerm term, int ppWidth) {
        if(term instanceof IStrategoString) {
            return ((IStrategoString)term).stringValue();
        } else {
            final @Nullable IStrategoString pp = prettyPrintTerm(term, ppWidth);
            if(pp != null) {
                return pp.stringValue();
            } else {
                return term.toString();
            }
        }
    }

    private static @Nullable IStrategoString prettyPrintTerm(IStrategoTerm term) {
        return prettyPrintTerm(term, ppWidthDefault);
    }

    private static @Nullable IStrategoString prettyPrintTerm(IStrategoTerm term, int ppWidth) {
        final Context context = org.strategoxt.stratego_aterm.Main.init();
        @Nullable IStrategoTerm transformedTerm = term;
        transformedTerm = aterm_escape_strings_0_0.instance.invoke(context, transformedTerm);
        transformedTerm = pp_aterm_box_0_0.instance.invoke(context, transformedTerm);
        transformedTerm = box2text_string_0_1.instance.invoke(context, transformedTerm, context.getFactory().makeInt(ppWidth));
        if(transformedTerm == null) {
            return null;
        } else {
            return (IStrategoString)transformedTerm;
        }
    }


    public static String toShortString(IStrategoTerm term) {
        switch(term.getType()) {
            case APPL:
                return ((IStrategoAppl)term).getConstructor().toString();
            case LIST:
                return "[" + term.getSubtermCount() + " subterms]";
            case TUPLE:
                return "(" + term.getSubtermCount() + " subterms)";
            default:
                return term.toString();
        }
    }
}
