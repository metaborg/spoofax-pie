package mb.stratego.common;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
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

    public static @Nullable IStrategoString prettyPrintTerm(IStrategoTerm term) {
        return prettyPrintTerm(term, 120);
    }

    public static @Nullable IStrategoString prettyPrintTerm(IStrategoTerm term, int maxWidth) {
        final Context context = org.strategoxt.stratego_aterm.Main.init();
        term = aterm_escape_strings_0_0.instance.invoke(context, term);
        term = pp_aterm_box_0_0.instance.invoke(context, term);
        term = box2text_string_0_1.instance.invoke(context, term, context.getFactory().makeInt(maxWidth));
        return (IStrategoString) term;
    }
}
