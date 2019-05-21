package mb.constraint.common;

import mb.common.message.Messages;
import mb.common.message.MessagesBuilder;
import mb.common.message.Severity;
import mb.log.api.Logger;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConstraintAnalyzer {
    public static class Result {
        public final @Nullable IStrategoTerm ast;
        public final @Nullable IStrategoTerm analysis;

        public Result(@Nullable IStrategoTerm ast, @Nullable IStrategoTerm analysis) {
            this.ast = ast;
            this.analysis = analysis;
        }

        public Result() {
            this.ast = null;
            this.analysis = null;
        }
    }

    public static class SingleFileResult {
        public final @Nullable IStrategoTerm ast;
        public final @Nullable IStrategoTerm analysis;
        public final Messages messages;

        public SingleFileResult(@Nullable IStrategoTerm ast, @Nullable IStrategoTerm analysis, Messages messages) {
            this.ast = ast;
            this.analysis = analysis;
            this.messages = messages;
        }
    }

    public static class MultiFileResult {
        public final HashMap<ResourceKey, Result> results;
        public final Messages messages;

        public MultiFileResult(HashMap<ResourceKey, Result> results, Messages messages) {
            this.results = results;
            this.messages = messages;
        }
    }


    private final Logger logger;
    private final ITermFactory termFactory;

    private final ResultCache resultCache = new ResultCache();


    @Inject public ConstraintAnalyzer(Logger logger, ITermFactory termFactory) {
        this.logger = logger;
        this.termFactory = termFactory;
    }


    public SingleFileResult analyze(ResourceKey resource, IStrategoTerm ast, StrategoRuntime strategoRuntime, String strategyId)
        throws ConstraintAnalyzerException {
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>(1);
        asts.put(resource, ast);
        final MultiFileResult multiFileResult = doAnalyze(null, asts, strategoRuntime, false, strategyId);
        final @Nullable Result result = multiFileResult.results.get(resource);
        if(result == null) {
            throw new ConstraintAnalyzerException("No analysis result found");
        }
        return new SingleFileResult(result.ast, result.analysis, multiFileResult.messages);
    }

    public MultiFileResult analyze(ResourceKey root, HashMap<ResourceKey, IStrategoTerm> asts, StrategoRuntime strategoRuntime, String strategyId)
        throws ConstraintAnalyzerException {
        return doAnalyze(root, asts, strategoRuntime, true, strategyId);
    }

    private MultiFileResult doAnalyze(
        @Nullable ResourceKey root,
        HashMap<ResourceKey, IStrategoTerm> asts,
        StrategoRuntime strategoRuntime,
        boolean multifile,
        String strategyId
    ) throws ConstraintAnalyzerException {
        final HashSet<ResourceKey> removed = new HashSet<>();
        final HashMap<ResourceKey, IStrategoTerm> addedOrChangedAsts = new HashMap<>();

        final ITermFactory termFactory = strategoRuntime.getTermFactory();

        /// 1. Compute changeset, and remove invalidated units from context.

        final ArrayList<IStrategoTerm> changes = new ArrayList<>();
        final HashMap<ResourceKey, Expect> expects = new HashMap<>();

        // Root analysis.
        final @Nullable IStrategoTerm rootChange;
        if(multifile && root != null) {
            final IStrategoTerm ast = tuple();
            final IStrategoTerm change;
            final Expect expect;
            if(resultCache.containsKey(root)) {
                final Result cachedResult = resultCache.get(root);
                change = appl("Cached", cachedResult.analysis);
                expect = new Update(root);
                resultCache.remove(root);
            } else {
                change = appl("Added", ast);
                expect = new Project(root);
            }
            expects.put(root, expect);
            rootChange = tuple(blob(root), change);
        } else {
            rootChange = null;
        }

        // Removed resources.
        for(ResourceKey resource : removed) {
            if(resultCache.containsKey(resource)) {
                final Result cachedResult = resultCache.get(resource);
                changes.add(tuple(blob(resource), appl("Removed", cachedResult.analysis)));
                resultCache.remove(resource);
            }
        }

        // Added and changed resources.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChangedAsts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final IStrategoTerm ast = entry.getValue();
            final IStrategoTerm change;
            if(resultCache.containsKey(resource)) {
                final Result cachedResult = resultCache.get(resource);
                change = appl("Changed", ast, cachedResult.analysis);
                resultCache.remove(resource);
            } else {
                change = appl("Added", ast);
            }
            expects.put(resource, new Full(resource));
            changes.add(tuple(blob(resource), change));
        }

        // Cached files.
        if(multifile) {
            for(Map.Entry<ResourceKey, Result> entry : resultCache.entrySet()) {
                final ResourceKey resource = entry.getKey();
                final Result cachedResult = entry.getValue();
                if(!addedOrChangedAsts.containsKey(resource)) {
                    final IStrategoTerm change = appl("Cached", cachedResult.analysis);
                    expects.put(resource, new Update(resource));
                    changes.add(tuple(blob(resource), change));
                }
            }
        }

        /// 2. Call analysis, and list results.

        final Map<ResourceKey, IStrategoTerm> resultTerms = new HashMap<>();
        final IStrategoTerm action;
        if(multifile) {
            action = appl("AnalyzeMulti", rootChange, termFactory.makeList(changes));
        } else {
            action = appl("AnalyzeSingle", termFactory.makeList(changes));
        }

        final @Nullable IStrategoTerm allResultsTerm;
        try {
            allResultsTerm = strategoRuntime.invoke(strategyId, action, new IOAgent());
        } catch(StrategoException e) {
            throw new ConstraintAnalyzerException(e);
        }
        if(allResultsTerm == null) {
            throw new ConstraintAnalyzerException("Constraint analysis strategy '" + strategyId + "' failed");
        }

        final @Nullable List<IStrategoTerm> allResultTerms = match(allResultsTerm, "AnalysisResult", 1);
        if(allResultTerms == null || allResultTerms.isEmpty()) {
            throw new ConstraintAnalyzerException("Invalid constraint analysis result, got: " + allResultsTerm);
        }

        final IStrategoTerm resultsTerm = allResultTerms.get(0);
        if(!Tools.isTermList(resultsTerm)) {
            throw new ConstraintAnalyzerException("Expected list of results, got: " + resultsTerm);
        }
        for(IStrategoTerm entry : resultsTerm) {
            if(!Tools.isTermTuple(entry) || entry.getSubtermCount() != 2) {
                throw new ConstraintAnalyzerException("Expected tuple result, got " + entry);
            }
            final IStrategoTerm resourceTerm = entry.getSubterm(0);
            final IStrategoTerm resultTerm = entry.getSubterm(1);
            if(!(resourceTerm instanceof StrategoResourceKey)) {
                throw new ConstraintAnalyzerException(
                    "Expected StrategoResourceKey as first component, got " + resourceTerm);
            }
            final ResourceKey resource = ((StrategoResourceKey) resourceTerm).value;
            resultTerms.put(resource, resultTerm);
        }

        /// 3. Process analysis results and collect messages.

        final MessagesBuilder messagesBuilder = new MessagesBuilder();

        // Process result terms, updating the result cache.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : resultTerms.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final IStrategoTerm resultTerm = entry.getValue();
            if(expects.containsKey(resource)) {
                expects.get(resource).processResultTerm(resultTerm, messagesBuilder);
            } else {
                logger.warn("Got result for an invalid resource");
            }
        }

        // Check if all input resources have been covered.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChangedAsts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            if(!resultTerms.containsKey(resource)) {
                expects.get(resource).addFailMessage("Missing analysis result", messagesBuilder);
            }
        }

        /// 4. Build and return result object.

        final HashMap<ResourceKey, Result> results = new HashMap<>(asts.size());
        for(ResourceKey resource : addedOrChangedAsts.keySet()) {
            final @Nullable Result result = resultCache.get(resource);
            if(result != null) {
                results.put(resource, result);
            } else {
                results.put(resource, new Result());
            }
        }
        return new MultiFileResult(results, messagesBuilder.build());
    }


    static class ResultCache {
        final HashMap<ResourceKey, Result> map = new HashMap<>();

        boolean containsKey(ResourceKey resource) {
            return map.containsKey(resource);
        }

        @Nullable Result get(ResourceKey resource) {
            return map.get(resource);
        }

        Set<Entry<ResourceKey, Result>> entrySet() {
            return map.entrySet();
        }

        void update(ResourceKey resource, IStrategoTerm ast, IStrategoTerm analysis) {
            map.put(resource, new Result(ast, analysis));
        }

        void updateAst(ResourceKey resource, IStrategoTerm ast) {
            final @Nullable Result result = map.get(resource);
            if(result == null) {
                map.put(resource, new Result(ast, null));
            } else {
                map.put(resource, new Result(ast, result.analysis));
            }
        }

        void updateAnalysis(ResourceKey resource, IStrategoTerm analysis) {
            final @Nullable Result result = map.get(resource);
            if(result == null) {
                map.put(resource, new Result(null, analysis));
            } else {
                map.put(resource, new Result(result.ast, analysis));
            }
        }

        void remove(ResourceKey resource) {
            map.remove(resource);
        }
    }


    abstract class Expect {
        final ResourceKey resource;

        Expect(ResourceKey resource) {
            this.resource = resource;
        }

        void addResultMessages(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, MessagesBuilder messagesBuilder) {
            MessageUtil.addMessagesFromTerm(messagesBuilder, errors, Severity.Error);
            MessageUtil.addMessagesFromTerm(messagesBuilder, warnings, Severity.Warning);
            MessageUtil.addMessagesFromTerm(messagesBuilder, notes, Severity.Info);
        }

        void addFailMessage(String text, MessagesBuilder messagesBuilder) {
            messagesBuilder.addMessage(text, Severity.Error, resource);
        }

        abstract void processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder);
    }

    class Full extends Expect {
        Full(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override public void processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm ast = results.get(0);
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                resultCache.update(resource, ast, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                resultCache.remove(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }

    class Update extends Expect {
        Update(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override public void processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Update", 4)) != null) {
                final IStrategoTerm analysis = results.get(0);
                addResultMessages(results.get(1), results.get(2), results.get(3), messagesBuilder);
                resultCache.updateAnalysis(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                resultCache.remove(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }

    class Project extends Expect {
        Project(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override public void processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                resultCache.updateAnalysis(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                resultCache.remove(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }


    private IStrategoTerm appl(String op, IStrategoTerm... subterms) {
        return termFactory.makeAppl(termFactory.makeConstructor(op, subterms.length), subterms);
    }

    private IStrategoTerm tuple(IStrategoTerm... subterms) {
        return termFactory.makeTuple(subterms);
    }

    private StrategoResourceKey blob(ResourceKey resource) {
        return new StrategoResourceKey(resource);
    }

    private @Nullable List<IStrategoTerm> match(@Nullable IStrategoTerm term, String op, int n) {
        if(term == null || !Tools.isTermAppl(term) || !Tools.hasConstructor((IStrategoAppl) term, op, n)) {
            return null;
        }
        return Arrays.asList(term.getAllSubterms());
    }
}