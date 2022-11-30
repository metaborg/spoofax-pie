package mb.statix.referenceretention.stratego;

import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.statix.constraints.messages.IMessage;
import mb.statix.referenceretention.statix.LockedReference;
import mb.statix.referenceretention.tego.RRContext;
import mb.statix.referenceretention.tego.RRSolverState;
import mb.statix.referenceretention.tego.UnwrapOrFixAllReferencesStrategy;
import mb.statix.solver.IConstraint;
import mb.stratego.common.AdaptableContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.sequences.Seq;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.NotImplementedException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static mb.tego.strategies.StrategyExt.fun;

/**
 * Invokes the Tego strategy {@link UnwrapOrFixAllReferencesStrategy}
 * to unwrap the placeholder body and fix all references.
 */
public final class RRFixReferencesStrategy extends AbstractPrimitive {
    public static final String NAME = "RR_fix_references";
    public RRFixReferencesStrategy() {
        super(NAME, 0, 0);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        try {
            final RRStrategoContext context = AdaptableContext.adaptContextObject(env.contextObject(), RRStrategoContext.class);
            // Return the name, for debugging
            final TegoRuntime tegoRuntime = context.tegoRuntime;
            final Execution execution = new Execution(
                context.tegoRuntime,
                context.strategoRuntime,
                context.strategoTerms,
                context.qualifyReferenceStrategyName,
                env.getFactory());
            // TODO: Get solver state
            // TODO: Get term transformed to NaBL term

            final RRSolverState state = null;  // TODO
            final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors = Collections.emptyList(); // TODO
            final @Nullable IStrategoTerm result = null; // TODO: Call this: execution.fix(state, allowedErrors);
            // TODO: Transform NaBl term back to Stratego term
            if (result == null) return false;
            env.setCurrent(result);
            return true;
        } catch(RuntimeException e) {
            return false; // Context not available; fail
        }
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

        private @Nullable IStrategoTerm fix(RRSolverState state, Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
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
            throw new NotImplementedException();
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
