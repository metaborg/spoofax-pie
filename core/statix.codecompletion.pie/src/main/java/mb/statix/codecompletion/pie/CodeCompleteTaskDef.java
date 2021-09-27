package mb.statix.codecompletion.pie;

import com.google.common.collect.ImmutableList;
import io.usethesource.capsule.Set;
import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editing.TextEdit;
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
import mb.nabl2.terms.ListTerms;
import mb.nabl2.terms.Terms;
import mb.nabl2.terms.stratego.PlaceholderVarMap;
import mb.nabl2.terms.stratego.StrategoPlaceholders;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.nabl2.terms.stratego.TermPlaceholder;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.CodeCompletionProposal;
import mb.statix.SolutionMeta;
import mb.statix.SolverContext;
import mb.statix.SolverState;
import mb.statix.codecompletion.strategies.runtime.CompleteStrategy;
import mb.statix.codecompletion.strategies.runtime.InferStrategy;
import mb.statix.constraints.CUser;
import mb.statix.constraints.messages.IMessage;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoExceptions;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.StrategoUtil;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.tego.pie.GetTegoRuntimeProvider;
import mb.tego.sequences.Seq;
import mb.tego.strategies.Strategy;
import mb.tego.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.util.TermUtils;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static mb.tego.strategies.StrategyExt.pred;

/**
 * Code completion task definition.
 */
public class CodeCompleteTaskDef implements TaskDef<CodeCompleteTaskDef.Args, @Nullable CodeCompletionResult> {

    public static class Args implements Serializable {
        /** The root directory of the project. */
        public final ResourcePath rootDirectory;
        /** The file being completed. */
        public final ResourceKey file;
        /** The primary selection at which to complete. */
        public final Region primarySelection;

        /**
         * Initializes a new instance of the {@link CodeCompleteTaskDef.Args} class.
         * @param rootDirectory the root directory of the project
         * @param file the file being completed
         * @param primarySelection the primary selection at which to complete
         */
        public Args(ResourcePath rootDirectory, ResourceKey file, Region primarySelection) {
            this.rootDirectory = rootDirectory;
            this.file = file;
            this.primarySelection = primarySelection;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            return equals((CodeCompleteTaskDef.Args)o);
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
        protected boolean equals(CodeCompleteTaskDef.Args that) {
            if (this == that) return true;
            return this.rootDirectory.equals(that.rootDirectory)
                && this.file.equals(that.file)
                && this.primarySelection.equals(that.primarySelection);
        }

        @Override public int hashCode() {
            return Objects.hash(
                this.rootDirectory,
                this.file,
                this.primarySelection
            );
        }

        @Override public String toString() {
            return "CodeCompleteTaskDef.Args{" +
                "rootDirectory=" + rootDirectory + ", " +
                "file=" + file + ", " +
                "primarySelection=" + primarySelection +
                "}";
        }
    }

    private final Logger log;
    private final JsglrParseTaskDef parseTask;
    private final ConstraintAnalyzeFile analyzeFileTask;
    private final StrategoTerms strategoTerms;
    private final GetStrategoRuntimeProvider getStrategoRuntimeProviderTask;
    private final GetTegoRuntimeProvider getTegoRuntimeProviderTask;

