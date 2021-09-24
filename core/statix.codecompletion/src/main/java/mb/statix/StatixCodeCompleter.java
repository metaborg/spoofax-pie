package mb.statix;

import com.google.common.collect.ImmutableList;
import io.usethesource.capsule.Set;
import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.editing.TextEdit;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.style.StyleName;
import mb.common.util.ListView;
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
import mb.resource.ResourceKey;
import mb.statix.codecompletion.strategies.runtime.CompleteStrategy;
import mb.statix.codecompletion.strategies.runtime.InferStrategy;
import mb.statix.constraints.CUser;
import mb.statix.constraints.messages.IMessage;
import mb.statix.sequences.Seq;
import mb.statix.solver.IConstraint;
import mb.statix.solver.persistent.State;
import mb.statix.spec.Spec;
import mb.statix.strategies.Strategy;
import mb.statix.strategies.runtime.TegoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mb.statix.strategies.StrategyExt.pred;

/**
 * Performs code completion using Statix.
 */
public abstract class StatixCodeCompleter {

    private final Logger log;

    private final StrategoTerms strategoTerms;
    private final ITermFactory termFactory;
    private final TegoRuntime tegoRuntime;

    /**
     * Initializes a new instance of the {@link StatixCodeCompleter} class.
     *
     * @param loggerFactory the logger factory
     */
    public StatixCodeCompleter(
        StrategoTerms strategoTerms,
        ITermFactory termFactory,
        TegoRuntime tegoRuntime,
        LoggerFactory loggerFactory
    ) {
        this.strategoTerms = strategoTerms;
        this.termFactory = termFactory;
        this.tegoRuntime = tegoRuntime;
        this.log = loggerFactory.create(getClass());
    }

    /**
     *
     * @param ast the explicated AST of the file, with placeholders
     * @param primarySelection
     * @param resource
     * @return
     */
    public @Nullable CodeCompletionResult complete(Spec spec, IStrategoTerm ast, Region primarySelection, ResourceKey resource) {
        final int caretLocation = primarySelection.getStartOffset();

        @Nullable final IStrategoTerm explicatedAst = explicate(ast).ifErr(ex ->
            log.error("Completion failed: we did not get an explicated AST.", ex)
        ).get();
        if (explicatedAst == null) return null; // Cannot complete when we don't get an explicated AST.

        // Convert to Statix AST
        IStrategoTerm annotatedAst = StrategoTermIndices.index(explicatedAst, resource.toString(), termFactory);
        ITerm tmpStatixAst = strategoTerms.fromStratego(annotatedAst);
        PlaceholderVarMap placeholderVarMap = new PlaceholderVarMap(resource.toString());
        // FIXME: Ideally we would know the sort of the placeholder
        //  so we can use that sort to call the correct downgrade-placeholders-Lang-Sort strategy
        // FIXME: We can generate: downgrade-placeholders-Lang(|sort) = where(<?"Exp"> sort); downgrade-placeholders-Lang-Exp
        ITerm statixAst = StrategoPlaceholders.replacePlaceholdersByVariables(tmpStatixAst, placeholderVarMap);
        @Nullable ITermVar placeholderVar = findPlaceholderAt(statixAst, caretLocation);
        if (placeholderVar == null) {
            log.error("Completion failed: we don't know the placeholder.");
            return null;   // Cannot complete when we don't know the placeholder.
        }

        // 4) Get the solver state of the program (whole project),
        //    which should have some remaining constraints on the placeholder.
        //    TODO: What to do when the file is semantically incorrect? Recovery?

        List<IStrategoTerm> completionTerms;
        final Path debugPath = Paths.get("/Users/daniel/repos/spoofax3/devenv-cc/debug.yml");
        System.out.println("DEBUG path: " + debugPath.toAbsolutePath());
        // TODO: Specify spec name and root rule name somewhere
        SolverState startState = createStartState(spec, statixAst, "statics", "programOk")
            .withExistentials(placeholderVarMap.getVars())
            .withPrecomputedCriticalEdges();
        SolverState initialState = analyze(startState);
        if(initialState.hasErrors()) {
            log.error("Completion failed: input program validation failed.\n" + initialState.toString());
            return null;    // Cannot complete when analysis fails.
        }
        if(initialState.getConstraints().isEmpty()) {
            log.error("Completion failed: no constraints left, nothing to complete.\n" + initialState.toString());
            return null;    // Cannot complete when there are no constraints left.
        }

        // 5) Invoke the completer on the solver state, indicating the placeholder for which we want completions
        // 6) Get the possible completions back, as a list of ASTs with new solver states
        completionTerms = completeToTerms(placeholderVar, /* TODO: Allowed errors */ Collections.emptyList(), initialState);

        // 7) Format each completion as a proposal, with pretty-printed text
        List<String> completionStrings = completionTerms.stream().map(proposal -> {
            // TODO: We should call the correct downgrade-placeholders-Lang-Sort based on the
            //  sort of the placeholder.

            @Nullable final IStrategoTerm downgradedTerm = downgrade(proposal).ifErr(ex ->
                log.error("Downgrading failed on proposal: " + proposal, ex)
            ).get();
            if (downgradedTerm == null) return proposal.toString(); // Return the term when downgrading failed

            @Nullable final IStrategoTerm implicatedTerm = implicate(downgradedTerm).ifErr(ex ->
                log.error("Implication failed on downgraded: " + downgradedTerm + "\nFrom proposal: " + proposal, ex)
            ).get();
            if (implicatedTerm == null) return downgradedTerm.toString(); // Return the term when implication failed

            @Nullable final String prettyPrinted = prettyPrint(implicatedTerm).ifErr(ex ->
                log.warn("Pretty-printing failed on implicated: " + implicatedTerm + "\nFrom downgraded: " + downgradedTerm + "\nFrom proposal: " + proposal, ex)
            ).get();
            if (implicatedTerm == null) return implicatedTerm.toString(); // Return the term when pretty-printing failed

            return prettyPrinted;
        }).collect(Collectors.toList());

        // 8) Insert the selected completion: insert the pretty-printed text in the code,
        //    and (maybe?) add the solver state to the current solver state
        List<CodeCompletionItem> completionItems = completionStrings.stream().map(s -> createCodeCompletionItem(s, caretLocation)).collect(Collectors.toList());

        if (completionItems.isEmpty()) {
            log.warn("Completion returned no completion proposals.");
        }

        return new CodeCompletionResult(ListView.copyOf(completionItems), Objects.requireNonNull(getRegion(placeholderVar)), true);
    }


