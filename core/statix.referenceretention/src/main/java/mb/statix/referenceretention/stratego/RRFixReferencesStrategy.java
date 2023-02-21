package mb.statix.referenceretention.stratego;

import com.google.common.collect.ImmutableList;
import io.usethesource.capsule.Map;
import io.usethesource.capsule.Set;
import mb.common.util.UncheckedException;
import mb.nabl2.terms.IListTerm;
import mb.nabl2.terms.IStringTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.build.TermBuild;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.resource.DefaultResourceKey;
import mb.resource.ResourceKey;
import mb.statix.constraints.CConj;
import mb.statix.constraints.CEqual;
import mb.statix.constraints.CUser;
import mb.statix.constraints.messages.IMessage;
import mb.statix.referenceretention.statix.RRPlaceholder;
import mb.statix.referenceretention.tego.InferStrategy;
import mb.statix.referenceretention.tego.RRContext;
import mb.statix.referenceretention.tego.RRSolverState;
import mb.statix.referenceretention.tego.RRUtils;
import mb.statix.referenceretention.tego.UnwrapOrFixAllReferencesStrategy;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import mb.statix.spoofax.StatixPrimitive;
import mb.stratego.common.AdaptableContext;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.tego.sequences.Seq;
import mb.tego.strategies.Strategy1;
import mb.tego.strategies.Strategy2;
import mb.tego.strategies.Strategy3;
import mb.tego.strategies.runtime.TegoRuntime;
import mb.tego.tuples.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static mb.nabl2.terms.matching.Transform.T;
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
    public static final int SVARS = 0;
    public static final int TVARS = 1;
    public RRFixReferencesStrategy()  { super(NAME, TVARS); }

    // Usage:
    // fixedAst := <prim("RR_fix_references", solverResultTerm)> ast
    @Override
    protected Optional<? extends ITerm> call(IContext env, ITerm term, List<ITerm> terms) throws InterpreterException {
        // The `term` must be explicated, as well as the terms in the placeholders.

        final RRStrategoContext context;
        try {
            context = AdaptableContext.adaptContextObject(env.contextObject(), RRStrategoContext.class);
        } catch(RuntimeException e) {
            return Optional.empty(); // Context not available; fail
        }
        // The solver result (analysis) should be the first term argument
        final SolverResult analysis = RRTermUtils.extractFinalSolverResult(terms.get(0));

        final Execution execution = new Execution(
            context.tegoRuntime,
            context.strategoRuntime,
            context.strategoTerms,
            context.qualifyReferenceStrategyName,
            env.getFactory(),
            analysis.spec(),

            new DefaultResourceKey("rr", "somefile.tig"),   // TODO: Not sure how to get the resource key here? Do we even need it?
            "main",                         // TODO: Tiger specific. Make configurable.
            "programOk",                    // TODO: Tiger specific. Make configurable.
            "pre-analyze",                  // TODO: Tiger specific. Make configurable.
            "post-analyze",                 // TODO: Tiger specific. Make configurable.
            "explicate-injections-tiger",   // TODO: Tiger specific. Make configurable.
            "implicate-injections-tiger"    // TODO: Tiger specific. Make configurable.
            );

        // Build a new analysis
        final IState.Transient state = analysis.state().melt();
        final Pair<ITerm, Map.Immutable<ITermVar, RRPlaceholder>> pair = extractPlaceholders(state, term);
        final ITerm newTerm = pair.component1();
        final Map.Immutable<ITermVar, RRPlaceholder> placeholderDescriptors = pair.component2();

        final Pair<ITermVar, RRSolverState> initialRootVarAndSolverState = execution.createInitialSolverState(
            newTerm, // explicated
            placeholderDescriptors.keySet(),
            placeholderDescriptors
        );
        final ITermVar rootVar = initialRootVarAndSolverState.component1();
        final RRSolverState initialSolverState = initialRootVarAndSolverState.component2();
        final RRSolverState analyzedState = execution.analyze(initialSolverState);
        final Collection<Map.Entry<IConstraint, IMessage>> allowedErrors = initialSolverState.getMessages().entrySet();
        final @Nullable RRSolverState fixedState = execution.fix(analyzedState, allowedErrors);
        if (fixedState == null) return Optional.empty();
        final ITerm fixedAst = fixedState.project(rootVar);
        return Optional.of(fixedAst);
    }

    /**
     * Extract placeholders from the given term.
     *
     * @param state the state
     * @param term the term to extract from
     * @return a pair of the new term (with term variables instead of placeholders)
     * and a mapping of term variables to placeholders
     */
    private Pair<ITerm, Map.Immutable<ITermVar, RRPlaceholder>> extractPlaceholders(IState.Transient state, ITerm term) {
        final Map.Transient<ITermVar, RRPlaceholder> map = Map.Transient.of();

        final ITerm newTerm = T.sometd(t -> RRPlaceholder.matcher().map(p -> (ITerm)freshVarForPlaceholder(state, map, p)).match(t)).apply(term);
        return Pair.of(newTerm, map.freeze());
    }

    /**
     * Creates a fresh variable for the specified placeholder.
     *
     * @param state the state in which to add the new variable
     * @param map the map in which to store the new mapping
     * @param placeholder the placeholder being replaced
     * @return the fresh variable
     */
    private ITermVar freshVarForPlaceholder(IState.Transient state, Map.Transient<ITermVar, RRPlaceholder> map, RRPlaceholder placeholder) {
        ITermVar newVar = state.freshWld();
        map.put(newVar, placeholder);
        return newVar;
    }

    private final class Execution {
        private final TegoRuntime tegoRuntime;
        private final StrategoRuntime strategoRuntime;
        private final StrategoTerms strategoTerms;
        private final String qualifyReferenceStrategyName;
        private final ITermFactory termFactory;
        /** The Statix specification. */
        private final Spec spec;

        private final String statixSpecName;
        private final String statixRootPredicateName;
        private final String preAnalyzeStrategyName;
        private final String postAnalyzeStrategyName;
        private final String explicateStrategyName;
        private final String implicateStrategyName;

        private final ResourceKey resource;

        private Execution(
            TegoRuntime tegoRuntime,
            StrategoRuntime strategoRuntime,
            StrategoTerms strategoTerms,
            String qualifyReferenceStrategyName,
            ITermFactory termFactory,
            Spec spec,

            ResourceKey resource,
            String statixSpecName,
            String statixRootPredicateName,
            String preAnalyzeStrategyName,
            String postAnalyzeStrategyName,
            String explicateStrategyName,
            String implicateStrategyName
        ) {
            this.tegoRuntime = tegoRuntime;
            this.strategoRuntime = strategoRuntime;
            this.strategoTerms = strategoTerms;
            this.qualifyReferenceStrategyName = qualifyReferenceStrategyName;
            this.termFactory = termFactory;
            this.spec = spec;

            this.resource = resource;
            this.statixSpecName = statixSpecName;
            this.statixRootPredicateName = statixRootPredicateName;
            this.preAnalyzeStrategyName = preAnalyzeStrategyName;
            this.postAnalyzeStrategyName = postAnalyzeStrategyName;
            this.explicateStrategyName = explicateStrategyName;
            this.implicateStrategyName = implicateStrategyName;
        }

        /**
         * Creates the initial solver state for the code completion algorithm.
         *
         * @param ast the AST
         * @param existentials the existentials
         * @param placeholderDescriptors the placeholder descriptors
         * @return a term variable for the AST, and the initial solver state
         */
        private Pair<ITermVar, RRSolverState> createInitialSolverState(
            ITerm ast,
            java.util.Set<ITermVar> existentials,
            Map.Immutable<ITermVar, RRPlaceholder> placeholderDescriptors
        ) {
            final IState.Transient state = State.of().melt();
            String qualifiedName = RRUtils.makeQualifiedName(statixSpecName, statixRootPredicateName);
            final ITermVar rootVar = state.freshWld();
            final IConstraint initialConstraint = new CConj(
                new CUser(qualifiedName, Collections.singletonList(rootVar), null),
                new CEqual(rootVar, ast)
            );
            final HashSet<ITermVar> newExistentials = new HashSet<>(existentials);
            newExistentials.add(rootVar);
            final RRSolverState solverState = RRSolverState.of(
                    spec,                                   // the specification
                    state.freeze(),                         // the new empty Statix state
                    ImmutableList.of(initialConstraint),    // list of constraints
                    placeholderDescriptors                  // list of placeholder descriptors
                )
                .withExistentials(newExistentials)
                .withPrecomputedCriticalEdges();
            return Pair.of(rootVar, solverState);
        }

        /**
         * Explicates the given AST, that is, adds explicit injection constructors.
         * This also adds term indices on terms where they are not present.
         * <p>
         * The AST may contain term variables.
         *
         * @param ast the AST to explicate
         * @return the explicated AST
         */
        private ITerm explicate(ITerm ast) {
            final IStrategoTerm strAst = strategoTerms.toStratego(ast);
            // Explicate
            final IStrategoTerm explicatedStrAst;
            try {
                explicatedStrAst = strategoRuntime.invoke(explicateStrategyName, strAst);
            } catch (StrategoException ex) {
                throw new UncheckedException(ex);
            }
            // Add missing term indices
            final IStrategoTerm indexedStrAst = StrategoTermIndices.indexMore(explicatedStrAst, resource.toString(), StrategoTermIndices.findMaxIndex(strAst) + 1, termFactory);
            return strategoTerms.fromStratego(indexedStrAst);
        }

        /**
         * Implicates the given AST, that is, removes explicit injection constructors.
         * <p>
         * The AST may contain term variables.
         *
         * @param ast the AST to implicate
         * @return the implicated AST
         */
        private ITerm implicate(ITerm ast) {
            final IStrategoTerm strAst = strategoTerms.toStratego(ast);
            // Implicate
            final IStrategoTerm implcatedStrAst;
            try {
                implcatedStrAst = strategoRuntime.invoke(implicateStrategyName, strAst);
            } catch (StrategoException ex) {
                throw new UncheckedException(ex);
            }
            return strategoTerms.fromStratego(implcatedStrAst);
        }

        /**
         * Performs analysis on the given solver state.
         *
         * @return the resulting analyzed solver state
         * @throws IllegalStateException if the analyzed solver state has errors or has no constraints
         */
        private RRSolverState analyze(RRSolverState state) {
            final @Nullable RRSolverState analyzedState = tegoRuntime.eval(InferStrategy.getInstance(), state);
            if (analyzedState == null) {
                throw new IllegalStateException("Reference retention failed: got no result from Tego strategy.");
            } else if(analyzedState.hasErrors()) {
                // TODO: We can add these errors to the set of allowed errors
                throw new IllegalStateException("Reference retention failed: input program validation failed:\n" + analyzedState.messagesToString());
            } else if(analyzedState.getConstraints().isEmpty()) {
                throw new IllegalStateException("Reference retention failed: no constraints left, nothing to complete.\n" + analyzedState);
            }
            return analyzedState;
        }

        /**
         * Fixes the specified solver state (which includes placeholders).
         *
         * @param state the state to fix
         * @param allowedErrors the errors that are allowed
         * @return the fixed state; or {@code null} if the state could not be fixed
         */
        private @Nullable RRSolverState fix(RRSolverState state, Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
            // Create a strategy that fails if the term is not an injection
            final Strategy3</* ctx */ IListTerm, /* sortName */ IStringTerm, /* solverResult */SolverResult, /* term */ ITerm, /* result */ @Nullable ITerm> qualifyReferenceStrategy = fun(this::qualifyReference);

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
                // TODO: Extract one of the solver states (use a heuristic) and then find the AST in there?
                //  Probably similar to how I did it in code completion
                resultsEvaluated = results.toList();
                // Return the first one. TODO: Better heuristic to pick one if there are multiple options.
                return resultsEvaluated.stream().findFirst().orElse(null);
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Qualifies the given reference.
         *
         * @param term the reference to qualify
         * @param contextTerms the context terms
         * @param sortName the sort name of the incoming and resulting reference
         * @return a list of pairs of a qualified reference term and a term index; otherwise, {@code null}
         */
        private @Nullable ITerm qualifyReference(ITerm term, IListTerm contextTerms, IStringTerm sortName, SolverResult solverResult) {
            try {
                final IStrategoTerm sTerm = strategoTerms.toStratego(term, true);
                final IStrategoTerm sContextTerms = strategoTerms.toStratego(contextTerms, true);
                final IStrategoTerm sSortName = strategoTerms.toStratego(sortName, true);
                final IStrategoTerm sSolverResult = strategoTerms.toStratego(mb.nabl2.terms.build.TermBuild.B.newBlob(solverResult));
//                @Nullable final IStrategoTerm output = strategoRuntime.invokeOrNull(qualifyReferenceStrategyName, sTerm, sContextTerms, sSortName);
                @Nullable final IStrategoTerm output = strategoRuntime.invoke(qualifyReferenceStrategyName, sTerm, sContextTerms, sSortName, sSolverResult);
//                if (output == null) {
//
//                    throw StrategoException.strategyFail("qualifyReference", sTerm, sContextTerms, sSortName)
//                }
                return strategoTerms.fromStratego(output);
            } catch (StrategoException ex) {
                throw new UncheckedException(ex);
            }
        }
    }
}
