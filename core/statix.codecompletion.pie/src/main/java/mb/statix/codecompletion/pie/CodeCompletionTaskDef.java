package mb.statix.codecompletion.pie;

import com.google.common.collect.ImmutableList;
import io.usethesource.capsule.Set;
import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editing.TextEdit;
import mb.common.option.Option;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.StyleName;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.pie.JsglrParseTaskDef;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.IApplTerm;
import mb.nabl2.terms.ITerm;
import mb.nabl2.terms.ITermVar;
import mb.nabl2.terms.stratego.PlaceholderVarMap;
import mb.nabl2.terms.stratego.StrategoPlaceholders;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.codecompletion.CodeCompletionProposal;
import mb.statix.codecompletion.SolutionMeta;
import mb.statix.codecompletion.SolverContext;
import mb.statix.codecompletion.SolverState;
import mb.statix.codecompletion.TermCodeCompletionItem;
import mb.statix.codecompletion.TermCodeCompletionResult;
import mb.statix.codecompletion.strategies.runtime.CompleteStrategy;
import mb.statix.codecompletion.strategies.runtime.InferStrategy;
import mb.statix.constraints.CUser;
import mb.statix.constraints.messages.IMessage;
import mb.statix.constraints.messages.MessageKind;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.tego.sequences.Seq;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static mb.statix.codecompletion.pie.CodeCompletionUtils.findAllPlaceholdersIn;
import static mb.statix.codecompletion.pie.CodeCompletionUtils.findPlaceholderAt;
import static mb.statix.codecompletion.pie.CodeCompletionUtils.getRegion;
import static mb.statix.codecompletion.pie.CodeCompletionUtils.iterableToListView;
import static mb.statix.codecompletion.pie.CodeCompletionUtils.makeQualifiedName;
import static mb.statix.codecompletion.pie.CodeCompletionUtils.normalizeText;
import static mb.statix.codecompletion.pie.CodeCompletionUtils.tryGetRegion;
import static mb.tego.strategies.StrategyExt.pred;

/**
 * Code completion task definition.
 */
public class CodeCompletionTaskDef implements TaskDef<CodeCompletionTaskDef.Input, Result<CodeCompletionResult, ?>> {

    /**
     * Input arguments for the {@link CodeCompletionTaskDef}.
     */
    public static class Input implements Serializable {
        /** The primary selection at which to complete. */
        public final Region primarySelection;
        /** The file being completed. */
        public final ResourceKey file;
        /** The root directory of the project; or {@code null} when not specified. */
        public final @Nullable ResourcePath rootDirectoryHint;
        /** Whether to perform deterministic completion. */
        private final boolean completeDeterministic;

        /**
         * Initializes a new instance of the {@link Input} class.
         *
         * @param primarySelection the primary selection at which completion is invoked
         * @param file      the key of the resource in which completion is invoked
         * @param rootDirectoryHint the root directory of the project; or {@code null} when not specified
         */
        public Input(Region primarySelection, ResourceKey file, @Nullable ResourcePath rootDirectoryHint) {
            this(primarySelection, file, rootDirectoryHint, false);
        }

        /**
         * Initializes a new instance of the {@link Input} class.
         *
         * @param primarySelection the primary selection at which completion is invoked
         * @param file      the key of the resource in which completion is invoked
         * @param rootDirectoryHint the root directory of the project; or {@code null} when not specified
         * @param completeDeterministic whether to perform deterministic completion
         */
        public Input(Region primarySelection, ResourceKey file, @Nullable ResourcePath rootDirectoryHint, boolean completeDeterministic) {
            this.primarySelection = primarySelection;
            this.file = file;
            this.rootDirectoryHint = rootDirectoryHint;
            this.completeDeterministic = completeDeterministic;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return innerEquals((Input)o);
        }