    private SolverState createStartState(Spec spec, ITerm statixAst, String specName, String rootRuleName) {
        IConstraint rootConstraint = getRootConstraint(statixAst, specName, rootRuleName);
        return SolverState.of(spec, State.of(), ImmutableList.of(rootConstraint), Set.Immutable.of(), new SolutionMeta());
    }

    /**
     * Gets the root constraint of the specification.
     *
     * @return the root constraint
     */
    private IConstraint getRootConstraint(ITerm statixAst, String specName, String rootRuleName) {
        String qualifiedName = makeQualifiedName(specName, rootRuleName);
        return new CUser(qualifiedName, Collections.singletonList(statixAst), null);
    }

    /**
     * Returns the qualified name of the rule.
     *
     * @param specName the name of the specification
     * @param ruleName the name of the rule
     * @return the qualified name of the rule, in the form of {@code <specName>!<ruleName>}.
     */
    private String makeQualifiedName(String specName, String ruleName) {
        if (specName.equals("") || ruleName.contains("!")) return ruleName;
        return specName + "!" + ruleName;
    }

    /**
     * Invokes the {@code infer} strategy.
     *
     * @param state the solver state
     * @return the resulting solver state after inference
     */
    public SolverState analyze(SolverState state) {
        final @Nullable SolverState result = tegoRuntime.eval(InferStrategy.getInstance(), state);
        if (result == null) throw new IllegalStateException("This cannot be happening.");
        return result;
    }

