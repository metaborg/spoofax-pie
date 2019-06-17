package mb.constraint.common;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.TermIndex;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.resource.ResourceKey;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConstraintAnalyzer {
    public static class Result implements Serializable {
        public final ResourceKey resource;
        public final @Nullable IStrategoTerm ast;
        public final @Nullable IStrategoTerm analysis;

        public Result(ResourceKey resource, @Nullable IStrategoTerm ast, @Nullable IStrategoTerm analysis) {
            this.resource = resource;
            this.ast = ast;
            this.analysis = analysis;
        }

        public Result(ResourceKey resource) {
            this.resource = resource;
            this.ast = null;
            this.analysis = null;
        }
    }

    public static class SingleFileResult implements Serializable {
        public final @Nullable IStrategoTerm ast;
        public final @Nullable IStrategoTerm analysis;
        public final Messages messages;

        public SingleFileResult(@Nullable IStrategoTerm ast, @Nullable IStrategoTerm analysis, Messages messages) {
            this.ast = ast;
            this.analysis = analysis;
            this.messages = messages;
        }
    }

    public static class MultiFileResult implements Serializable {
        public final ArrayList<Result> results;
        public final KeyedMessages keyedMessages;

        public MultiFileResult(ArrayList<Result> results, KeyedMessages keyedMessages) {
            this.results = results;
            this.keyedMessages = keyedMessages;
        }

        public @Nullable Result getResult(ResourceKey resource) {
            // OPTO: linear search to lookup. Cannot use HashMap because it does not serialize properly.
            return results.stream().filter((r) -> r.resource.equals(resource)).findFirst().orElse(null);
        }
    }


    private final StrategoRuntime strategoRuntime;
    private final ITermFactory termFactory;
    private final String strategyId;
    private final boolean multifile;


    @Inject public ConstraintAnalyzer(StrategoRuntime strategoRuntime, String strategyId, boolean multifile) {
        this.strategoRuntime = strategoRuntime;
        this.termFactory = strategoRuntime.getTermFactory();
        this.strategyId = strategyId;
        this.multifile = multifile;
    }


    public SingleFileResult analyze(ResourceKey resource, IStrategoTerm ast, ConstraintAnalyzerContext context, IOAgent strategoIOAgent)
        throws ConstraintAnalyzerException {
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>(1);
        asts.put(resource, ast);
        final MultiFileResult multiFileResult = doAnalyze(null, asts, context, strategoIOAgent);
        final @Nullable Result result;
        try {
            result = multiFileResult.results.get(0);
        } catch(IndexOutOfBoundsException e) {
            throw new RuntimeException("BUG: no analysis result was found for resource '" + resource + "'", e);
        }
        return new SingleFileResult(result.ast, result.analysis, multiFileResult.keyedMessages.asMessages());
    }

    public MultiFileResult analyze(@Nullable ResourceKey root, HashMap<ResourceKey, IStrategoTerm> asts, ConstraintAnalyzerContext context, IOAgent strategoIOAgent)
        throws ConstraintAnalyzerException {
        return doAnalyze(root, asts, context, strategoIOAgent);
    }

    private MultiFileResult doAnalyze(
        @Nullable ResourceKey root,
        HashMap<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext context,
        IOAgent strategoIOAgent
    ) throws ConstraintAnalyzerException {
        /// 1. Compute changeset from given asts and cache.

        final HashMap<ResourceKey, IStrategoTerm> addedOrChangedAsts = new HashMap<>();
        for(Entry<ResourceKey, IStrategoTerm> entry : asts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final IStrategoTerm ast = entry.getValue();
            addedOrChangedAsts.put(resource, ast);
        }
        final HashSet<ResourceKey> removed = new HashSet<>(context.getResultResources());
        removed.removeAll(addedOrChangedAsts.keySet());

        /// 2. Transform changeset into list of changed terms and expect objects, and remove invalidated units from the context.

        final ArrayList<IStrategoTerm> changeTerms = new ArrayList<>();
        final HashMap<ResourceKey, Expect> expects = new HashMap<>();

        // Root analysis.
        final @Nullable IStrategoTerm rootChange;
        if(multifile && root != null) {
            context.registerResource(root);
            final IStrategoTerm ast = mkProjectTerm(root);
            final IStrategoTerm change;
            final Expect expect;
            final @Nullable Result cachedResult = context.getResult(root);
            if(cachedResult != null) {
                change = mkAppl("Cached", cachedResult.analysis);
                expect = new Update(root);
                context.removeResult(root);
            } else {
                change = mkAppl("Added", ast);
                expect = new Project(root);
            }
            expects.put(root, expect);
            rootChange = mkTuple(mkString(root), change);
        } else {
            rootChange = null;
        }

        // Removed resources.
        for(ResourceKey resource : removed) {
            final @Nullable Result cachedResult = context.getResult(resource);
            if(cachedResult != null) {
                changeTerms.add(mkTuple(mkString(resource), mkAppl("Removed", cachedResult.analysis)));
                context.removeResult(resource);
                context.removeResource(resource);
            }
        }

        // Added and changed resources.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChangedAsts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            context.registerResource(resource);
            final IStrategoTerm ast = entry.getValue();
            final IStrategoTerm change;
            final @Nullable Result cachedResult = context.getResult(resource);
            if(cachedResult != null) {
                change = mkAppl("Changed", ast, cachedResult.analysis);
                context.removeResult(resource);
            } else {
                change = mkAppl("Added", ast);
            }
            expects.put(resource, new Full(resource));
            changeTerms.add(mkTuple(mkString(resource), change));
        }

        // Cached resources.
        if(multifile) {
            for(Map.Entry<ResourceKey, Result> entry : context.getResultEntries()) {
                final ResourceKey resource = entry.getKey();
                context.registerResource(resource);
                final Result cachedResult = entry.getValue();
                if(!addedOrChangedAsts.containsKey(resource)) {
                    final IStrategoTerm change = mkAppl("Cached", cachedResult.analysis);
                    expects.put(resource, new Update(resource));
                    changeTerms.add(mkTuple(mkString(resource), change));
                }
            }
        }

        /// 3. Call analysis, and list results.

        final Map<ResourceKey, IStrategoTerm> resultTerms = new HashMap<>();
        final IStrategoTerm action;
        if(multifile && root != null) {
            action = mkAppl("AnalyzeMulti", rootChange, termFactory.makeList(changeTerms));
        } else {
            action = mkAppl("AnalyzeSingle", termFactory.makeList(changeTerms));
        }

        final @Nullable IStrategoTerm allResultsTerm;
        try {
            allResultsTerm = strategoRuntime.invoke(strategyId, action, strategoIOAgent);
        } catch(StrategoException e) {
            throw new ConstraintAnalyzerException(e);
        }
        if(allResultsTerm == null) {
            throw new ConstraintAnalyzerException("Constraint analysis strategy '" + strategyId + "' failed");
        }

        final @Nullable List<IStrategoTerm> allResultTerms = match(allResultsTerm, "AnalysisResult", 1);
        if(allResultTerms == null || allResultTerms.isEmpty()) {
            throw new RuntimeException("BUG: invalid constraint analysis result, got " + allResultsTerm);
        }

        final IStrategoTerm resultsTerm = allResultTerms.get(0);
        if(!Tools.isTermList(resultsTerm)) {
            throw new RuntimeException("BUG: expected list of results, got: " + resultsTerm);
        }
        for(IStrategoTerm entry : resultsTerm) {
            if(!Tools.isTermTuple(entry) || entry.getSubtermCount() != 2) {
                throw new RuntimeException("BUG: expected tuple result, got " + entry);
            }
            final IStrategoTerm resourceTerm = entry.getSubterm(0);
            if(!Tools.isTermString(resourceTerm)) {
                throw new RuntimeException("BUG: expected resource string as first component, got " + resourceTerm);
            }
            final String resourceString = Tools.asJavaString(resourceTerm);
            final @Nullable ResourceKey resource = context.getResource(resourceString);
            if(resource == null) {
                throw new RuntimeException(
                    "BUG: could not get resource for resource string '" + resourceString + "' in result term " + entry);
            }
            final IStrategoTerm resultTerm = entry.getSubterm(1);
            resultTerms.put(resource, resultTerm);
        }

        /// 4. Process analysis results and collect messages.

        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();

        // Process result terms, updating the result cache.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : resultTerms.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final IStrategoTerm resultTerm = entry.getValue();
            if(expects.containsKey(resource)) {
                expects.get(resource).processResultTerm(resultTerm, context, messagesBuilder);
            } else {
                throw new RuntimeException(
                    "BUG: got result '" + resultTerm + "' for resource '" + resource + "' that was not part of the input");
            }
        }

        // Check if all input resources have been covered.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChangedAsts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            if(!resultTerms.containsKey(resource)) {
                expects.get(resource).addFailMessage("Missing analysis result", messagesBuilder);
            }
        }

        /// 5. Build and return result object.

        final ArrayList<Result> results = new ArrayList<>(asts.size());
        for(ResourceKey resource : addedOrChangedAsts.keySet()) {
            final @Nullable Result result = context.getResult(resource);
            if(result != null) {
                results.add(result);
            } else {
                results.add(new Result(resource));
            }
        }
        return new MultiFileResult(results, messagesBuilder.build());
    }


    abstract class Expect {
        final ResourceKey resource;

        Expect(ResourceKey resource) {
            this.resource = resource;
        }

        void addResultMessages(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable ResourceKey resourceOverride = multifile ? null : resource;
            MessageUtil.addMessagesFromTerm(messagesBuilder, errors, Severity.Error, resourceOverride);
            MessageUtil.addMessagesFromTerm(messagesBuilder, warnings, Severity.Warning, resourceOverride);
            MessageUtil.addMessagesFromTerm(messagesBuilder, notes, Severity.Info, resourceOverride);
        }

        void addFailMessage(String text, KeyedMessagesBuilder messagesBuilder) {
            messagesBuilder.addMessage(text, Severity.Error, resource);
        }

        abstract void processResultTerm(IStrategoTerm resultTerm, ConstraintAnalyzerContext context, KeyedMessagesBuilder messagesBuilder);
    }

    class Full extends Expect {
        Full(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(IStrategoTerm resultTerm, ConstraintAnalyzerContext context, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm ast = results.get(0);
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                context.updateResult(resource, ast, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }

    class Update extends Expect {
        Update(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(IStrategoTerm resultTerm, ConstraintAnalyzerContext context, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Update", 4)) != null) {
                final IStrategoTerm analysis = results.get(0);
                addResultMessages(results.get(1), results.get(2), results.get(3), messagesBuilder);
                context.updateResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }

    class Project extends Expect {
        Project(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(IStrategoTerm resultTerm, ConstraintAnalyzerContext context, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                context.updateResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }


    private IStrategoTerm mkAppl(String op, IStrategoTerm... subterms) {
        return termFactory.makeAppl(termFactory.makeConstructor(op, subterms.length), subterms);
    }

    private IStrategoTerm mkTuple(IStrategoTerm... subterms) {
        return termFactory.makeTuple(subterms);
    }

    private IStrategoString mkString(Object obj) {
        return termFactory.makeString(obj.toString());
    }

    private IStrategoTerm mkProjectTerm(ResourceKey resource) {
        final String resourceStr = resource.toString(); // TODO: must use ResourceService to turn this into a string.
        IStrategoTerm ast = termFactory.makeTuple();
        ast = StrategoTermIndices.put(TermIndex.of(resourceStr, 0), ast, termFactory);
        TermOrigin.of(resourceStr).put(ast);
        return ast;
    }


    private static @Nullable List<IStrategoTerm> match(@Nullable IStrategoTerm term, String op, int n) {
        if(term == null || !Tools.isTermAppl(term) || !Tools.hasConstructor((IStrategoAppl) term, op, n)) {
            return null;
        }
        return Arrays.asList(term.getAllSubterms());
    }
}