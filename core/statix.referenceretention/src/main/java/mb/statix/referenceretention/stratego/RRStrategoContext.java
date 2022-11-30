package mb.statix.referenceretention.stratego;

import mb.nabl2.terms.stratego.StrategoTerms;
import mb.stratego.common.StrategoRuntime;
import mb.tego.strategies.runtime.TegoRuntime;

/**
 * The Stratego context object for reference retention.
 */
public final class RRStrategoContext {

    public final TegoRuntime tegoRuntime;
    public StrategoRuntime strategoRuntime = null;
    public final StrategoTerms strategoTerms;
    public final String qualifyReferenceStrategyName;

    public RRStrategoContext(
        TegoRuntime tegoRuntime,
        StrategoTerms strategoTerms,
        String qualifyReferenceStrategyName
    ) {
        this.tegoRuntime = tegoRuntime;
        this.strategoTerms = strategoTerms;
        this.qualifyReferenceStrategyName = qualifyReferenceStrategyName;
    }
}
