package mb.statix.codecompletion.pie;

import com.google.common.collect.ImmutableList;
import io.usethesource.capsule.Set;
import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editing.TextEdit;
import mb.common.option.Option;
import mb.common.region.Region;
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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class CodeCompletionTaskDef implements TaskDef<CodeCompletionTaskDef.Input, Option<CodeCompletionResult>> {

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
            return this.primarySelection.equals(that.primarySelection)
                && this.file.equals(that.file)
                && Objects.equals(this.rootDirectoryHint, that.rootDirectoryHint)
                && this.completeDeterministic == that.completeDeterministic;
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
            return "CodeCompletionTaskDef.Input{" +
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
    private final CodeCompletionEventHandler eventHandler;

    /**
     * Initializes a new instance of the {@link CodeCompletionTaskDef} class.
     *
     * @param parseTask the parser task
     * @param analyzeFileTask the analysis task
     * @param getStrategoRuntimeProviderTask the Stratego runtime provider task
     * @param statixSpec the Statix spec task
     * @param strategoTerms the Stratego to NaBL terms utility class
     * @param loggerFactory the logger factory
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
     * @param eventHandler the code completion event handler
     */
    public CodeCompletionTaskDef(
        JsglrParseTaskDef parseTask,
        ConstraintAnalyzeFile analyzeFileTask,
        GetStrategoRuntimeProvider getStrategoRuntimeProviderTask,
        TegoRuntime tegoRuntime,
        StatixSpecTaskDef statixSpec,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory,

        String preAnalyzeStrategyName,
        String postAnalyzeStrategyName,
        String upgradePlaceholdersStrategyName,
        String downgradePlaceholdersStrategyName,
        String isInjStrategyName,
        String ppPartialStrategyName,

        String statixSecName,
        String statixRootPredicateName,
        CodeCompletionEventHandler eventHandler
    ) {
        this.parseTask = parseTask;
        this.analyzeFileTask = analyzeFileTask;
        this.getStrategoRuntimeProviderTask = getStrategoRuntimeProviderTask;
        this.tegoRuntime = tegoRuntime;
        this.statixSpec = statixSpec;
        this.strategoTerms = strategoTerms;
        this.log = loggerFactory.create(getClass());

        this.preAnalyzeStrategyName = preAnalyzeStrategyName;
        this.postAnalyzeStrategyName = postAnalyzeStrategyName;
        this.upgradePlaceholdersStrategyName = upgradePlaceholdersStrategyName;
        this.downgradePlaceholdersStrategyName = downgradePlaceholdersStrategyName;
        this.isInjStrategyName = isInjStrategyName;
        this.ppPartialStrategyName = ppPartialStrategyName;

        this.statixSecName = statixSecName;
        this.statixRootPredicateName = statixRootPredicateName;
        this.eventHandler = eventHandler;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public Option<CodeCompletionResult> exec(ExecContext context, Input input) throws Exception {
        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProviderTask, None.instance).getValue().get();
        final Spec spec = context.require(statixSpec, None.instance).unwrap();

        final Option<CodeCompletionResult> results = new Execution(
            context, input, strategoRuntime, spec
        ).complete();
        return results;
    }

    /**
     * Keeps objects used by the code completion algorithm in a more accessible place.
     */
    private final class Execution {
        private final ExecContext context;
        private final StrategoRuntime strategoRuntime;
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
        public Option<CodeCompletionResult> complete() throws Exception {
            eventHandler.begin();

            // Parse the AST
            eventHandler.beginParse();
            final IStrategoTerm parsedAst = parse();
            eventHandler.endParse();

            // Prepare the AST (explicate, add term indices, upgrade placeholders)
            eventHandler.beginPreparation();
            final @Nullable IStrategoTerm explicatedAst = preAnalyze(parsedAst);
            if (explicatedAst == null) return Option.ofNone();
            final IStrategoTerm indexedAst = addTermIndices(explicatedAst);
            final ITerm statixAst = toStatix(indexedAst);
            final PlaceholderVarMap placeholderVarMap = new PlaceholderVarMap(file.toString());
            final @Nullable ITerm upgradedAst = upgradePlaceholders(statixAst, placeholderVarMap);
            if (upgradedAst == null) return Option.ofNone();
            final ITermVar placeholder = getCompletionPlaceholder(upgradedAst);
            final SolverState initialState = createInitialSolverState(upgradedAst, statixSecName, statixRootPredicateName, placeholderVarMap);
            eventHandler.endPreparation();

            // Analyze the AST
            eventHandler.beginAnalysis();
            final SolverState analyzedState = analyze(initialState);
            eventHandler.endAnalysis();

            // Execute the code completion Tego strategy
            eventHandler.beginCodeCompletion();
            final Seq<CodeCompletionProposal> completionProposals = complete(analyzedState, placeholder, Collections.emptyList() /* TODO: Get the set of analysis errors */);
            final Seq<CodeCompletionProposal> filteredProposals = filterProposals(completionProposals);
            final List<CodeCompletionProposal> instantiatedProposals = filteredProposals.toList(); // NOTE: This is where we actually coerce the lazy list find the completions.
            eventHandler.endCodeCompletion();

            // Get, convert, and prepare the proposals
            eventHandler.beginFinishing();
            final List<CodeCompletionProposal> orderedProposals = orderProposals(instantiatedProposals);
            final Region placeholderRegion = getRegion(placeholder, Region.atOffset(primarySelection.getStartOffset() /* TODO: Support the whole selection? */));
            final List<CodeCompletionItem> finalProposals = proposalsToCodeCompletionItems(orderedProposals, placeholderRegion);
            eventHandler.endFinishing();

            if (finalProposals.isEmpty()) {
                log.warn("Completion returned no completion proposals.");
            } else {
                log.trace("Completion returned the following proposals:\n - " + finalProposals.stream().map(i -> i.getLabel()).collect(Collectors.joining("\n - ")));
            }

            eventHandler.end();
            return Option.ofSome(new TermCodeCompletionResult(
                placeholder,
                ListView.copyOf(finalProposals),
                Objects.requireNonNull(tryGetRegion(placeholder)),
                true
            ));
        }

        /**
         * Parses the input file.
         *
         * @return the AST of the file
         * @throws JsglrParseException if parsing failed
         */
        private IStrategoTerm parse() throws JsglrParseException {
            return context.require(parseTask.inputBuilder()
                .withFile(file)
                .rootDirectoryHint(Optional.ofNullable(rootDirectoryHint))
                .buildRecoverableAstSupplier()
            ).unwrap();
        }

        /**
         * Pretty-prints the given term.
         * @param term the term to pretty-print
         * @return the pretty-printed term; or {@code null} when pretty-printing failed
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable String prettyPrint(IStrategoTerm term) throws StrategoException {
            @Nullable final IStrategoTerm output = invokeStrategy(ppPartialStrategyName, term);
            return TermUtils.asJavaString(output).orElse(null);
        }

        /**
         * Performs pre-analysis on the given AST.
         *
         * @param ast     the AST to explicate
         * @return the explicated AST; or {@code null} when explication failed
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable IStrategoTerm preAnalyze(IStrategoTerm ast) throws StrategoException {
            return invokeStrategy(preAnalyzeStrategyName, ast);
        }

        /**
         * Performs post-analysis on the given term.
         *
         * @param term     the term to implicate
         * @return the implicated AST; or {@code null} when implication failed
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable IStrategoTerm postAnalyze(IStrategoTerm term) throws StrategoException {
            return invokeStrategy(postAnalyzeStrategyName, term);
        }

        /**
         * Upgrade the placeholder terms to term variables.
         *
         * @param ast the AST to upgrade
         * @param placeholderVarMap the map to which mappings of placeholders to variables are added
         * @return the upgraded AST; or {@code null} when upgrading failed
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable ITerm upgradePlaceholders(ITerm ast, PlaceholderVarMap placeholderVarMap) throws StrategoException {
            // FIXME: Ideally we would know the sort of the placeholder
            //  so we can use that sort to call the correct downgrade-placeholders-Lang-Sort strategy
            // FIXME: We can generate: downgrade-placeholders-Lang(|sort) = where(<?"Exp"> sort); downgrade-placeholders-Lang-Exp
            return StrategoPlaceholders.replacePlaceholdersByVariables(ast, placeholderVarMap);
        }

        /**
         * Downgrades the placeholder term variables to actual placeholders.
         *
         * @param term the term to downgrade
         * @return the downgraded term; or {@code null} when downgrading failed
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable IStrategoTerm downgradePlaceholders(IStrategoTerm term) throws StrategoException {
            return invokeStrategy(downgradePlaceholdersStrategyName, term);
        }

        /**
         * Determines if the given term is an injection.
         *
         * @param term the term to check
         * @return {@code true} when the term is an injection; otherwise, {@code false}
         * @throws RuntimeException if a {@link StrategoException} occurred
         */
        private boolean isInjection(ITerm term) {
            try {
                final IStrategoTerm strategoTerm = strategoTerms.toStratego(term, true);
                @Nullable final IStrategoTerm output = invokeStrategy(isInjStrategyName, strategoTerm);
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
         * @return the term variable of the placeholder being completed
         */
        private ITermVar getCompletionPlaceholder(ITerm ast) {
            @Nullable ITermVar placeholderVar = findPlaceholderAt(ast, primarySelection.getStartOffset() /* TODO: Support the whole selection? */);
            if (placeholderVar == null) {
                throw new IllegalStateException("Completion failed: we don't know the placeholder.");
            }
            return placeholderVar;
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
            } else if(analyzedState.hasErrors()) {
                // TODO: We can add these errors to the set of allowed errors
                throw new IllegalStateException("Completion failed: input program validation failed:\n" + analyzedState.messagesToString());
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
        private List<CodeCompletionItem> proposalsToCodeCompletionItems(List<CodeCompletionProposal> proposals, Region placeholderRegion) {
            return proposals.stream().map(p -> proposalToCodeCompletionItem(p, placeholderRegion)).collect(Collectors.toList());
        }

        /**
         * Converts a proposal to a code completion item.
         *
         * @param proposal the proposal
         * @param placeholderRegion the placeholder region to be replaced
         * @return the code completion item
         */
        private CodeCompletionItem proposalToCodeCompletionItem(CodeCompletionProposal proposal, Region placeholderRegion) {
            final IStrategoTerm strategoTerm = proposalToStrategoTerm(proposal);
            final String text = strategoTermToString(strategoTerm);
            ListView<TextEdit> textEdits = ListView.of(new TextEdit(placeholderRegion, text));
            String label = normalizeText(text);
            // TODO: Determine the style of the completion
            //  (basically, what kind of entity it represents)
            StyleName style = Objects.requireNonNull(StyleName.fromString("meta.template"));
            // TODO: Fill out the other useful fields too
            //  (basically depends on what entity it represents)
            //  This is an opportunity for rich metadata to be unerstood by Spoofax
            return new TermCodeCompletionItem(proposal.getTerm(), strategoTerm, label, "", "", "", "", style, textEdits, false);
        }

        /**
         * Converts a proposal to its term representation.
         *
         * @param proposal the proposal
         * @return the proposal term
         */
        private IStrategoTerm proposalToStrategoTerm(CodeCompletionProposal proposal) {
            try {
                final IStrategoTerm proposalTerm = strategoTerms.toStratego(proposal.getTerm(), true);
                @Nullable final IStrategoTerm downgradedTerm = downgradePlaceholders(proposalTerm);
                if (downgradedTerm == null) return proposalTerm; // Return the term when downgrading failed
                @Nullable final IStrategoTerm implicatedTerm = postAnalyze(downgradedTerm);
                if (implicatedTerm == null) return downgradedTerm; // Return the term when implication failed
                return implicatedTerm;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Converts an implicated proposal term to its string representation.
         *
         * @param implicatedTerm the implicated term of the proposal
         * @return the string representation
         */
        private String strategoTermToString(IStrategoTerm implicatedTerm) {
            try {
                @Nullable final String prettyPrintedTerm = prettyPrint(implicatedTerm);
                if (prettyPrintedTerm == null) return implicatedTerm.toString(); // Return the term when pretty-printing failed
                return prettyPrintedTerm;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Invokes a Stratego strategy.
         *
         * @param strategyName    the name of the strategy to invoke
         * @param input           the input term
         * @return the resulting term; or {@code null} if the strategy failed
         * @throws StrategoException if the strategy invocation failed
         */
        private @Nullable IStrategoTerm invokeStrategy(String strategyName, IStrategoTerm input) throws StrategoException {
            return invokeStrategy(strategyName, input, ListView.of());
        }

        /**
         * Invokes a Stratego strategy.
         *
         * @param strategyName    the name of the strategy to invoke
         * @param input           the input term
         * @param arguments       the term arguments
         * @return the resulting term; or {@code null} if the strategy failed
         * @throws StrategoException if the strategy invocation failed
         */
        private @Nullable IStrategoTerm invokeStrategy(String strategyName, IStrategoTerm input, IStrategoTerm... arguments) throws StrategoException {
            return invokeStrategy(strategyName, input, ListView.of(arguments));
        }

        /**
         * Invokes a Stratego strategy.
         *
         * @param strategyName    the name of the strategy to invoke
         * @param input           the input term
         * @param arguments       the term arguments
         * @return the resulting term; or {@code null} if the strategy failed
         * @throws StrategoException if the strategy invocation failed
         */
        private @Nullable IStrategoTerm invokeStrategy(String strategyName, IStrategoTerm input, Iterable<IStrategoTerm> arguments) throws StrategoException {
            return invokeStrategy(strategyName, input, iterableToListView(arguments));
        }

        /**
         * Invokes a Stratego builder.
         *
         * A builder strategy accepts a tuple of {@code (selection: Term, position: List, ast: Term, filePath: String, projectPath: String}.
         *
         * @param builderName     the name of the strategy to invoke
         * @param input           the input term
         * @return the resulting term; or {@code null} if the strategy failed
         * @throws StrategoException if the strategy invocation failed
         */
        private @Nullable IStrategoTerm invokeBuilder(String builderName, IStrategoTerm input) throws StrategoException {
            final IStrategoTerm builderInputTerm = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), input, file.asString(), rootDirectoryHint != null ? rootDirectoryHint.asString() : "");
            return invokeStrategy(builderName, builderInputTerm);
        }

        /**
         * Invokes a Stratego strategy.
         *
         * @param strategyName    the name of the strategy to invoke
         * @param input           the input term
         * @param arguments       the term arguments
         * @return the resulting term; or {@code null} if the strategy failed
         * @throws StrategoException if the strategy invocation failed
         */
        private @Nullable IStrategoTerm invokeStrategy(String strategyName, IStrategoTerm input, ListView<IStrategoTerm> arguments) throws StrategoException {
            try {
                if(arguments.isEmpty()) {
                    return strategoRuntime.invokeOrNull(strategyName, input);
                } else {
                    return strategoRuntime.invokeOrNull(strategyName, input, arguments);
                }
            } catch (StrategoException ex) {
                throw ex;
            }
        }
    }

}
