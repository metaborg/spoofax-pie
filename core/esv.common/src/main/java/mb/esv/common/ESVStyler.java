package mb.esv.common;

import mb.common.region.Region;
import mb.common.style.*;
import mb.common.token.Token;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.ParentAttachment;

import java.util.ArrayList;

public class ESVStyler {
    private final ESVStylingRules rules;
    private final Logger logger;


    public ESVStyler(ESVStylingRules rules, LoggerFactory loggerFactory) {
        this.rules = rules;
        this.logger = loggerFactory.create(getClass());
    }


    public Styling style(Iterable<? extends Token<IStrategoTerm>> tokens) {
        final ArrayList<TokenStyle> tokenStyles = new ArrayList<>();
        for(Token<IStrategoTerm> token : tokens) {
            final @Nullable Style style = tokenStyle(token);
            if(style != null) {
                tokenStyles.add(new TokenStyleImpl(token, style));
            }
        }
        int offset = -1;
        final ArrayList<TokenStyle> validated = new ArrayList<>();
        for(TokenStyle tokenStyle : tokenStyles) {
            final Region region = tokenStyle.getToken().getRegion();
            if(offset >= region.getStartOffset()) {
                logger.warn("Invalid {}, starting offset is greater than offset in previous regions, "
                    + "token style will be skipped", tokenStyle);
            } else if(offset >= region.getEndOffsetInclusive()) {
                logger.warn("Invalid {}, ending offset is greater than offset in previous regions, "
                    + "token style will be skipped", tokenStyle);
            } else if(region.getStartOffset() > region.getEndOffsetInclusive()) {
                logger.warn("Invalid {}, starting offset is greater than ending offset, "
                    + "token style will be skipped", tokenStyle);
            } else {
                validated.add(tokenStyle);
                offset = region.getEndOffsetInclusive();
            }
        }
        return new StylingImpl(validated);
    }


    private @Nullable Style tokenStyle(Token<IStrategoTerm> token) {
        final @Nullable IStrategoTerm term = token.getFragment();
        if(term != null) {
            final @Nullable Style style = termStyle(term);
            if(style != null) {
                return style;
            }
        }

        return rules.tokenTypeStyle(token.getType());
    }

    private @Nullable Style termStyle(IStrategoTerm term) {
        final int termType = term.getTermType();
        if(termType != IStrategoTerm.APPL && termType != IStrategoTerm.TUPLE && termType != IStrategoTerm.LIST) {
            // Try to use the parent of terminal nodes, mimicking behavior of old Spoofax/IMP runtime.
            final IStrategoTerm parentTerm = ParentAttachment.getParent(term);
            if(parentTerm != null) {
                final @Nullable Style style = sortConsStyle(parentTerm);
                if(style != null) {
                    return style;
                }
            }
        }

        return sortConsStyle(term);
    }

    private @Nullable Style sortConsStyle(IStrategoTerm term) {
        final @Nullable ImploderAttachment imploderAttachment = ImploderAttachment.get(term);
        if(imploderAttachment == null) {
            return null;
        }
        final @Nullable String sort = imploderAttachment.getSort();
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