    /**
     * Initializes a new instance of the {@link CodeCompleteTaskDef} class.
     *
     * @param parseTask the parser task
     * @param analyzeFileTask the analysis task
     * @param getStrategoRuntimeProviderTask the Stratego runtime provider task
     * @param getTegoRuntimeProviderTask the Tego runtime provider task
     * @param strategoTerms the Stratego to NaBL terms utility class
     * @param loggerFactory the logger factory
     */
    public CodeCompleteTaskDef(
        JsglrParseTaskDef parseTask,
        ConstraintAnalyzeFile analyzeFileTask,
        GetStrategoRuntimeProvider getStrategoRuntimeProviderTask,
        GetTegoRuntimeProvider getTegoRuntimeProviderTask,
        //StatixCompileSpec compileSpec,
        StrategoTerms strategoTerms,
        LoggerFactory loggerFactory
    ) {
        this.parseTask = parseTask;
        this.analyzeFileTask = analyzeFileTask;
        this.getStrategoRuntimeProviderTask = getStrategoRuntimeProviderTask;
        this.getTegoRuntimeProviderTask = getTegoRuntimeProviderTask;
        this.strategoTerms = strategoTerms;
        this.log = loggerFactory.create(getClass());
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public @Nullable CodeCompletionResult exec(ExecContext context, Args input) throws Exception {
        // FIXME: Do we need this?
//        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
//        context.require(classLoaderResources.tryGetAsLocalResource(StatixEvaluateTest.Args.class), ResourceStampers.hashFile());

        final StrategoRuntime strategoRuntime = context.require(getStrategoRuntimeProviderTask, None.instance).getValue().get();
        final TegoRuntime tegoRuntime = context.require(getTegoRuntimeProviderTask, None.instance).getValue().get();
        final Spec spec = null; // TODO: Get spec from StatixCompileSpec. This is the merged spec AST, converted to a Spec object

        return new Execution(
            context, input, strategoRuntime, tegoRuntime, spec
        ).complete();
    }

    /**
     * Keeps objects used by the code completion algorithm in a more accessible place.
     */
    private final class Execution {
        private final ExecContext context;
        private final StrategoRuntime strategoRuntime;
        private final TegoRuntime tegoRuntime;
        private final ITermFactory termFactory;
        /** The root directory of the project. */
        public final ResourcePath rootDirectory;
        /** The file being completed. */
        public final ResourceKey file;
        /** The primary selection at which to complete. */
        public final Region primarySelection;
        /** The Statix specification. */
        private final Spec spec;

        /**
         * Initializes a new instance of the {@link Execution} class.
         *
         * @param context the execution context
         * @param args the task arguments
         * @param strategoRuntime the Stratego runtime
         * @param tegoRuntime the Tego runtime
         * @param spec the Statix specification
         */
        public Execution(
            ExecContext context,
            Args args,
            StrategoRuntime strategoRuntime,
            TegoRuntime tegoRuntime,
            Spec spec
        ) {
            this.context = context;
            this.strategoRuntime = strategoRuntime;
            this.tegoRuntime = tegoRuntime;
            this.spec = spec;
            this.termFactory = strategoRuntime.getTermFactory();
            this.rootDirectory = args.rootDirectory;
            this.file = args.file;
            this.primarySelection = args.primarySelection;
        }

        /**
         * Performs code completion.
         *
         * @return the code completion result
         * @throws Exception if an exception occurred
         */
        public @Nullable CodeCompletionResult complete() throws Exception {
            // Get, prepare, and analyze the incoming AST
            final IStrategoTerm parsedAst = parse();
            final IStrategoTerm explicatedAst = explicate(parsedAst);
            final IStrategoTerm indexedAst = addTermIndices(explicatedAst);
            final ITerm statixAst = toStatix(indexedAst);
            final PlaceholderVarMap placeholderVarMap = new PlaceholderVarMap(file.toString());
            final ITerm upgradedAst = upgradePlaceholders(statixAst, placeholderVarMap);
            final ITermVar placeholder = getCompletionPlaceholder(upgradedAst);
            // TODO: Specify spec name and root rule name somewhere
            final SolverState initialState = createInitialSolverState(upgradedAst, "statics", "programOk", placeholderVarMap);
            final SolverState analyzedState = analyze(initialState);

            // Execute the code completion Tego strategy
            final Seq<CodeCompletionProposal> completionProposals = complete(analyzedState, placeholder, Collections.emptyList() /* TODO: Get the set of analysis errors */);
            final Seq<CodeCompletionProposal> filteredProposals = filterProposals(completionProposals);

            // Get, convert, and prepare the proposals
            final List<CodeCompletionProposal> instantiatedProposals = filteredProposals.toList(); // NOTE: This is where we actually coerce the lazy list find the completions.
            final List<CodeCompletionProposal> orderedProposals = orderProposals(instantiatedProposals);
            final List<CodeCompletionItem> finalProposals = proposalsToCodeCompletionItems(orderedProposals);

            if (finalProposals.isEmpty()) {
                log.warn("Completion returned no completion proposals.");
            }

            return new CodeCompletionResult(
                ListView.copyOf(finalProposals),
                Objects.requireNonNull(getRegion(placeholder)),
                true
            );
        }

        /**
         * Parses the input file.
         *
         * @return the AST of the file
         * @throws JsglrParseException if parsing failed
         */
        private IStrategoTerm parse() throws JsglrParseException {
            return context.require(parseTask.inputBuilder().withFile(file).buildAstSupplier()).unwrap();
        }

        /**
         * Pretty-prints the given term.
         * @param term the term to pretty-print
         * @return the pretty-printed term
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable String prettyPrint(IStrategoTerm term) throws StrategoException {
            // TODO: Make this strategy name not language specific
            @Nullable final IStrategoTerm prettyPrintedTerm = invokeStrategy("pp-partial-Tiger-string", term);
            return prettyPrintedTerm != null ? TermUtils.asJavaString(prettyPrintedTerm).get() : null;
        }

        /**
         * Explicates the given AST.
         *
         * @param ast     the AST to explicate
         * @return the explicated AST
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private IStrategoTerm explicate(IStrategoTerm ast) throws StrategoException {
            // TODO: Make this strategy name not language specific
            @Nullable final IStrategoTerm explicatedAst = invokeStrategy("explicate", ast);
            if(explicatedAst == null)
                throw new IllegalStateException("Completion failed: we did not get an explicated AST.");
            return explicatedAst;
        }

        /**
         * Implicates the given term.
         *
         * @param term     the term to implicate
         * @return the implicated AST; or {@code null} when implication failed
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable IStrategoTerm implicate(IStrategoTerm term) throws StrategoException {
            // TODO: Make this strategy name not language specific
            @Nullable final IStrategoTerm implicatedAst = invokeStrategy("implicate", term);
            return implicatedAst;
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
         * Upgrade the placeholder terms to term variables.
         *
         * @param ast the AST to upgrade
         * @param placeholderVarMap the map to which mappings of placeholders to variables are added
         * @return the upgraded AST
         */
        private ITerm upgradePlaceholders(ITerm ast, PlaceholderVarMap placeholderVarMap) {
            // FIXME: Ideally we would know the sort of the placeholder
            //  so we can use that sort to call the correct downgrade-placeholders-Lang-Sort strategy
            // FIXME: We can generate: downgrade-placeholders-Lang(|sort) = where(<?"Exp"> sort); downgrade-placeholders-Lang-Exp
            return StrategoPlaceholders.replacePlaceholdersByVariables(ast, placeholderVarMap);
        }

        /**
         * Downgrades the placeholder term variables to actual placeholders.
         *
         * @param term the term to downgrade
         * @return the downgraded term; or {@code null when downgrading failed}
         * @throws StrategoException if an error occurred while invoking the Stratego strategy
         */
        private @Nullable IStrategoTerm downgradePlaceholders(IStrategoTerm term) throws StrategoException {
            // TODO: Make this strategy name not language specific
            @Nullable final IStrategoTerm downgradedTerm = invokeStrategy("downgrade-placeholders-Tiger", term);
            return downgradedTerm;
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

            final SolverContext ctx = new SolverContext(placeholder, allowedErrors, isInjPredicate);

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
         * @return the list of completion proposals terms
         */
        private List<CodeCompletionItem> proposalsToCodeCompletionItems(List<CodeCompletionProposal> proposals) {
            return proposals.stream().map(p -> proposalToCodeCompletionItem(p)).collect(Collectors.toList());
        }

        /**
         * Converts a proposal to a code completion item.
         *
         * @param proposal the proposal
         * @return the code completion item
         */
        private CodeCompletionItem proposalToCodeCompletionItem(CodeCompletionProposal proposal) {
            final String text = proposalToString(proposal);
            ListView<TextEdit> textEdits = ListView.of(new TextEdit(Region.atOffset(primarySelection.getStartOffset() /* TODO: Support the whole selection? */), text));
            String label = normalizeText(text);
            // TODO: Determine the style of the completion
            //  (basically, what kind of entity it represents)
            StyleName style = Objects.requireNonNull(StyleName.fromString("meta.template"));
            // TODO: Fill out the other useful fields too
            //  (basically depends on what entity it represents)
            //  This is an opportunity for rich metadata to be unerstood by Spoofax
            return new CodeCompletionItem(label, "", "", "", "", style, textEdits, false);
        }

        /**
         * Converts a proposal to its string representation.
         *
         * @param proposal the proposal
         * @return the string representation
         */
        private String proposalToString(CodeCompletionProposal proposal) {
            try {
                final IStrategoTerm proposalTerm = strategoTerms.toStratego(proposal.getTerm(), true);
                @Nullable final IStrategoTerm downgradedTerm = downgradePlaceholders(proposalTerm);
                if (downgradedTerm == null) return proposal.toString(); // Return the term when downgrading failed
                @Nullable final IStrategoTerm implicatedTerm = implicate(downgradedTerm);
                if (implicatedTerm == null) return downgradedTerm.toString(); // Return the term when implication failed
                @Nullable final String prettyPrintedTerm = prettyPrint(implicatedTerm);
                if (prettyPrintedTerm == null) return implicatedTerm.toString(); // Return the term when pretty-printing failed
                return prettyPrintedTerm;
            } catch (StrategoException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Determines if the given term is an injection.
         *
         * @param term the term to check
         * @return {@code true} when the term is an injection; otherwise, {@code false}
         * @throws RuntimeException if a {@link StrategoException} occurred
         */
        private boolean isInjection(ITerm term) {
            final IStrategoTerm strategoTerm = strategoTerms.toStratego(term, true);
            try {
                // TODO: Make this strategy name not language specific
                @Nullable final IStrategoTerm result = invokeStrategy("is-Tiger-inj-cons", strategoTerm);
                return result != null;
            } catch(StrategoException ex) {
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
            final IStrategoTerm builderInputTerm = StrategoUtil.createLegacyBuilderInputTerm(strategoRuntime.getTermFactory(), input, file.asString(), rootDirectory.asString());
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
                    return strategoRuntime.invoke(strategyName, input);
                } else {
                    return strategoRuntime.invoke(strategyName, input, arguments);
                }
            } catch (StrategoException ex) {
                if (isStrategyFailException(ex)) return null;
                throw ex;
            }
        }
    }

    /**
     * Replaces all strings of layout (newlines, spaces) with a single space.
     *
     * @param text the text to normalize
     * @return the normalized text
     */
    private static String normalizeText(String text) {
        // TODO: We should probably support using the template's whitespace
        return text.replaceAll("\\s+", " ");
    }

    /**
     * Returns the qualified name of the rule.
     *
     * @param specName the name of the specification
     * @param ruleName the name of the rule
     * @return the qualified name of the rule, in the form of {@code &lt;specName&gt;!&lt;ruleName&gt;}.
     */
    private static String makeQualifiedName(String specName, String ruleName) {
        if (specName.equals("") || ruleName.contains("!")) return ruleName;
        return specName + "!" + ruleName;
    }

    /**
     * Determines whether the exception is a {@code StrategyFail}.
     *
     * Because of the annoying use of ADTs, it is impossible to catch or do an {@code instanceOf} to check
     * the type of exception. It is also not possible to throw a checked exception from a lambda that doesn't
     * support this (which, of course, the ADT generated lambda's don't support). So we have to jump though these
     * hoops to get the one type of exception we're interested in. Probably not good for performance either.
     *
     * @param ex the exception to check
     * @return {@code true} if the exception is an {@cpde StrategyFail}; otherwise, {@code false}
     */
    private static boolean isStrategyFailException(StrategoException ex) {
        return ex.match(StrategoExceptions.cases(
            (strategyName, input, trace) -> true,
            (strategyName, input, trace) -> false,
            (strategyName, input, trace, term) -> false,
            (strategyName, input, trace, exitCode) -> false,
            (strategyName, input, trace, undefinedStrategyName) -> false,
            (strategyName, input, trace, cause) -> false
        ));
    }

    /**
     * Finds the placeholder near the caret location in the specified term.
     *
     * This method assumes all terms in the term are uniquely identifiable,
     * for example through a term index or unique tree path.
     *
     * @param term the term (an AST with placeholders)
     * @param caretOffset the caret location
     * @return the placeholder; or {@code null} if not found
     */
    private static @Nullable ITermVar findPlaceholderAt(ITerm term, int caretOffset) {
        if (!termContainsCaret(term, caretOffset)) return null;
        // Recurse into the term
        return term.match(Terms.cases(
            (appl) -> appl.getArgs().stream().map(a -> findPlaceholderAt(a, caretOffset)).filter(Objects::nonNull).findFirst().orElse(null),
            (list) -> list.match(ListTerms.cases(
                (cons) -> {
                    @Nullable final ITermVar headMatch = findPlaceholderAt(cons.getHead(), caretOffset);
                    if (headMatch != null) return headMatch;
                    return findPlaceholderAt(cons.getTail(), caretOffset);
                },
                (nil) -> null,
                (var) -> null
            )),
            (string) -> null,
            (integer) -> null,
            (blob) -> null,
            (var) -> isPlaceholder(var) ? var : null
        ));
    }

    /**
     * Determines whether the specified term contains the specified caret offset.
     *
     * @param term the term
     * @param caretOffset the caret offset to find
     * @return {@code true} when the term contains the caret offset;
     * otherwise, {@code false}.
     */
    private static boolean termContainsCaret(ITerm term, int caretOffset) {
        @Nullable Region region = getRegion(term);
        if (region == null) {
            // One of the children must contain the caret
            return term.match(Terms.cases(
                (appl) -> appl.getArgs().stream().anyMatch(a -> termContainsCaret(a, caretOffset)),
                (list) -> list.match(ListTerms.cases(
                    (cons) -> {
                        final boolean headContains = termContainsCaret(cons.getHead(), caretOffset);
                        if (headContains) return true;
                        return termContainsCaret(cons.getTail(), caretOffset);
                    },
                    (nil) -> false,
                    (var) -> false
                )),
                (string) -> false,
                (integer) -> false,
                (blob) -> false,
                (var) -> false
            ));
        }
        return region.contains(caretOffset);
    }

    /**
     * Gets the region occupied by the specified term.
     *
     * @param term the term
     * @return the term's region; or {@code null} when it could not be determined
     */
    private static @Nullable Region getRegion(ITerm term) {
        @Nullable final TermOrigin origin = TermOrigin.get(term).orElse(null);
        if (origin == null) return null;
        final ImploderAttachment imploderAttachment = origin.getImploderAttachment();
        // We get the zero-based offset of the first character in the token
        int startOffset = imploderAttachment.getLeftToken().getStartOffset();
        // We get the zero-based offset of the character following the token, which is why we have to add 1
        int endOffset = imploderAttachment.getRightToken().getEndOffset() + 1;
        // If the token is empty or malformed, we skip it. (An empty token cannot contain a caret anyway.)
        if (endOffset <= startOffset) return null;

        return Region.fromOffsets(
            startOffset,
            endOffset
        );
    }

    /**
     * Determines whether the given term variable is a placeholder term variable.
     *
     * @param var the term variable
     * @return {@code true} when the term variabnle is a placeholder term variable;
     * otherwise, {@code false}
     */
    private static boolean isPlaceholder(ITermVar var) {
        return TermPlaceholder.has(var);
    }

    /**
     * Converts an iterable to a list.
     *
     * @param iterable the iterable to convert
     * @param <T>      the type of elements in the iterable
     * @return a list with the elements of the iterable
     */
    private static <T> List<T> iterableToList(Iterable<T> iterable) {
        if(iterable instanceof List) {
            // It's already a List<T>.
            return (List<T>)iterable;
        } else if(iterable instanceof Collection) {
            // It's a Collection<T>.
            return new ArrayList<>((Collection<T>)iterable);
        } else {
            final Iterator<T> iterator = iterable.iterator();
            if(!iterator.hasNext()) {
                // It's an empty Iterable<T>.
                return Collections.emptyList();
            } else {
                // It's a non-empty Iterable<T>.
                final ArrayList<T> list = new ArrayList<>();
                while(iterator.hasNext()) {
                    list.add(iterator.next());
                }
                return list;
            }
        }
    }

    /**
     * Converts an iterable to a {@link ListView}.
     *
     * @param iterable the iterable to convert
     * @param <T>      the type of elements in the iterable
     * @return a list with the elements of the iterable
     */
    private static <T> ListView<T> iterableToListView(Iterable<T> iterable) {
        if(iterable instanceof ListView) {
            // It's already a ListView<T>.
            return (ListView<T>)iterable;
        } else {
            // It's something else.
            return new ListView<T>(iterableToList(iterable));
        }
    }
}