        /**
         * Determines whether this object is equal to the specified object.
         *
         * Note: this method does not check whether the type of the argument is exactly the same.
         *
         * @param that the object to compare to
         * @return {@code true} when this object is equal to the specified object;
         * otherwise, {@code false}
         */
        protected boolean innerEquals(Input that) {
            // @formatter:off
            return this.primarySelection.equals(that.primarySelection)
                && this.file.equals(that.file)
                && Objects.equals(this.rootDirectoryHint, that.rootDirectoryHint)
                && this.completeDeterministic == that.completeDeterministic;
            // @formatter:on
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.rootDirectoryHint,
                this.file,
                this.primarySelection,
                this.completeDeterministic
            );
        }

        @Override public String toString() {
            return "CodeCompletionTaskDef$Input{" +
                "primarySelection=" + primarySelection + ", " +
                "rootDirectoryHint=" + rootDirectoryHint + ", " +
                "file=" + file + ", " +
                "completeDeterministic=" + completeDeterministic +
                "}";
        }
    }

    private final Logger log;
    private final JsglrParseTaskDef parseTask;
    private final ConstraintAnalyzeFile analyzeFileTask;
    private final AstWithPlaceholdersTaskDef astWithPlaceholdersTaskDef;
    private final GetStrategoRuntimeProvider getStrategoRuntimeProviderTask;
    private final TegoRuntime tegoRuntime;
    private final StatixSpecTaskDef statixSpec;
    private final StrategoTerms strategoTerms;

    private final String preAnalyzeStrategyName;
    private final String postAnalyzeStrategyName;
    private final String upgradePlaceholdersStrategyName;
    private final String downgradePlaceholdersStrategyName;
    private final String isInjStrategyName;
    private final String ppPartialStrategyName;

    private final String statixSecName;
    private final String statixRootPredicateName;
    private Supplier<@Nullable CodeCompletionEventHandler> eventHandlerProvider;

    /**
     * Initializes a new instance of the {@link CodeCompletionTaskDef} class.
     *
     * @param parseTask the parser task
     * @param analyzeFileTask the analysis task
     * @param astWithPlaceholdersTaskDef the task that inserts placeholders near the caret location in the AST
     * @param getStrategoRuntimeProviderTask the Stratego runtime provider task
     * @param statixSpec the Statix spec task
     * @param strategoTerms the Stratego to NaBL terms utility class
     * @param loggerFactory the logger factory
     * @param eventHandlerProvider the event handler provider
     *
     * @param preAnalyzeStrategyName the {@code pre-analyze} Stratego strategy name
     * @param postAnalyzeStrategyName the {@code post-analyze} Stratego strategy name
     * @param upgradePlaceholdersStrategyName the {@code upgrade-placeholders} Stratego strategy name
     * @param downgradePlaceholdersStrategyName the {@code downgrade-placeholders} Stratego strategy name
     * @param isInjStrategyName the {@code is-inj} Stratego strategy name
     * @param ppPartialStrategyName the {@code pp-partial} Stratego strategy name
     *
     * @param statixSecName the name of the Statix spec
     * @param statixRootPredicateName the name of the single-analysis root predicate in the Statix spec
     */
    public CodeCompletionTaskDef(
        JsglrParseTaskDef parseTask,
        ConstraintAnalyzeFile analyzeFileTask,
        AstWithPlaceholdersTaskDef astWithPlaceholdersTaskDef,
        GetStrategoRuntimeProvider getStrategoRuntimeProviderTask,
        TegoRuntime tegoRuntime,
        StatixSpecTaskDef statixSpec,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory,
        Supplier<@Nullable CodeCompletionEventHandler> eventHandlerProvider,

        String preAnalyzeStrategyName,
        String postAnalyzeStrategyName,
        String upgradePlaceholdersStrategyName,
        String downgradePlaceholdersStrategyName,
        String isInjStrategyName,
        String ppPartialStrategyName,

        String statixSecName,
        String statixRootPredicateName
    ) {
        this.parseTask = parseTask;
        this.analyzeFileTask = analyzeFileTask;
        this.astWithPlaceholdersTaskDef = astWithPlaceholdersTaskDef;
        this.getStrategoRuntimeProviderTask = getStrategoRuntimeProviderTask;
        this.tegoRuntime = tegoRuntime;
        this.statixSpec = statixSpec;
        this.strategoTerms = strategoTerms;
        this.log = loggerFactory.create(getClass());
        this.eventHandlerProvider = eventHandlerProvider;

        this.preAnalyzeStrategyName = preAnalyzeStrategyName;
        this.postAnalyzeStrategyName = postAnalyzeStrategyName;
        this.upgradePlaceholdersStrategyName = upgradePlaceholdersStrategyName;
        this.downgradePlaceholdersStrategyName = downgradePlaceholdersStrategyName;
        this.isInjStrategyName = isInjStrategyName;
        this.ppPartialStrategyName = ppPartialStrategyName;

        this.statixSecName = statixSecName;
        this.statixRootPredicateName = statixRootPredicateName;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public Result<CodeCompletionResult, ?> exec(ExecContext context, Input input) throws Exception {
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProviderTask, None.instance).getValue().get();
        final Result<Spec, ?> specResult = context.require(statixSpec, None.instance);
        if (specResult.isErr()) return specResult.ignoreValueIfErr();
        final Spec spec = specResult.unwrapUnchecked();

        return new Execution(
            context, input, strategoRuntime, spec
        ).complete();
    }

    /**
     * Sets the event handler provider for this task.
     *
     * @param eventHandlerProvider the event handler provider
     */
    public void withEventHandlerProvider(Supplier<@Nullable CodeCompletionEventHandler> eventHandlerProvider) {
        this.eventHandlerProvider = eventHandlerProvider;
    }

    /**
     * Keeps objects used by the code completion algorithm in a more accessible place.
     */
    private final class Execution {
        /** The execution context. */
        private final ExecContext context;
        /** The Stratego runtime. */
        private final StrategoRuntime strategoRuntime;
        /** The term factory. */
        private final ITermFactory termFactory;
        /** The root directory of the project; or {@code null} when not specified. */
        public final @Nullable ResourcePath rootDirectoryHint;
        /** The file being completed. */
        public final ResourceKey file;
        /** The primary selection at which to complete. */
        public final Region primarySelection;
        /** The Statix specification. */
        private final Spec spec;
        /** Whether to perform deterministic completion. */
        private final boolean completeDeterministic;

        /**
         * Initializes a new instance of the {@link Execution} class.
         *
         * @param context the execution context
         * @param input the task arguments
         * @param strategoRuntime the Stratego runtime
         * @param spec the Statix specification
         */
        public Execution(
            ExecContext context,
            Input input,
            StrategoRuntime strategoRuntime,
            Spec spec
        ) {
            this.context = context;
            this.strategoRuntime = strategoRuntime;
            this.spec = spec;
            this.termFactory = strategoRuntime.getTermFactory();
            this.rootDirectoryHint = input.rootDirectoryHint;
            this.file = input.file;
            this.primarySelection = input.primarySelection;
            this.completeDeterministic = input.completeDeterministic;
        }

        /**
         * Performs code completion.
         *
         * @return the code completion result
         * @throws Exception if an exception occurred
         */
        public Result<CodeCompletionResult, ?> complete() throws Exception {
            @Nullable final CodeCompletionEventHandler eventHandler = eventHandlerProvider.get();
            if (eventHandler != null) eventHandler.begin();

            // Parse the AST
            if (eventHandler != null) eventHandler.beginParse();
            final Result<IStrategoTerm, ?> parsedAstResult = parse(primarySelection);
            if (parsedAstResult.isErr()) return parsedAstResult.ignoreValueIfErr();
            final IStrategoTerm parsedAst = parsedAstResult.unwrapUnchecked();
            if (eventHandler != null) eventHandler.endParse();
            log.trace("Parsed completion AST: " + parsedAst);

            // Prepare the AST (explicate, add term indices, upgrade placeholders)
            if (eventHandler != null) eventHandler.beginPreparation();
            final Result<IStrategoTerm, ?> explicatedAstResult = preAnalyze(parsedAst);
            if (explicatedAstResult.isErr()) return explicatedAstResult.ignoreValueIfErr();
            final IStrategoTerm explicatedAst = explicatedAstResult.unwrapUnchecked();
            final IStrategoTerm indexedAst = addTermIndices(explicatedAst);
            final ITerm statixAst = toStatix(indexedAst);
            final PlaceholderVarMap placeholderVarMap = new PlaceholderVarMap(file.toString());
            final Result<ITerm, ?> upgradedAstResult = upgradePlaceholders(statixAst, placeholderVarMap);
            if (upgradedAstResult.isErr()) return upgradedAstResult.ignoreValueIfErr();
            final ITerm upgradedAst = upgradedAstResult.unwrap();
            // TODO: We can perform our own placeholder inference here, since in the upgraded AST placeholders
            //  for lists _are_ possible.  The only remaining problem is that we don't have a way to represent
            //  placeholders for lists in code completion proposals.
            final ITermVar placeholder = getCompletionPlaceholders(upgradedAst, primarySelection).get(0);   // FIXME: Use all placeholders instead of just the first
            final SolverState initialState = createInitialSolverState(upgradedAst, statixSecName, statixRootPredicateName, placeholderVarMap);
            if (eventHandler != null) eventHandler.endPreparation();

            // Analyze the AST
            if (eventHandler != null) eventHandler.beginAnalysis();
            final SolverState analyzedState = analyze(initialState);
            final ArrayList<Map.Entry<IConstraint, IMessage>> allowedMessages = new ArrayList<>();
            if (analyzedState.hasErrors()) {
                analyzedState.getMessages().forEach((c, m) -> {
                    if(m.kind() == MessageKind.ERROR) {
                        allowedMessages.add(new AbstractMap.SimpleEntry<>(c, m));
                    }
                });
            }
            if (eventHandler != null) eventHandler.endAnalysis();

            // Execute the code completion Tego strategy
            if (eventHandler != null) eventHandler.beginCodeCompletion();
            final Seq<CodeCompletionProposal> completionProposals = complete(analyzedState, placeholder, allowedMessages /*Collections.emptyList() */);
            final Seq<CodeCompletionProposal> filteredProposals = filterProposals(completionProposals);
            final List<CodeCompletionProposal> instantiatedProposals = filteredProposals.toList(); // NOTE: This is where we actually coerce the lazy list find the completions.
            if (eventHandler != null) eventHandler.endCodeCompletion();

            // Get, convert, and prepare the proposals
            if (eventHandler != null) eventHandler.beginFinishing();
            final List<CodeCompletionProposal> orderedProposals = orderProposals(instantiatedProposals);
            final Region placeholderRegion = getRegion(placeholder, Region.atOffset(primarySelection.getStartOffset() /* TODO: Support the whole selection? */));
            final Result<List<CodeCompletionItem>, ?> finalProposalsResult = proposalsToCodeCompletionItems(orderedProposals, placeholderRegion);
            if (finalProposalsResult.isErr()) return finalProposalsResult.ignoreValueIfErr();
            final List<CodeCompletionItem> finalProposals = finalProposalsResult.unwrapUnchecked();
            if (eventHandler != null) eventHandler.endFinishing();

            if (finalProposals.isEmpty()) {
                log.info("Completion returned no completion proposals.");
            } else {
                log.trace("Completion returned the following proposals:\n - " + finalProposals.stream()
                    .map(CodeCompletionItem::getLabel).collect(Collectors.joining("\n - ")));
            }

            // TODO: We should track to which placeholder each completion belongs,
            //  and insert the completion at that placeholder with properly parenthesized.

            if (eventHandler != null) eventHandler.end();
            return Result.ofOk(new TermCodeCompletionResult(
                placeholder,
                ListView.copyOf(finalProposals),
                Objects.requireNonNull(tryGetRegion(placeholder)),
                true
            ));
        }

        /**
         * Parses the input file.
         *
         * @param selection code selection
         * @return the AST of the file
         */
        private Result<IStrategoTerm, ?> parse(Region selection) {
            final mb.pie.api.Supplier<Result<IStrategoTerm, JsglrParseException>> astSupplier = parseTask.inputBuilder()
                .withFile(file)
                .rootDirectoryHint(Optional.ofNullable(rootDirectoryHint))
                .codeCompletionMode(true)
                .cursorOffset(selection.getStartOffset() /* TODO: Support the whole selection? */)
                .buildRecoverableAstSupplier();

            return context.require(astWithPlaceholdersTaskDef, new AstWithPlaceholdersTaskDef.Input(selection, astSupplier))
                .map(o -> o.ast);
        }

        private Result<IStrategoTerm, ?> parenthesize(IStrategoTerm term) {
            try {
                return Result.ofOk(strategoRuntime.invoke("parenthesize-completion-term", term));
            } catch (StrategoException ex) {
                return Result.ofErr(ex);
            }
        }

        /**
         * Pretty-prints the given term.
         *
         * @param term the term to pretty-print
         * @return the pretty-printed term
         */
        private Result<String, ?> prettyPrint(IStrategoTerm term) {
            try {
                final IStrategoTerm output = strategoRuntime.invoke(ppPartialStrategyName, term);
                return Result.ofOk(TermUtils.toJavaString(output));
            } catch (StrategoException ex) {
                return Result.ofErr(ex);
            }
        }

        /**
         * Performs pre-analysis on the given AST.
         *
         * @param ast     the AST to explicate
         * @return the explicated AST
         */
        private Result<IStrategoTerm, ?> preAnalyze(IStrategoTerm ast) {
            try {
                return Result.ofOk(strategoRuntime.invoke(preAnalyzeStrategyName, ast));
            } catch (StrategoException ex) {
                return Result.ofErr(ex);
            }
        }

        /**
         * Performs post-analysis on the given term.
         *
         * @param term     the term to implicate
         * @return the implicated AST
         */
        private Result<IStrategoTerm, ?> postAnalyze(IStrategoTerm term) {
            try {
                return Result.ofOk(strategoRuntime.invoke(postAnalyzeStrategyName, term));
            } catch (StrategoException ex) {
                return Result.ofErr(ex);
            }
        }

        /**
         * Upgrade the placeholder terms to term variables.
         *
         * @param ast the AST to upgrade
         * @param placeholderVarMap the map to which mappings of placeholders to variables are added
         * @return the upgraded AST
         */
        private Result<ITerm, ?> upgradePlaceholders(ITerm ast, PlaceholderVarMap placeholderVarMap) {
            // FIXME: Ideally we would know the sort of the placeholder
            //  so we can use that sort to call the correct downgrade-placeholders-Lang-Sort strategy
            // FIXME: We can generate: downgrade-placeholders-Lang(|sort) = where(<?"Exp"> sort); downgrade-placeholders-Lang-Exp
            return Result.ofOk(StrategoPlaceholders.replacePlaceholdersByVariables(ast, placeholderVarMap));
        }

        /**
         * Downgrades the placeholder term variables to actual placeholders.
         *
         * @param term the term to downgrade
         * @return the downgraded term
         */
        private Result<IStrategoTerm, ?> downgradePlaceholders(IStrategoTerm term) {
            try {
                return Result.ofOk(strategoRuntime.invoke(downgradePlaceholdersStrategyName, term));
            } catch (StrategoException ex) {
                return Result.ofErr(ex);
            }
        }

        /**
         * Determines if the given term is an injection.
         *
         * @param term the term to check
         * @return {@code true} when the term is an injection; otherwise, {@code false}
         */
        private boolean isInjection(ITerm term) {
            try {
                final IStrategoTerm strategoTerm = strategoTerms.toStratego(term, true);
                @Nullable final IStrategoTerm output = strategoRuntime.invokeOrNull(isInjStrategyName, strategoTerm);
                return output != null;
            } catch (StrategoException ex) {
                return false;
            }
        }

        /**
         * Adds term indices to the given AST.
         *
         * @param ast     the AST to add term indices to
         * @return the AST with term indices
         */
        private IStrategoTerm addTermIndices(IStrategoTerm ast) {
            return StrategoTermIndices.index(ast, file.toString(), termFactory);
        }

        /**
         * Converts the {@link IStrategoTerm} AST to a Statix {@link ITerm} term.
         *
         * @param ast the AST to convert
         * @return the converted AST
         */
        private ITerm toStatix(IStrategoTerm ast) {
            return strategoTerms.fromStratego(ast);
        }

        /**
         * Determines the placeholder being completed.
         *
         * @param ast the AST to inspect
         * @param selection code selection
         * @return the term variable of the placeholder being completed
         */
        @Deprecated
        private ITermVar getCompletionPlaceholder(ITerm ast, Region selection) {
            @Nullable ITermVar placeholderVar = findPlaceholderAt(ast, selection.getStartOffset() /* TODO: Support the whole selection? */);
            if (placeholderVar == null) {
                throw new IllegalStateException("Completion failed: we don't know the placeholder.");
            }
            return placeholderVar;
        }

        /**
         * Determines all placeholders near or intersecting the selection.
         *
         * @param ast the AST to inspect
         * @param selection the selection to complete at
         * @return the list of term variables of the placeholders being completed
         */
        private List<? extends ITermVar> getCompletionPlaceholders(ITerm ast, Region selection) {
            List<? extends ITermVar> placeholderVars = findAllPlaceholdersIn(ast, selection);
            if (placeholderVars.isEmpty()) {
                throw new IllegalStateException("Completion failed: we don't know the placeholder.");
            }
            return placeholderVars;
        }

        /**
         * Creates the initial solver state for the code completion algorithm.
         *
         * @param ast the AST
         * @param specName the name of the Statix spec
         * @param rootPredicateName the name of the root predicate
         * @param placeholderVarMap the map of placeholders to variables
         * @return the initial solver state
         */
        private SolverState createInitialSolverState(ITerm ast, String specName, String rootPredicateName, PlaceholderVarMap placeholderVarMap) {
            String qualifiedName = makeQualifiedName(specName, rootPredicateName);
            IConstraint rootConstraint = new CUser(qualifiedName, Collections.singletonList(ast), null);
            return SolverState.of(
                    spec,                               // the specification
                    State.of(),                         // the new empty Statix state
                    ImmutableList.of(rootConstraint),   // list of constraints
                    Set.Immutable.of(),                 // empty set of expanded rules
                    new SolutionMeta()                  // empty solution Meta
                )
                .withExistentials(placeholderVarMap.getVars())
                .withPrecomputedCriticalEdges();
        }

        /**
         * Performs analysis on the given solver state.
         *
         * @param initialState the solver state to analyze
         * @return the resulting analyzed solver state
         * @throws IllegalStateException if the analyzed solver state has errors or has no constraints
         */
        private SolverState analyze(SolverState initialState) {
            final @Nullable SolverState analyzedState = tegoRuntime.eval(InferStrategy.getInstance(), initialState);
            if (analyzedState == null) {
                throw new IllegalStateException("Completion failed: got no result from Tego strategy.");
//            } else if(analyzedState.hasErrors()) {
//                // TODO: We can add these errors to the set of allowed errors
//                throw new IllegalStateException("Completion failed: input program validation failed:\n" + analyzedState.messagesToString());
            } else if(analyzedState.getConstraints().isEmpty()) {
                throw new IllegalStateException("Completion failed: no constraints left, nothing to complete.\n" + analyzedState);
            }
            return analyzedState;
        }

        /**
         * Completes the given placeholder in the given state to a list of terms.
         *
         * @param state the analyzed solver state
         * @param placeholder the placeholder being completed
         * @param allowedErrors the collection of allowed errors
         * @return a lazy sequence of code completion proposals
         */
        private Seq<CodeCompletionProposal> complete(SolverState state, ITermVar placeholder, Collection<Map.Entry<IConstraint, IMessage>> allowedErrors) {
            // Create a strategy that fails if the term is not an injection
            final Strategy<ITerm, @Nullable ITerm> isInjPredicate = pred(this::isInjection);

            final SolverContext ctx = new SolverContext(placeholder, allowedErrors, isInjPredicate, completeDeterministic);

            final ITerm termInUnifier = state.getState().unifier().findRecursive(placeholder);
            if (!termInUnifier.equals(placeholder)) {
                // The variable we're looking for is already in the unifier
                return Seq.of(new CodeCompletionProposal(state, termInUnifier));
            }

            // The variable we're looking for is not in the unifier
            final @Nullable Seq<SolverState> results = tegoRuntime.eval(CompleteStrategy.getInstance(), ctx, placeholder, Collections.emptySet(), state);
            if (results == null) throw new IllegalStateException("This cannot be happening.");
            // NOTE: This is the point at which the built sequence gets evaluated.
            return results.map(s -> new CodeCompletionProposal(s, s.project(placeholder)));
        }

        /**
         * Filters some proposals from the list of proposals.
         *
         * @param proposals the list of proposals
         * @return the filtered list of completion proposals
         */
        private Seq<CodeCompletionProposal> filterProposals(Seq<CodeCompletionProposal> proposals) {
            return proposals.filter(p -> //!StrategoPlaceholders.containsLiteralVar(p.getTerm())      // Don't show proposals that require a literal to be filled out, such as an ID, string literal, int literal
                !StrategoPlaceholders.isPlaceholder(p.getTerm())           // Don't show proposals of naked placeholder constructors (e.g., Exp-Plhdr())
                && !(p.getTerm() instanceof ITermVar)
            );
        }

        /**
         * Orders the list of proposals.
         *
         * @param proposals the list of proposals
         * @return the ordered list of completion proposals
         */
        private List<CodeCompletionProposal> orderProposals(List<CodeCompletionProposal> proposals) {
            return proposals.stream().sorted(Comparator
                // Sort expanded queries after expanded rules before leftovers
                .<CodeCompletionProposal, Integer>comparing(it -> it.getState().getMeta().getExpandedQueries() > 0 ? 2 : (it.getState().getMeta().getExpandedRules() > 0 ? 1 : 0))
                // Sort more expanded queries after less expanded queries
                .<Integer>thenComparing(it -> it.getState().getMeta().getExpandedQueries())
                // Sort more expanded rules after less expanded rules
                .<Integer>thenComparing(it -> it.getState().getMeta().getExpandedRules())
                // Sort solutions with higher rank after solutions with lower rank
                .<Integer>thenComparing(it -> rankTerm(it.getTerm()))
                // Reverse the whole thing
                .reversed()
            ).collect(Collectors.toList());
        }

        /**
         * Ranks the term. A higher value means a better result.
         *
         * Terms are ranked by how many concrete (non-placeholder) terms they have.
         * The deeper the term, the higher the rank.
         *
         * @param root the term to rank
         * @return the rank
         */
        private int rankTerm(ITerm root) {
            int sum = 0;
            int level = -1;
            Deque<ITerm> currWorklist = new ArrayDeque<>();
            Deque<ITerm> nextWorklist = new ArrayDeque<>();
            nextWorklist.add(root);
            while (!nextWorklist.isEmpty()) {
                Deque<ITerm> tmp = currWorklist;
                currWorklist = nextWorklist;
                nextWorklist = tmp;
                level += 1;
                while(!currWorklist.isEmpty()) {
                    final ITerm term = currWorklist.remove();
                    final int addition =  (term instanceof ITermVar ? 0 : 1 << level);
                    if (Integer.MAX_VALUE - addition < sum) return Integer.MAX_VALUE;
                    sum += addition;
                    if(term instanceof IApplTerm) {
                        nextWorklist.addAll(((IApplTerm)term).getArgs());
                    }
                }
            }
            return sum;
        }

        /**
         * Converts a list of proposals to a list of strings.
         *
         * @param proposals the list of proposals
         * @param placeholderRegion the placeholder region to be replaced
         * @return the list of completion proposals terms
         */
        private Result<List<CodeCompletionItem>, ?> proposalsToCodeCompletionItems(List<CodeCompletionProposal> proposals, Region placeholderRegion) {
            final List<CodeCompletionItem> items = new ArrayList<>();
            for (CodeCompletionProposal proposal : proposals) {
                final Result<CodeCompletionItem, ?> codeCompletionItemResult = proposalToCodeCompletionItem(proposal, placeholderRegion);
                if (codeCompletionItemResult.isErr()) return codeCompletionItemResult.ignoreValueIfErr();
                items.add(codeCompletionItemResult.unwrapUnchecked());
            }
            return Result.ofOk(items);
        }

        /**
         * Converts a proposal to a code completion item.
         *
         * @param proposal the proposal
         * @param placeholderRegion the placeholder region to be replaced
         * @return the code completion item
         */
        private Result<CodeCompletionItem, ?> proposalToCodeCompletionItem(CodeCompletionProposal proposal, Region placeholderRegion) {
            final Result<IStrategoTerm, ?> strategoTermResult = proposalToStrategoTerm(proposal);
            final Result<String, ?> textResult = proposalToStrategoTerm(proposal).flatMap(strategoTerm -> strategoTermToString(strategoTerm).ignoreErrIfOk());
            if (textResult.isErr()) return textResult.ignoreValueIfErr();
            final IStrategoTerm strategoTerm = strategoTermResult.unwrapUnchecked();
            final String text = textResult.unwrapUnchecked();
            ListView<TextEdit> textEdits = ListView.of(new TextEdit(placeholderRegion, text));
            String label = normalizeText(text);
            // TODO: Determine the style of the completion
            //  (basically, what kind of entity it represents)
            StyleName style = Objects.requireNonNull(StyleName.fromString("meta.template"));
            // TODO: Fill out the other useful fields too
            //  (basically depends on what entity it represents)
            //  This is an opportunity for rich metadata to be understood by Spoofax
            return Result.ofOk(new TermCodeCompletionItem(
                proposal.getTerm(),
                strategoTerm,
                label,
                "",
                "",
                "",
                "",
                style,
                textEdits,
                false
            ));
        }

        /**
         * Converts a proposal to its term representation.
         *
         * @param proposal the proposal
         * @return the proposal term
         */
        private Result<IStrategoTerm, ?> proposalToStrategoTerm(CodeCompletionProposal proposal) {
                final IStrategoTerm proposalTerm = strategoTerms.toStratego(proposal.getTerm(), true);
                return downgradePlaceholders(proposalTerm)
                    .flatMap((IStrategoTerm term) -> postAnalyze(term).ignoreErrIfOk());
        }

        /**
         * Converts an implicated proposal term to its string representation.
         *
         * @param implicatedTerm the implicated term of the proposal
         * @return the string representation
         */
        private Result<String, ?> strategoTermToString(IStrategoTerm implicatedTerm) {
            return prettyPrint(implicatedTerm);
        }
    }

}
