package mb.pipe.run.spoofax.esv;


import java.util.List;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.ParentAttachment;

import com.google.common.collect.Lists;

import mb.pipe.run.core.model.parse.IToken;
import mb.pipe.run.core.model.style.IStyle;
import mb.pipe.run.core.model.style.IStyling;
import mb.pipe.run.core.model.style.ITokenStyle;
import mb.pipe.run.core.model.style.Styling;
import mb.pipe.run.core.model.style.TokenStyle;

public class Styler {
    private final StylingRules rules;


    public Styler(StylingRules rules) {
        this.rules = rules;
    }


    public IStyling style(Iterable<IToken> tokenStream) {
        final List<ITokenStyle> tokenStyles = Lists.newArrayList();
        for(IToken token : tokenStream) {
            final IStyle style = tokenStyle(token);
            if(style != null) {
                tokenStyles.add(new TokenStyle(token, style));
            }
        }
        return new Styling(tokenStyles);
    }


    private @Nullable IStyle tokenStyle(IToken token) {
        final IStrategoTerm term = token.associatedTerm();
        if(term != null) {
            final IStyle style = termStyle(term);
            if(style != null) {
                return style;
            }
        }

        return rules.tokenTypeStyle(token.type());
    }

    private @Nullable IStyle termStyle(IStrategoTerm term) {
        final int termType = term.getTermType();
        if(termType != IStrategoTerm.APPL && termType != IStrategoTerm.TUPLE && termType != IStrategoTerm.LIST) {
            // Try to use the parent of terminal nodes, mimicking behavior of old Spoofax/IMP runtime.
            final IStrategoTerm parentTerm = ParentAttachment.getParent(term);
            if(parentTerm != null) {
                final IStyle style = sortConsStyle(parentTerm);
                if(style != null) {
                    return style;
                }
            }
        }

        return sortConsStyle(term);
    }

    private @Nullable IStyle sortConsStyle(IStrategoTerm term) {
        if(term.getTermType() != IStrategoTerm.APPL) {
            return null;
        }

        final ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
        final String sort = imploderAttachment.getSort();
        if(sort == null) {
            return null;
        }

        // LEGACY: for some reason, when using concrete syntax extensions, all sorts are appended with _sort.
        final String massagedSort = sort.replace("_sort", "");

        final String cons = ((IStrategoAppl) term).getConstructor().getName();
        if(rules.hasSortConsStyle(massagedSort, cons)) {
            return rules.sortConsStyle(massagedSort, cons);
        } else if(rules.hasConsStyle(cons)) {
            return rules.consStyle(cons);
        } else if(rules.hasSortStyle(massagedSort)) {
            return rules.sortStyle(massagedSort);
        }

        return null;
    }
}
