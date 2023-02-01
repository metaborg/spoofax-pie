package mb.statix.referenceretention.stratego;

import mb.constraint.common.ConstraintAnalyzerContext;
import mb.nabl2.terms.ITerm;
import static mb.nabl2.terms.matching.Transform.T;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.terms.stratego.TermIndex;
import mb.scopegraph.oopsla20.IScopeGraph;
import mb.statix.referenceretention.statix.RRLockedReference;
import mb.statix.scopegraph.Scope;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spoofax.StatixPrimitive;
import mb.stratego.common.AdaptableContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static mb.statix.referenceretention.stratego.RRTermUtils.unwrapConstraintAnalyzerContext;
import static mb.statix.referenceretention.stratego.RRTermUtils.unwrapStrategoContext;

/**
 * Creates a locked reference, that is, a reference whose syntax and resolution are specified.
 */
public final class RRLockReferenceStrategy extends StatixPrimitive {
    public static final String NAME = "RR_lock_reference";
    public RRLockReferenceStrategy() {
        super(NAME, 2);
    }


    @Override
    protected Optional<? extends ITerm> call(IContext env, ITerm term, List<ITerm> terms) throws InterpreterException {
        final RRStrategoContext rrctx;
        final ConstraintAnalyzerContext cactx;
        try {
            rrctx = unwrapStrategoContext(env);
            cactx = unwrapConstraintAnalyzerContext(env);
        } catch(RuntimeException e) {
            return Optional.empty(); // Context not available; fail
        }
        @Nullable final ITerm result = eval(
            rrctx,
            cactx,
            env.getFactory(),
            term,
            terms.get(0),
            terms.get(1)
        );
        return Optional.ofNullable(result);
    }

    // Usage:
    // a := <stx--get-ast-analysis> ast;
    // decl := <stx--get-ast-property(|a,Ref())> ref;
    // <prim("RR_lock_reference", decl, solverResultTerm)> ref
    // TODO: The scope should be an appropriate type, e.g., Scope or something.
    @Nullable private ITerm eval(
        RRStrategoContext rrctx,
        ConstraintAnalyzerContext cactx,
        ITermFactory termFactory,
        ITerm ref,
        ITerm decl,
        ITerm solverResultTerm
    ) throws InterpreterException {
        final @Nullable TermIndex declIdx = decl.getAttachments().get(TermIndex.class);
        if (declIdx == null) throw new RuntimeException("Could not get TermIndex from declaration: " + decl);


        // The solver result (analysis) should be the first term argument
        final SolverResult result = RRTermUtils.extractFinalSolverResult(solverResultTerm);
        final IScopeGraph<Scope, ITerm, ITerm> scopeGraph = result.state().scopeGraph();
        final Optional<Scope> declScope = scopeGraph.getData().entrySet().stream()
            .filter(e -> {
                ITerm scopeData = e.getValue();
                return T.collecttd(t -> Optional.ofNullable(declIdx.equals(t.getAttachments().get(TermIndex.class)) ? t : null))
                    .apply(scopeData).isEmpty();
            })
            .map(e -> e.getKey())
            .findFirst();
        if (!declScope.isPresent()) {
            // throw new RuntimeException("Could not find scope for declaration: " + decl);
            return null;
        }

        return RRLockedReference.of(ref, declScope.get());
    }
}

