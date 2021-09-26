package mb.statix.codecompletion;

//import mb.statix.common.PlaceholderVarMap;
//import mb.statix.common.SolverContext;
//import mb.statix.common.SolverState;
//import mb.statix.common.StatixAnalyzer;
//import mb.statix.common.StatixSpec;
//import mb.statix.common.strategies.InferStrategy;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.fail;
//
///**
// * Tests that the completion algorithm is complete.
// * For a given AST, it must be able to regenerate that AST in a number of completion steps,
// * when presented with the AST with a hole in it.
// */
//@SuppressWarnings("SameParameterValue")
//public abstract class CompletenessTest {
//
//    private static final SLF4JLoggerFactory loggerFactory = new SLF4JLoggerFactory();
//    private static final Logger log = loggerFactory.create(CompletenessTest.class);
//    protected static final String TESTPATH = "/mb/statix/completions";
//
//
//    /**
//     * Creates a completion test.
//     *
//     * @param expectedTermPath the resource path to a file with the expected Stratego ATerm
//     * @param inputTermPath the resource path to a file with the input Stratego ATerm
//     * @param specPath the resource path to a file with the merged Statix spec Stratego ATerm
//     * @param specName the name of the specification
//     * @param rootRuleName the name of the root rule
//     * @return the created test
//     */
//    protected DynamicTest completenessTest(String testPath, String expectedTermPath, String inputTermPath, String specPath, String specName, String csvPath, String rootRuleName) {
//        return DynamicTest.dynamicTest("complete file " + Paths.get(testPath).relativize(Paths.get(inputTermPath)) + " to " + Paths.get(testPath).relativize(Paths.get(expectedTermPath)) + " using spec " + Paths.get(specPath).getFileName() + "",
//            () -> {
//                StatixSpec spec = StatixSpec.fromClassLoaderResources(CompletenessTest.class, specPath);
//                IStrategoTerm expectedTerm = MoreTermUtils.fromClassLoaderResources(CompletenessTest.class, expectedTermPath);
//                IStrategoTerm inputTerm = MoreTermUtils.fromClassLoaderResources(CompletenessTest.class, inputTermPath);
//                doCompletenessTest(expectedTerm, inputTerm, spec, specName, rootRuleName, expectedTermPath, inputTermPath, csvPath);
//            });
//    }
//
//    /**
//     * Performs a completion test.
//     *
//     * @param expectedTerm the expected Stratego ATerm
//     * @param inputTerm the input Stratego ATerm
//     * @param spec the merged Statix spec Stratego ATerm
//     * @param specName the name of the specification
//     * @param rootRuleName the name of the root rule
//     */
//    private void doCompletenessTest(IStrategoTerm expectedTerm, IStrategoTerm inputTerm, StatixSpec spec, String specName, String rootRuleName, String expectedTermPath, String inputTermPath, String csvPath) throws InterruptedException, IOException {
//        ITermFactory termFactory = new TermFactory();
//        StrategoTerms strategoTerms = new StrategoTerms(termFactory);
//        ResourceKey resourceKey = new DefaultResourceKey("test", "ast");
//
//        IStrategoTerm annotatedExpectedTerm = StrategoTermIndices.index(expectedTerm, resourceKey.toString(), termFactory);
//        ITerm expectedStatixTerm = strategoTerms.fromStratego(annotatedExpectedTerm);
//
//        IStrategoTerm annotatedInputTerm = StrategoTermIndices.index(inputTerm, resourceKey.toString(), termFactory);
//        ITerm inputStatixTerm = strategoTerms.fromStratego(annotatedInputTerm);
//
//        doCompletenessTest(expectedStatixTerm, inputStatixTerm, spec, termFactory, resourceKey, specName, rootRuleName, expectedTermPath, csvPath);
//    }
//
//    /**
//     * Performs a completion test.
//     *
//     * @param expectedTerm the expected NaBL term
//     * @param inputTerm the input NaBL term
//     * @param spec the merged Statix spec
//     * @param termFactory the Stratego term factory
//     * @param resourceKey the resource key used to create term indices
//     * @param specName the name of the specification
//     * @param rootRuleName the name of the root rule
//     */
//    private void doCompletenessTest(ITerm expectedTerm, ITerm inputTerm, StatixSpec spec, ITermFactory termFactory, ResourceKey resourceKey, String specName, String rootRuleName, String testName, String csvPath) throws InterruptedException, IOException {
//        StatsGatherer stats = new StatsGatherer(csvPath);
//
//        StatixCodeCompleterBase completer = new StatixCodeCompleter(
//            spec.getSpec(),
//            new StrategoTerms(termFactory),
//            termFactory,
//            new TegoRuntimeImpl(loggerFactory),
//            loggerFactory,
//            null, // TODO
//            null, // TODO
//            null, // TODO
//            null, // TODO
//            null, // TODO
//            null // TODO
//        );
//        precomputeOrderIndependentRules(spec.getSpec());
//        ExecutorService executorService = Executors.newCachedThreadPool();
//
//        // Preparation
//        stats.startTest(testName);
//        PlaceholderVarMap placeholderVarMap = new PlaceholderVarMap(resourceKey.toString());
//        CompletionExpectation<? extends ITerm> completionExpectation = CompletionExpectation.fromTerm(inputTerm, expectedTerm, placeholderVarMap);
//
//        // Get the solver state of the program (whole project),
//        // which should have some remaining constraints on the placeholders.
//        SolverContext ctx = analyzer.createContext(eventHandler).withReporters(
//            t -> stats.reportSubTime(0, t),
//            t -> stats.reportSubTime(1, t),
//            t -> stats.reportSubTime(2, t),
//            t -> stats.reportSubTime(3, t));
//        stats.startInitialAnalysis();
//        SolverState startState = analyzer.createStartState(completionExpectation.getIncompleteAst(), specName, rootRuleName)
//            .withExistentials(placeholderVarMap.getVars())
//            .precomputeCriticalEdges(ctx.getSpec());
//        SolverState initialState = analyzer.analyze(ctx, startState);
//
//        // We track the current collection of errors.
//        final List<java.util.Map.Entry<IConstraint, IMessage>> currentErrors = initialState.getMessages().entrySet().stream().filter(kv -> kv.getValue().kind() == MessageKind.ERROR).collect(Collectors.toList());
//        if(!currentErrors.isEmpty()) {
//            //log.warn("input program validation failed.\n"+ initialState);
//            fail("Completion failed: input program validation failed.\n" + initialState);
//            return;
//        }
//
//        if(initialState.getConstraints().isEmpty()) {
//            fail("Completion failed: no constraints left, nothing to complete.\n" + initialState);
//            return;
//        }
//
//        final SolverContext newCtx = ctx.withAllowedErrors(currentErrors);
//
//        // We use a heuristic here.
//        final Predicate<ITerm> isInjPredicate = t -> t instanceof IApplTerm && ((IApplTerm)t).getArity() == 1 && ((IApplTerm)t).getOp().contains("2");
//
//        completionExpectation = completionExpectation.withState(initialState);
//
//        // Perform a left-to-right depth-first search of completions:
//        // - For each incomplete variable, we perform completion.
//        // - If a variable is a declaration's name or literal, we try to insert it.
//        // - If any of the variables result in one candidate, this candidate is applied.
//        // - If any of the variables result in more than one candidate, these are put in a stack and tried if completion fails.
//        // - If none of the variables result in at least one candidates,
//        //     then completion fails.
//
//        // Stack of expectations we are trying
//        final ArrayDeque<CompletionExpectation<? extends ITerm>> expectations = new ArrayDeque<>();
//        expectations.push(completionExpectation);
//
//        final ArrayList<FailTestResult> fails = new ArrayList<>();
//
//        while(!expectations.isEmpty()) {
//            CompletionExpectation<? extends ITerm> expectation = expectations.pop();
//            log.warn("Trying branch, starting from: " + expectation);
//
//            final FailTestResult result = completeUntilDone(expectation, testName, newCtx, isInjPredicate, completer, executorService, stats, expectations);
//            if (result == null) {
//                // Done!
//                break;
//            } else {
//                fails.add(result);
//                if (expectations.isEmpty()) {
//                    fail(() -> "FAILED!!\n  - " + fails.stream().map(it -> it.message).collect(Collectors.joining("\n  - ")));
//                }
//            }
//        }
//        log.info("Done completing!");
//
//        // Done! Success!
//        stats.endTest();
//    }
//
//    private @Nullable FailTestResult completeUntilDone(CompletionExpectation<? extends ITerm> expectation, String testName, SolverContext newCtx, Predicate<ITerm> isInjPredicate, TermCompleter completer, ExecutorService executorService, StatsGatherer stats, ArrayDeque<CompletionExpectation<? extends ITerm>> expectations) {
//        // List of failed variables
//        final Set<ITermVar> failedVars = new HashSet<>();
//        // List of delayed variables
//        final Set<ITermVar> delayedVars = new HashSet<>();
//        // Whether we did anything useful since the last time we tried all delays/failures
//        boolean madeProgress = false;
//        while(!expectation.isComplete()) {
//            cleanup();
//
//            // Pick the next variable that is not delayed or failed
//            ITermVar var = expectation.getVars().stream().filter(v -> !failedVars.contains(v) && !delayedVars.contains(v)).findFirst().orElse(null);
//            if(var != null) {
//                CompletionRunnable runnable = new CompletionRunnable(completer, expectation, var, stats, newCtx, isInjPredicate, testName);
//
//                Future<CompletionResult> future = executorService.submit(runnable);
//                try {
////                        CompletionResult result = future.get();
//                    CompletionResult result = future.get(15, TimeUnit.SECONDS);
//                    switch(result.state) {
//                        case Inserted:
//                            // Fallthrough:
//                        case Success:
//                            assert !result.getCompletionExpectations().isEmpty();
//                            madeProgress = true;
//                            // Push the second and subsequent candidates in reverse order on the stack
//                            for (int i = result.getCompletionExpectations().size() - 1; i > 0; i--) {
//                                CompletionExpectation<? extends ITerm> candidateExpectation = result.getCompletionExpectations().get(i);
//                                expectations.push(candidateExpectation);
//                            }
//                            // And continue with the first candidate
//                            expectation = result.getCompletionExpectations().get(0);
//                            break;
//                        case Skip:
//                            log.warn("Delayed {}", var);
//                            delayedVars.add(var);
//                            break;
//                        case Fail:
//                            log.warn("Failed {}", var);
//                            failedVars.add(var);
//                            break;
//                    }
//                } catch(TimeoutException ex) {
//                    return new FailTestResult("Timedout.");
//                } catch(InterruptedException ex) {
//                    return new FailTestResult("Interrupted.");
//                } catch(ExecutionException ex) {
//                    log.error("Error was thrown: " + ex.getMessage(), ex);
//                    return new FailTestResult("Error was thrown: " + ex.getMessage());
//                }
//            } else if(madeProgress && (!delayedVars.isEmpty() || !failedVars.isEmpty())) {
//                log.warn("All variables delayed or rejected, retrying since we made progress.");
//                // Try again on all completion variables
//                failedVars.clear();
//                delayedVars.clear();
//                madeProgress = false;
//                continue;
//            } else {
//                // No literals to insert
//                return new FailTestResult("All completions failed and could not insert any literals. The following are waiting: " + String.join(", ", failedVars.stream().map(Object::toString).collect(Collectors.toList())));
//            }
//        }
//        // Success!
//        return null;
//    }
//
//    private static class FailTestResult {
//        private final String message;
//
//        private FailTestResult(String message) {
//            this.message = message;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//    }
//
//    private void precomputeOrderIndependentRules(Spec spec) {
//        log.info("Precomputing...");
//        long start = System.nanoTime();
//        for(String ruleName : spec.rules().getRuleNames()) {
//            spec.rules().getOrderIndependentRules(ruleName);
//        }
//        log.info("Precomputed order independent rules in " + ((System.nanoTime() - start) / 1000000) + " ms");
//
//    }
//
//    private static void logCompletionStepResult(Level level, String message, String testName, ITermVar var, CompletionExpectation<?> expectation) {
//        log.log(level, "-------------- " + testName +" ----------------\n" +
//            endWithNewline(message) +
//            "Completion of var " + var + " in AST:\n  " + expectation.getIncompleteAst() +
//            "\nExpected:\n  " + expectation.getExpectations().get(var));
////            "\nState:\n  " + expectation.getState());
//    }
//
//    private static void logCompletionStepResultWithProposals(Level level, String message, String testName, ITermVar var, CompletionExpectation<?> expectation, List<CodeCompletionProposal> proposals) {
//        logCompletionStepResult(level, message + "\nProposals:\n  " + proposals.stream().map(p -> p.getTerm() + " <-  " + p.getNewState()).collect(Collectors.joining("\n  ")), testName, var, expectation);
//    }
//
//    private static void logCompletionStepResultWithCandidates(Level level, String message, String testName, ITermVar var, CompletionExpectation<?> expectation, List<CompletionExpectation<? extends ITerm>> candidates) {
//        logCompletionStepResult(level, message + "\nGot " + candidates.size() + " candidate" + (candidates.size() == 1 ? "" : "s") + ":\n  " + (DebugStrategy.debug ?
//            candidates.stream().map(c -> "- " + c.getState().project(var)).collect(Collectors.joining("\n  ")) + "\n  " +
//                candidates.stream().map(c -> "- " + c.getState().toString()).collect(Collectors.joining("\n  "))
//            : ""), testName, var, expectation);
//    }
//    // List<CodeCompletionProposal> proposals
//
//    private static String endWithNewline(String s) {
//        if (s == null || s.isEmpty() || s.trim().isEmpty()) return "";
//        if (s.endsWith("\n")) return s;
//        return s + "\n";
//    }
//
//    private static void cleanup() {
//        log.info("Cleaning...");
//        long cleanStart = System.nanoTime();
//        System.gc();
//        System.runFinalization();
//        System.gc();
//        log.info("Cleaned in " + ((System.nanoTime() - cleanStart) / 1000000) + " ms");
//        Runtime runtime = Runtime.getRuntime();
//        NumberFormat format = NumberFormat.getInstance();
//        long maxMemory = runtime.maxMemory();
//        long allocatedMemory = runtime.totalMemory();
//        long freeMemory = runtime.freeMemory();
//        log.info("Free memory: {} MB", freeMemory / (1024 * 1024));
//        log.info("Allocated memory: {} MB", allocatedMemory / (1024 * 1024));
//        log.info("Max memory: {} MB", maxMemory / (1024 * 1024));
//        log.info("Total free memory: {} MB", (freeMemory + (maxMemory - allocatedMemory)) / (1024 * 1024));
//    }
//
//    private static boolean isVarInDelays(Map.Immutable<IConstraint, Delay> delays, ITermVar var) {
//        return delays.values().stream().anyMatch(d -> d.vars().contains(var));
////        return delays.keySet().stream().anyMatch(c -> c.getVars().contains(var));
//    }
//
//    private static boolean isLiteral(ITerm term) {
//        // Is it a literal term, or an injection of a literal term?
//        return term instanceof IStringTerm
//            // TODO: Not use heuristics here.
//            || (term instanceof IApplTerm && ((IApplTerm)term).getOp().contains("-LEX2"));
//    }
//
//
//    private static class CompletionRunnable implements Callable<CompletionResult> {
//
//        private static final Logger log = loggerFactory.create(CompletionRunnable.class);
//        private final TermCompleter completer;
//        private final CompletionExpectation<? extends ITerm> completionExpectation;
//        private final ITermVar var;
//        private final StatsGatherer stats;
//        private final SolverContext newCtx;
//        private final Predicate<ITerm> isInjPredicate;
//        private final String testName;
//
//
//        private CompletionRunnable(
//            TermCompleter completer,
//            CompletionExpectation<? extends ITerm> completionExpectation,
//            ITermVar var,
//            StatsGatherer stats,
//            SolverContext newCtx,
//            Predicate<ITerm> isInjPredicate,
//            String testName
//        ) {
//            this.completer = completer;
//            this.completionExpectation = completionExpectation;
//            this.var = var;
//            this.stats = stats;
//            this.newCtx = newCtx;
//            this.isInjPredicate = isInjPredicate;
//            this.testName = testName;
//        }
//
//
//        @Override
//        public CompletionResult call() throws InterruptedException {
//            try {
//                stats.startRound();
//                final CompletionExpectation<? extends ITerm> newCompletionExpectation;
//                final SolverState state = Objects.requireNonNull(completionExpectation.getState());
//
//                log.info("====================== " + testName +" ================================\n" +
//                    "COMPLETING var " + var + " in AST:\n  " + completionExpectation.getIncompleteAst() + "\n" +
//                    "Expected:\n  " + completionExpectation.getExpectations().get(var));// + "\n" +
//                //"State:\n  " + state);
//
//                if (state.getConstraints().stream().filter(c -> c.getVars().contains(var))
//                    .anyMatch(CompletenessTest::isLiteralAstProperty) &&
//                    isLiteral(completionExpectation.getExpectations().get(var))) {
//                    // The variable is a literal that has an @decl or @lit annotation.
//                    log.info("Found declaration name or literal, inserting...");
//                    ITerm term = completionExpectation.getExpectations().get(var);
//
//                    // Add a constraint that inserts the literal, and perform inference
//                    CEqual ceq = new CEqual(var, term);
//                    SolverState newSolverState = completionExpectation.getState().updateConstraints(newCtx.getSpec(), Collections.singletonList(ceq), Collections.emptyList());
//                    List<SolverState> inferredSolverStates = InferStrategy.getInstance().eval(newCtx, newSolverState).toList().eval();
//                    if (inferredSolverStates.isEmpty()) {
//                        logCompletionStepResult(Level.Warn, "Inference failed when inserting literal. Could not insert: " + term + "\n",
//                            testName, var, completionExpectation);
//                        stats.endRound();
//                        return CompletionResult.fail();
//                    }
//                    SolverState inferredSolverState = inferredSolverStates.get(0);
//
//                    // Replace the term in the expectation
//                    @Nullable CompletionExpectation<? extends ITerm> candidate = completionExpectation.tryReplace(var, new CodeCompletionProposal(inferredSolverState, term));
//                    if(candidate == null) {
//                        logCompletionStepResult(Level.Warn, "Expected a declaration name or literal. Could not insert: " + term + "\n",
//                            testName, var, completionExpectation);
//                        stats.endRound();
//                        return CompletionResult.fail();
//                    }
//                    stats.insertedLiteral();
//                    newCompletionExpectation = candidate;
//                    logCompletionStepResult(Level.Info, "Inserted 1 declaration name or literal: " + term + "\n " + candidate.getIncompleteAst(),
//                        testName, var, completionExpectation);
//                    stats.endRound();
//                    return CompletionResult.inserted(newCompletionExpectation);
//                }
//
//                if(isVarInDelays(state.getDelays(), var)) {
//                    // We skip variables in delays, let's see where we get until we loop forever.
//                    stats.skipRound();
//                    log.info("All delayed. Skipped.");
//                    return CompletionResult.skip();
//                }
//
//                List<CodeCompletionProposal> proposals = completer.complete(newCtx, isInjPredicate, state, var);
//                // For each proposal, find the candidates that fit
//                final CompletionExpectation<? extends ITerm> currentCompletionExpectation = completionExpectation;
//
//                List<CompletionExpectation<? extends ITerm>> candidates = proposals.stream()
//                    .<CompletionExpectation<? extends ITerm>>map(p -> currentCompletionExpectation.tryReplace(var, p))
//                    .filter(Objects::nonNull)
//                    .collect(groupingBy(it -> it.getVars().size()))
//                    .entrySet()
//                    .stream()
//                    .min(Comparator.comparingInt(it -> it.getKey()))
//                    .map(it -> it.getValue())
//                    .orElse(Collections.emptyList());
//                if(candidates.size() == 1) {
//                    // Only one candidate, let's apply it
//                    logCompletionStepResultWithCandidates(Level.Info, "Only one candidate.", testName, var, currentCompletionExpectation, candidates);
//                    stats.endRound();
//                    return CompletionResult.of(candidates.get(0));
//                } else if(candidates.size() > 1) {
//                    // Multiple candidates, let's use the one with the least number of open variables
//                    // and otherwise the first one (could also use the biggest one instead)
//                    candidates.sort(Comparator.comparingInt(o -> o.getVars().size()));
//                    logCompletionStepResultWithCandidates(Level.Info, "Multiple candidates, picked the first for now...", testName, var, currentCompletionExpectation, candidates);
//                    stats.endRound();
//                    return CompletionResult.ofAll(candidates);
//                } else {
//                    // No candidates, completion algorithm is not complete
//                    logCompletionStepResultWithProposals(Level.Warn, "Got NO candidates.", testName, var, currentCompletionExpectation, proposals);
//                    stats.endRound();
//                    return CompletionResult.fail();
//                }
//            } catch(Throwable ex) {
//                log.error("Uncaught exception: " + ex.getMessage(), ex);
//                throw ex;
//            }
//        }
//    }
//
//    private static boolean isLiteralAstProperty(IConstraint c) {
//        if (!(c instanceof CAstProperty)) return false;
//        CAstProperty astProperty = (CAstProperty)c;
//        ITerm propertyTerm = astProperty.property();
//        if (!(propertyTerm instanceof IApplTerm)) return false;
//        IApplTerm propertyAppl = (IApplTerm)propertyTerm;
//        if (!propertyAppl.getOp().equals("Prop") || propertyAppl.getArity() != 1) return false;
//        ITerm propertyArg = propertyAppl.getArgs().get(0);
//        if (!(propertyArg instanceof IStringTerm)) return false;
//        String propertyName = ((IStringTerm)propertyArg).getValue();
//        // Declarations are marked `@name.decl := name`.
//        // Literals (int, string) are marked `@name.lit := name`.
//        return propertyName.equals("decl") || propertyName.equals("lit");
//    }
//
//    private static int countVars(ITerm t) {
//        return t.getVars().size();
//    }
//
//    private enum CompletionState {
//        Success,
//        Fail,
//        Skip,
//        Inserted,       // When a literal has been inserted
//    }
//
//    private static class CompletionResult {
//        private final CompletionState state;
//        private final List<CompletionExpectation<? extends ITerm>> completionExpectations;
//
//        public CompletionResult(CompletionState state, List<CompletionExpectation<? extends ITerm>> completionExpectations) {
//            this.state = state;
//            this.completionExpectations = completionExpectations;
//        }
//
//        public List<CompletionExpectation<? extends ITerm>> getCompletionExpectations() {
//            return completionExpectations;
//        }
//
//        public CompletionState getState() {
//            return state;
//        }
//
//        public static CompletionResult fail() { return new CompletionResult(CompletionState.Fail, Collections.emptyList()); }
//        public static CompletionResult skip() { return new CompletionResult(CompletionState.Skip, Collections.emptyList()); }
//        public static CompletionResult inserted(CompletionExpectation<? extends ITerm> completionExpectation) { return new CompletionResult(CompletionState.Inserted, Collections.singletonList(completionExpectation)); }
//        public static CompletionResult of(CompletionExpectation<? extends ITerm> completionExpectation) { return new CompletionResult(CompletionState.Success, Collections.singletonList(completionExpectation)); }
//        public static CompletionResult ofAll(Collection<CompletionExpectation<? extends ITerm>> completionExpectations) { return new CompletionResult(CompletionState.Success, new ArrayList<>(completionExpectations)); }
//
//    }
//
//}
