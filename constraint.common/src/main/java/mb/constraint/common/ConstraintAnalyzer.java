package mb.constraint.common;

import com.google.common.collect.ImmutableList;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ConstraintAnalyzer {
    private final Logger logger;

    private final HashMap<ResourceKey, Result> resultCache = new HashMap<>();


    @Inject public ConstraintAnalyzer(Logger logger) {
        this.logger = logger;
    }


    public static class SingleFileResult {
        public final @Nullable IStrategoTerm ast;
        public final @Nullable IStrategoTerm analysis;
        public final Messages messages;
    }

    public SingleFileResult analyze(ResourceKey resource, StrategoRuntime strategoRuntime) {

    }

    public static class Result {
        public final @Nullable IStrategoTerm ast;
        public final @Nullable IStrategoTerm analysis;
    }

    public static class MultiFileResult {
        public final HashMap<ResourceKey, Result> results;
        public final Messages messages;
    }

    public MultiFileResult analyze(HashSet<ResourceKey> resources, StrategoRuntime strategoRuntime) {

    }


    public void doAnalyze(
        ResourceKey root,
        ArrayList<ResourceKey> resources,
        StrategoRuntime strategoRuntime,
        boolean multifile,
        String strategy
    ) throws ConstraintAnalyzerException {
        // TODO: calculate which resources were removed w.r.t. last invocation.
        final HashSet<ResourceKey> removed = new HashSet<>();

        // TODO: request the AST for other resources, and put them in addedOrChanged.
        final HashMap<ResourceKey, IStrategoTerm> addedOrChanged = new HashMap<>();

        final ITermFactory termFactory = strategoRuntime.getTermFactory();

        /*******************************************************************
         * 1. Compute changeset, and remove invalidated units from context *
         *******************************************************************/

        final ArrayList<IStrategoTerm> changes = new ArrayList<>();
        final HashMap<ResourceKey, Expect> expects = new HashMap<>();

        // Root analysis.
        final @Nullable IStrategoTerm rootChange;
        if(multifile) {
            final IStrategoTerm ast = termFactory.makeTuple();
            final IStrategoTerm change;
            final Expect expect;
            if(resultCache.containsKey(root)) {
                final Result cachedResult = resultCache.get(root);
                change = build("Cached", cachedResult.analysis);
                expect = new Update(root);
                resultCache.remove(root);
            } else {
                change = build("Added", ast);
                expect = new Project(root);
            }
            expects.put(root, expect);
            rootChange = termFactory.makeTuple(termFactory.makeString(root), change);
        } else {
            rootChange = null;
        }

        // Removed resources.
        for(ResourceKey resourceKey : removed) {
            if(resultCache.containsKey(resourceKey)) {
                final Cache cache = resultCache.get(resourceKey);
                changes.add(termFactory.makeTuple(termFactory.makeString(resourceKey.toString()),
                    build("Removed", cache.analysis)));
                resultCache.remove(resourceKey);
            }
        }

        // Added and changed resources.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChanged.entrySet()) {
            final ResourceKey resourceKey = entry.getKey();
            final IStrategoTerm ast = entry.getValue();
            final IStrategoTerm change;
            if(resultCache.containsKey(resourceKey)) {
                final IStrategoTerm analysisTerm = resultCache.get(resourceKey);
                change = build("Changed", ast, analysisTerm);
                resultCache.remove(resourceKey);
            } else {
                change = build("Added", ast);
            }
            expects.put(resourceKey, new Full(resourceKey, ast));
            changes.add(termFactory.makeTuple(termFactory.makeString(resourceKey.toString()), change));
        }

        // Cached files.
        if(multifile) {
            for(Map.Entry<ResourceKey, IStrategoTerm> entry : resultCache.entrySet()) {
                final ResourceKey resourceKey = entry.getKey();
                final IStrategoTerm analysisTerm = entry.getValue();
                if(!addedOrChanged.containsKey(resourceKey)) {
                    final IStrategoTerm change = build("Cached", analysisTerm);
                    expects.put(resourceKey, new Update(resourceKey));
                    changes.add(termFactory.makeTuple(termFactory.makeString(resourceKey.toString()), change));
                }
            }
        }

        /***************************************
         * 2. Call analysis, and parse results *
         ***************************************/

        final Map<String, IStrategoTerm> results = new HashMap<>();
        final IStrategoTerm action;
        if(multifile) {
            action = build("AnalyzeMulti", rootChange, termFactory.makeList(changes));
        } else {
            action = build("AnalyzeSingle", termFactory.makeList(changes));
        }

        final @Nullable IStrategoTerm allResultsTerm;
        try {
            allResultsTerm = strategoRuntime.invoke(strategy, action, new IOAgent());
        } catch(StrategoException e) {
            throw new ConstraintAnalyzerException(e);
        }
        if(allResultsTerm == null) {
            throw new ConstraintAnalyzerException("Constraint analysis strategy '" + strategy + "' failed");
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
            if(!Tools.isTermString(resourceTerm)) {
                throw new ConstraintAnalyzerException(
                    "Expected resource string as first component, got " + resourceTerm);
            }
            final String resource = Tools.asJavaString(resourceTerm);
            results.put(resource, resultTerm);
        }

        /*******************************
         * 3. Process analysis results *
         *******************************/

        // call expects with result
        for(Map.Entry<String, IStrategoTerm> entry : results.entrySet()) {
            final String resource = entry.getKey();
            final IStrategoTerm result = entry.getValue();
            if(expects.containsKey(resource)) {
                expects.get(resource).processResultTerm(result);
            } else {
                logger.warn("Got result for invalid file.");
            }
        }

        // check coverage
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChanged.entrySet()) {
            final ResourceKey resource = entry.getKey();
            if(!results.containsKey(resource)) {
                expects.get(resource).addFailMessage("Missing analysis result");
            }
        }

        /**************************************
         * 4. Globally collect error messages *
         **************************************/

        final MessagesBuilder messagesBuilder = new MessagesBuilder();
        for(Map.Entry<String, Expect> entry : expects.entrySet()) {
            final Expect expect = entry.getValue();
            messagesBuilder.addMessages(expect.messagesBuilder);
            messages.putAll(expect.messages);
        }
    }


    static class Cache {
        final IStrategoTerm ast;
        final IStrategoTerm analysis;

        Cache(IStrategoTerm ast, IStrategoTerm analysis) {
            this.ast = ast;
            this.analysis = analysis;
        }

        Cache(Cache cache, IStrategoTerm analysis) {
            this.ast = cache.ast;
            this.analysis = analysis;
        }
    }


    abstract class Expect {
        final ResourceKey resourceKey;

        Expect(ResourceKey resourceKey) {
            this.resourceKey = resourceKey;
        }

        void addResultMessages(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, MessagesBuilder messagesBuilder) {
            MessageUtil.addMessagesFromTerm(messagesBuilder, errors, Severity.Error);
            MessageUtil.addMessagesFromTerm(messagesBuilder, warnings, Severity.Warning);
            MessageUtil.addMessagesFromTerm(messagesBuilder, notes, Severity.Info);
        }

        void addFailMessage(String text, MessagesBuilder messagesBuilder) {
            messagesBuilder.addMessage(text, Severity.Error, resourceKey);
        }

        /**
         * Process given {@code resultTerm}, adding messages to given {@code messagesBuilder}, returning the analyzed
         * AST, or null if none.
         */
        abstract @Nullable IStrategoTerm processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder);
    }

    class Full extends Expect {
        Full(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public @Nullable IStrategoTerm processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm ast = results.get(0);
                final IStrategoTerm analysisTerm = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                resultCache.put(resourceKey, analysisTerm);
                return ast;
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                resultCache.remove(resourceKey);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
            return null;
        }
    }

    class Update extends Expect {
        Update(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public @Nullable IStrategoTerm processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Update", 4)) != null) {
                final IStrategoTerm analysisTerm = results.get(0);
                addResultMessages(results.get(1), results.get(2), results.get(3), messagesBuilder);
                resultCache.put(resourceKey, analysisTerm);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                resultCache.remove(resourceKey);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
            return null; // TODO: always returns null?
        }
    }

    class Project extends Expect {
        Project(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override public IStrategoTerm processResultTerm(IStrategoTerm resultTerm, MessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm analysisTerm = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                resultCache.put(resourceKey, analysisTerm);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                resultCache.remove(resourceKey);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
            return null; // TODO: always returns null?
        }
    }


    protected IStrategoTerm build(String op, IStrategoTerm... subterms) {
        return termFactory.makeAppl(termFactory.makeConstructor(op, subterms.length), subterms);
    }

    protected @Nullable List<IStrategoTerm> match(@Nullable IStrategoTerm term, String op, int n) {
        if(term == null || !Tools.isTermAppl(term) || !Tools.hasConstructor((IStrategoAppl) term, op, n)) {
            return null;
        }
        return ImmutableList.copyOf(term.getAllSubterms());
    }
}