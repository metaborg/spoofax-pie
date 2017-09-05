package mb.spoofax.runtime.impl.esv;

import java.util.List;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.ParentAttachment;

import com.google.common.collect.Lists;

import mb.spoofax.runtime.model.parse.Token;
import mb.spoofax.runtime.model.style.Style;
import mb.spoofax.runtime.model.style.Styling;
import mb.spoofax.runtime.model.style.StylingImpl;
import mb.spoofax.runtime.model.style.TokenStyle;
import mb.spoofax.runtime.model.style.TokenStyleImpl;

public class Styler {
    private final StylingRules rules;


    public Styler(StylingRules rules) {
        this.rules = rules;
    }


    public Styling style(Iterable<Token> tokenStream) {
        final List<TokenStyle> tokenStyles = Lists.newArrayList();
        for(Token token : tokenStream) {
            final Style style = tokenStyle(token);
            if(style != null) {
                tokenStyles.add(new TokenStyleImpl(token, style));
            }
        }
        return new StylingImpl(tokenStyles);
    }


    private @Nullable Style tokenStyle(Token token) {
        final IStrategoTerm term = token.associatedTerm();
        if(term != null) {
            final Style style = termStyle(term);
            if(style != null) {
                return style;
            }
        }

        return rules.tokenTypeStyle(token.type());
    }

    private @Nullable Style termStyle(IStrategoTerm term) {
        final int termType = term.getTermType();
        if(termType != IStrategoTerm.APPL && termType != IStrategoTerm.TUPLE && termType != IStrategoTerm.LIST) {
            // Try to use the parent of terminal nodes, mimicking behavior of old Spoofax/IMP runtime.
            final IStrategoTerm parentTerm = ParentAttachment.getParent(term);
            if(parentTerm != null) {
                final Style style = sortConsStyle(parentTerm);
                if(style != null) {
                    return style;
                }
            }
        }

        return sortConsStyle(term);
    }

    private @Nullable Style sortConsStyle(IStrategoTerm term) {
        final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
        if(imploderAttachment == null) {
            return null;
        }
        final String sort = imploderAttachment.getSort();
        if(sort == null) {
            return null;
        }

        // LEGACY: for some reason, when using concrete syntax extensions, all sorts are appended with _sort.
        final String massagedSort = sort.replace("_sort", "");

        if(term.getTermType() == IStrategoTerm.APPL) {
            final String cons = ((IStrategoAppl) term).getConstructor().getName();
            if(rules.hasSortConsStyle(massagedSort, cons)) {
                return rules.sortConsStyle(massagedSort, cons);
            } else if(rules.hasConsStyle(cons)) {
                return rules.consStyle(cons);
            } else if(rules.hasSortStyle(massagedSort)) {
                return rules.sortStyle(massagedSort);
            }
        }
        
        if(rules.hasSortStyle(massagedSort)) {
            return rules.sortStyle(massagedSort);
        }

        return null;
    }
}
