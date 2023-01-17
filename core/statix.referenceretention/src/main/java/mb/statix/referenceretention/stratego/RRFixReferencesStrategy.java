package mb.statix.referenceretention.stratego;

import io.usethesource.capsule.Map;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.statix.constraints.messages.IMessage;
import mb.statix.referenceretention.statix.LockedReference;
import mb.statix.referenceretention.tego.RRContext;
import mb.statix.referenceretention.tego.RRPlaceholderDescriptor;
import mb.statix.referenceretention.tego.RRSolverState;
import mb.statix.referenceretention.tego.UnwrapOrFixAllReferencesStrategy;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spoofax.StatixPrimitive;
import mb.stratego.common.AdaptableContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.sequences.Seq;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static mb.nabl2.terms.matching.TermMatch.M;
import static mb.tego.strategies.StrategyExt.fun;

/**
 * Invokes the Tego strategy {@link UnwrapOrFixAllReferencesStrategy}
 * to unwrap the placeholder body and fix all references.
 * <p>
 * Usage: {@code prim("RR_fix_references", analysis)}, where {@code analysis} is the analysis result, a blob with an
 * instance of {@link SolverResult}.
 */
public final class RRFixReferencesStrategy extends StatixPrimitive {
    public static final String NAME = "RR_fix_references";
    public RRFixReferencesStrategy()  { super(NAME, 1); }

    @Override
    protected Optional<? extends ITerm> call(IContext env, ITerm term, List<ITerm> terms) throws InterpreterException {
        final RRStrategoContext context;
        try {
            context = AdaptableContext.adaptContextObject(env.contextObject(), RRStrategoContext.class);
        } catch(RuntimeException e) {
            return Optional.empty(); // Context not available; fail
        }
        // Return the name, for debugging
        final TegoRuntime tegoRuntime = context.tegoRuntime;
        final Execution execution = new Execution(
            context.tegoRuntime,
            context.strategoRuntime,
            context.strategoTerms,
            context.qualifyReferenceStrategyName,
            env.getFactory());

        // The solver result (analysis) should be the first term argument
        final SolverResult analysis = M.blobValue(SolverResult.class).match(terms.get(0))
            .orElseThrow(() -> new InterpreterException("Expected solver result."));

        final Map.Immutable<ITermVar, RRPlaceholderDescriptor> placeholderDescriptors = Map.Immutable.of(); // TODO: Get them from the AST
        final RRSolverState state = RRSolverState.fromSolverResult(analysis, null, placeholderDescriptors);  // TODO
        final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors = Collections.emptyList(); // TODO
        final @Nullable ITerm result = execution.fix(state, allowedErrors);
        if (result == null) return Optional.empty();
        return Optional.of(result);
    }

    private final class Execution {
        private final TegoRuntime tegoRuntime;
        private final StrategoRuntime strategoRuntime;
        private final StrategoTerms strategoTerms;
        private final String qualifyReferenceStrategyName;
        private final ITermFactory termFactory;

        private Execution(
            TegoRuntime tegoRuntime,
            StrategoRuntime strategoRuntime,
            StrategoTerms strategoTerms,
            String qualifyReferenceStrategyName,
            ITermFactory termFactory
        ) {
            this.tegoRuntime = tegoRuntime;
            this.strategoRuntime = strategoRuntime;
            this.strategoTerms = strategoTerms;
            this.qualifyReferenceStrategyName = qualifyReferenceStrategyName;
            this.termFactory = termFactory;
        }

        private @Nullable ITerm fix(RRSolverState state, Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
            // Create a strategy that fails if the term is not an injection
            final Strategy1<ITerm, LockedReference, @Nullable ITerm> qualifyReferenceStrategy = fun(this::qualifyReference);

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
            return null;
        }

        /**
         * Qualifies the given reference.
         *
         * @param lockedReference the declaration the reference is locked to
         * @param term the reference to qualify
         * @return the qualified reference term; otherwise, {@code null}
         */
        private @Nullable ITerm qualifyReference(LockedReference lockedReference, ITerm term) {
            try {
                final IStrategoTerm strategoTerm = strategoTerms.toStratego(term, true);
                @Nullable final IStrategoTerm output = strategoRuntime.invokeOrNull(qualifyReferenceStrategyName, strategoTerm);
                if (output == null) return null;
                return strategoTerms.fromStratego(output);
            } catch (StrategoException ex) {
                return null;
            }
        }
    }
}
