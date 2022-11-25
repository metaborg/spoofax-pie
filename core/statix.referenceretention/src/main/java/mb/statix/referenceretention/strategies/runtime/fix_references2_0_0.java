package mb.statix.referenceretention.strategies.runtime;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.statix.codecompletion.CodeCompletionProposal;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.IConstraint;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.sequences.Seq;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.NotImplementedException;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static mb.tego.strategies.StrategyExt.pred;

/**
 * Fixes the references in the AST.
 * <p>
 * In Stratego, declare this strategy as {@code external fix-references}.
 */
public final class fix_references2_0_0 extends Strategy {

    /** The instance. */
    public static final fix_references2_0_0 instance = null; // TODO: new fix_references2_0_0(tegoRuntime, strategoTerms, qualifyReferenceStrategyName);

    private final TegoRuntime tegoRuntime;
    private final StrategoTerms strategoTerms;

    private final String qualifyReferenceStrategyName;

    // Questions:
    // - how to register this strategy for all languages?
    // - how to obtain these dependencies from the configuration?

    public fix_references2_0_0(
        TegoRuntime tegoRuntime,
        StrategoTerms strategoTerms,
        String qualifyReferenceStrategyName
    ) {
        this.tegoRuntime = tegoRuntime;
        this.strategoTerms = strategoTerms;
        this.qualifyReferenceStrategyName = qualifyReferenceStrategyName;
    }

    @Override public @Nullable IStrategoTerm invoke(Context context, IStrategoTerm current) {
        // TODO: This should call the Tego engine and strategy.
        throw new IllegalStateException("Strategy fix_references_0_0 called!");
    }

    private final class Execution {
        private final StrategoRuntime strategoRuntime;

        private Execution(StrategoRuntime strategoRuntime) {
            this.strategoRuntime = strategoRuntime;
        }

        private IStrategoTerm fix(RRSolverState state, Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
            // Create a strategy that fails if the term is not an injection
            final Strategy1<ITerm, LockedReference, @Nullable ITerm> qualifyReferenceStrategy = null;// TODO, something with qualifyReference

            final RRContext ctx = new RRContext(qualifyReferenceStrategy, allowedErrors);

            final List<RRSolverState> resultsEvaluated;
            try {
                // The variable we're looking for is not in the unifier
                final @Nullable Seq<RRSolverState> results = tegoRuntime.eval(
                    UnwrapOrFixAllReferencesStrategy.getInstance(),
                    ctx,
                    state);
                if (results == null) throw new IllegalStateException("This cannot be happening.");
                // NOTE: This is the point at which the built sequence gets evaluated:
                resultsEvaluated = results.toList();
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
            // TODO: Create an AST with the fixed reference
            throw new NotImplementedException();
        }

        /**
         * Determines if the given term is an injection.
         *
         * @param term the term to check
         * @return {@code true} when the term is an injection; otherwise, {@code false}
         */
        private boolean qualifyReference(ITerm term) {
            try {
                final IStrategoTerm strategoTerm = strategoTerms.toStratego(term, true);
                @Nullable final IStrategoTerm output = strategoRuntime.invokeOrNull(qualifyReferenceStrategyName, strategoTerm);
                return output != null;
            } catch (StrategoException ex) {
                return false;
            }
        }
    }
}