    /**
     * Invokes the {@code complete} strategy.
     *
     * @param v the placeholder variable being expanded
     * @param allowedErrors the set of allowed errors
     * @param state the initial state
     * @return the resulting terms after completion
     */
    private List<IStrategoTerm> completeToTerms(
        ITermVar v,
        Collection<Map.Entry<IConstraint, IMessage>> allowedErrors,
        SolverState state
    ) {
        final List<CodeCompletionProposal> proposals;
        try {
            // NOTE: This is the point at which the built sequence gets evaluated.
            proposals = completeToProposals(v, allowedErrors, state).toList();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

        return orderProposals(proposals.stream()
            .filter(p -> //!StrategoPlaceholders.containsLiteralVar(p.getTerm())      // Don't show proposals that require a literal to be filled out, such as an ID, string literal, int literal
                !StrategoPlaceholders.isPlaceholder(p.getTerm())           // Don't show proposals of naked placeholder constructors (e.g., Exp-Plhdr())
                && !(p.getTerm() instanceof ITermVar))                        // Don't show proposals of naked term variables (e.g., $Exp0, which would become a Stratego placeholder eventually)
            )
            .map(p -> strategoTerms.toStratego(p.getTerm(), true))
            .collect(Collectors.toList());
    }

    /**
     * Invokes the {@code complete} strategy,
     * and converts the resulting solver states into code completion proposals.
     *
     * @param v the placeholder variable being expanded
     * @param allowedErrors the set of allowed errors
     * @param state the initial state
     * @return the lazy sequence of code completion proposals
     */
    private Seq<CodeCompletionProposal> completeToProposals(
        ITermVar v,
        Collection<Map.Entry<IConstraint, IMessage>> allowedErrors,
        SolverState state
    ) {
        final Strategy<ITerm, @Nullable ITerm> isInjPredicate = pred(t -> {
            final IStrategoTerm st = strategoTerms.toStratego(t, true);

            @Nullable final IStrategoTerm result = isInj(st).ifErr(ex ->
                log.error("Could not determine if term is an injection: " + st, ex)
            ).get();
            return result != null;
        });

        final SolverContext ctx = new SolverContext(v, allowedErrors, isInjPredicate);

        final ITerm termInUnifier = state.getState().unifier().findRecursive(v);
        if (!termInUnifier.equals(v)) {
            // The variable we're looking for is already in the unifier
            return Seq.of(new CodeCompletionProposal(state, termInUnifier));
        }

        // The variable we're looking for is not in the unifier
        final @Nullable Seq<SolverState> results = tegoRuntime.eval(CompleteStrategy.getInstance(), ctx, v, Collections.emptySet(), state);
        if (results == null) throw new IllegalStateException("This cannot be happening.");
        // NOTE: This is the point at which the built sequence gets evaluated.
        return results.map(s -> new CodeCompletionProposal(s, s.project(v)));
    }

    /**
     * Creates a completion proposal.
     *
     * @param text the text to insert
     * @param caretOffset the caret location
     * @return the created proposal
     */
    private CodeCompletionItem createCodeCompletionItem(String text, int caretOffset) {
        ListView<TextEdit> textEdits = ListView.of(new TextEdit(Region.atOffset(caretOffset), text));
        String label = normalizeText(text);
        StyleName style = Objects.requireNonNull(StyleName.fromString("meta.template"));
        return new CodeCompletionItem(label, "", "", "", "", style, textEdits, false);
    }

    private String normalizeText(String text) {
        // Replace all sequences of layout with a single space
        return text.replaceAll("\\s+", " ");
    }

    // TODO: This should be part of the completion strategy, so we can check that our ordering puts the most likely candidates first
    //  when doing the completeness tests.
    /**
     * Orders the proposals.
     *
     * We want to order deeper terms before shallower ones,
     * and terms with less placeholders before terms with more placeholders.
     *
     * @param proposals the proposals to order
     * @return the ordered proposals
     */
    private Stream<CodeCompletionProposal> orderProposals(Stream<CodeCompletionProposal> proposals) {
        return proposals.sorted(Comparator
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
        );
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
     * Finds the placeholder near the caret location in the specified term.
     *
     * This method assumes all terms in the term are uniquely identifiable,
     * for example through a term index or unique tree path.
     *
     * @param term the term (an AST with placeholders)
     * @param caretOffset the caret location
     * @return the placeholder; or {@code null} if not found
     */
    private @Nullable ITermVar findPlaceholderAt(ITerm term, int caretOffset) {
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

    private boolean isPlaceholder(ITermVar var) {
        return TermPlaceholder.has(var);
    }

    /**
     * Determines whether the specified term contains the specified caret offset.
     *
     * @param term the term
     * @param caretOffset the caret offset to find
     * @return {@code true} when the term contains the caret offset;
     * otherwise, {@code false}.
     */
    private boolean termContainsCaret(ITerm term, int caretOffset) {
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

    protected abstract Result<IStrategoTerm, ?> explicate(IStrategoTerm term);

    protected abstract Result<IStrategoTerm, ?> implicate(IStrategoTerm term);

    protected abstract Result<IStrategoTerm, ?> upgrade(IStrategoTerm term);

    protected abstract Result<IStrategoTerm, ?> downgrade(IStrategoTerm term);

    protected abstract Result<IStrategoTerm, ?> isInj(IStrategoTerm term);

    protected abstract Result<String, ?> prettyPrint(IStrategoTerm term);


}
