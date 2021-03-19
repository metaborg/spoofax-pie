package mb.constraint.common;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.TermIndex;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

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
        public final IStrategoTerm ast;
        public final IStrategoTerm analysis;

        public Result(ResourceKey resource, IStrategoTerm ast, IStrategoTerm analysis) {
            this.resource = resource;
            this.ast = ast;
            this.analysis = analysis;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Result result = (Result)o;
            if(!resource.equals(result.resource)) return false;
            if(!ast.equals(result.ast)) return false;
            return analysis.equals(result.analysis);
        }

        @Override public int hashCode() {
            int result = resource.hashCode();
            result = 31 * result + ast.hashCode();
            result = 31 * result + analysis.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Result{" +
                "resource=" + resource +
                ", ast=" + ast +
                ", analysis=" + analysis +
                '}';
        }
    }

    public static class ProjectResult implements Serializable {
        public final ResourceKey resource;
        public final IStrategoTerm analysis;

        public ProjectResult(ResourceKey resource, IStrategoTerm analysis) {
            this.resource = resource;
            this.analysis = analysis;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final ProjectResult that = (ProjectResult)o;
            if(!resource.equals(that.resource)) return false;
            return analysis.equals(that.analysis);
        }

        @Override public int hashCode() {
            int result = resource.hashCode();
            result = 31 * result + analysis.hashCode();
            return result;
        }

        @Override public String toString() {
            return "ProjectResult{" +
                "resource=" + resource +
                ", analysis=" + analysis +
                '}';
        }
    }


    public static class SingleFileResult implements Serializable {
        public final @Nullable ProjectResult projectResult;
        public final ResourceKey resource;
        public final IStrategoTerm ast;
        public final IStrategoTerm analysis;
        public final Messages messages;

        public SingleFileResult(@Nullable ProjectResult projectResult, ResourceKey resource, IStrategoTerm ast, IStrategoTerm analysis, Messages messages) {
            this.projectResult = projectResult;
            this.resource = resource;
            this.ast = ast;
            this.analysis = analysis;
            this.messages = messages;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final SingleFileResult that = (SingleFileResult)o;
            if(projectResult != null ? !projectResult.equals(that.projectResult) : that.projectResult != null)
                return false;
            if(!resource.equals(that.resource)) return false;
            if(!ast.equals(that.ast)) return false;
            if(!analysis.equals(that.analysis)) return false;
            return messages.equals(that.messages);
        }

        @Override public int hashCode() {
            int result = projectResult != null ? projectResult.hashCode() : 0;
            result = 31 * result + resource.hashCode();
            result = 31 * result + ast.hashCode();
            result = 31 * result + analysis.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "SingleFileResult{" +
                "projectResult=" + projectResult +
                ", resource=" + resource +
                ", ast=" + ast +
                ", analysis=" + analysis +
                ", messages=" + messages +
                '}';
        }
    }

    public static class MultiFileResult implements Serializable {
        public final @Nullable ProjectResult projectResult;
        public final ArrayList<Result> results;
        public final KeyedMessages messages;

        public MultiFileResult(@Nullable ProjectResult projectResult, ArrayList<Result> results, KeyedMessages messages) {
            this.projectResult = projectResult;
            this.results = results;
            this.messages = messages;
        }

        public MultiFileResult(ArrayList<Result> results, KeyedMessages messages) {
            this(null, results, messages);
        }

        public MultiFileResult(KeyedMessages messages) {
            this(new ArrayList<>(), messages);
        }

        public MultiFileResult() {
            this(KeyedMessages.of());
        }

        public @Nullable Result getResult(ResourceKey resource) {
            // OPTO: linear search to lookup. Cannot use HashMap because it does not serialize properly.
            return results.stream().filter((r) -> r.resource.equals(resource)).findFirst().orElse(null);
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final MultiFileResult that = (MultiFileResult)o;
            if(projectResult != null ? !projectResult.equals(that.projectResult) : that.projectResult != null)
                return false;
            if(!results.equals(that.results)) return false;
            return messages.equals(that.messages);
        }

        @Override public int hashCode() {
            int result = projectResult != null ? projectResult.hashCode() : 0;
            result = 31 * result + results.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "MultiFileResult{" +
                "projectResult=" + projectResult +
                ", results=" + results +
                ", messages=" + messages +
                '}';
        }
    }


    private final ResourceService resourceService;
    private final String strategyId;
    private final boolean multiFile;


    public ConstraintAnalyzer(
        ResourceService resourceService,
        String strategyId,
        boolean multiFile
    ) {
        this.resourceService = resourceService;
        this.strategyId = strategyId;
        this.multiFile = multiFile;
    }


    public SingleFileResult analyze(
        ResourceKey resource,
        IStrategoTerm ast,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime
    ) throws ConstraintAnalyzerException {
        return analyze(null, resource, ast, context, strategoRuntime);
    }

    public SingleFileResult analyze(
        @Nullable ResourceKey root,
        ResourceKey resource,
        IStrategoTerm ast,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime
    ) throws ConstraintAnalyzerException {
        final HashMap<ResourceKey, IStrategoTerm> asts = new HashMap<>(1);
        asts.put(resource, ast);
        final MultiFileResult multiFileResult = doAnalyze(root, asts, context, strategoRuntime);
        final @Nullable Result result;
        try {
            result = multiFileResult.results.get(0);
        } catch(IndexOutOfBoundsException e) {
            throw new RuntimeException("BUG: no analysis result was found for resource '" + resource + "'", e);
        }
        return new SingleFileResult(multiFileResult.projectResult, result.resource, result.ast, result.analysis, multiFileResult.messages.asMessages());
    }

    public MultiFileResult analyze(
        @Nullable ResourceKey root,
        HashMap<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime
    ) throws ConstraintAnalyzerException {
        return doAnalyze(root, asts, context, strategoRuntime);
    }


    private MultiFileResult doAnalyze(
        @Nullable ResourceKey root,
        HashMap<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime
    ) throws ConstraintAnalyzerException {
        final ITermFactory termFactory = strategoRuntime.getTermFactory();

        /// 1. Compute changeset from given asts and cache.

        final HashMap<ResourceKey, IStrategoTerm> addedOrChangedAsts = new HashMap<>();
        for(Entry<ResourceKey, IStrategoTerm> entry : asts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final IStrategoTerm ast = entry.getValue();
            ResourceKeyAttachment.setResourceKey(ast, resource);
            addedOrChangedAsts.put(resource, ast);
        }
        final HashSet<ResourceKey> removed = new HashSet<>(context.getResultResources());
        removed.removeAll(addedOrChangedAsts.keySet());

        /// 2. Transform changeset into list of changed terms and expect objects, and remove invalidated units from the context.

        final ArrayList<IStrategoTerm> changeTerms = new ArrayList<>();
        final HashMap<ResourceKey, Expect> expects = new HashMap<>();

        // Root analysis.
        final @Nullable IStrategoTerm rootChange;
        if(multiFile && root != null) {
            final IStrategoTerm ast = mkProjectTerm(termFactory, root);
            final IStrategoTerm change;
            final Expect expect;
            final @Nullable Result cachedResult = context.getResult(root);
            if(cachedResult != null) {
                change = termFactory.makeAppl("Cached", cachedResult.analysis);
                expect = new ProjectUpdate(root);
                context.removeProjectResult(root);
            } else {
                change = termFactory.makeAppl("Added", ast);
                expect = new ProjectFull(root);
            }
            expects.put(root, expect);
            rootChange = termFactory.makeTuple(termFactory.makeString(root.toString()), change);
        } else {
            rootChange = null;
        }

        // Removed resources.
        for(ResourceKey resource : removed) {
            final @Nullable Result cachedResult = context.getResult(resource);
            if(cachedResult != null) {
                changeTerms.add(termFactory.makeTuple(termFactory.makeString(resource.toString()), termFactory.makeAppl("Removed", cachedResult.analysis)));
                context.removeResult(resource);
            }
        }

        // Added and changed resources.
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChangedAsts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final IStrategoTerm ast = entry.getValue();
            final IStrategoTerm change;
            final @Nullable Result cachedResult = context.getResult(resource);
            if(cachedResult != null) {
                change = termFactory.makeAppl("Changed", ast, cachedResult.analysis);
                context.removeResult(resource);
            } else {
                change = termFactory.makeAppl("Added", ast);
            }
            expects.put(resource, new Full(resource));
            changeTerms.add(termFactory.makeTuple(termFactory.makeString(resource.toString()), change));
        }

        // Cached resources.
        if(multiFile) {
            for(Map.Entry<ResourceKey, Result> entry : context.getResultEntries()) {
                final ResourceKey resource = entry.getKey();
                final Result cachedResult = entry.getValue();
                if(!addedOrChangedAsts.containsKey(resource)) {
                    final IStrategoTerm change = termFactory.makeAppl("Cached", cachedResult.analysis);
                    expects.put(resource, new Update(resource));
                    changeTerms.add(termFactory.makeTuple(termFactory.makeString(resource.toString()), change));
                }
            }
        }

        // Progress and cancel terms
        final IStrategoTerm progressTerm = termFactory.makeTuple();   // ()
        final IStrategoTerm cancelTerm = termFactory.makeTuple();     // ()

        /// 3. Call analysis, and list results.

        final Map<ResourceKey, IStrategoTerm> resultTerms = new HashMap<>();
        final IStrategoTerm action;
        if(multiFile && root != null) {
            action = termFactory.makeAppl("AnalyzeMulti", rootChange, termFactory.makeList(changeTerms), progressTerm, cancelTerm);
        } else {
            action = termFactory.makeAppl("AnalyzeSingle", termFactory.makeList(changeTerms), progressTerm, cancelTerm);
        }

        final IStrategoTerm allResultsTerm;
        try {
            allResultsTerm = strategoRuntime.invoke(strategyId, action);
        } catch(StrategoException e) {
            throw ConstraintAnalyzerException.strategoInvokeFail(e);
        }

        final @Nullable List<IStrategoTerm> allResultTerms = match(allResultsTerm, "AnalysisResult", 1);
        if(allResultTerms == null || allResultTerms.isEmpty()) {
            throw new RuntimeException("BUG: invalid constraint analysis result, got " + allResultsTerm);
        }

        final IStrategoTerm resultsTerm = allResultTerms.get(0);
        if(!TermUtils.isList(resultsTerm)) {
            throw new RuntimeException("BUG: expected list of results, got: " + resultsTerm);
        }
        for(IStrategoTerm entry : resultsTerm) {
            if(!TermUtils.isTuple(entry, 2)) {
                throw new RuntimeException("BUG: expected tuple result, got " + entry);
            }
            final IStrategoTerm resourceTerm = entry.getSubterm(0);
            if(!TermUtils.isString(resourceTerm)) {
                throw new RuntimeException("BUG: expected resource string as first component, got " + resourceTerm);
            }
            final String resourceString = TermUtils.toJavaString(resourceTerm);
            final ResourceKey resource;
            try {
                resource = resourceService.getResourceKey(ResourceKeyString.parse(resourceString));
            } catch(ResourceRuntimeException e) {
                throw new RuntimeException(
                    "BUG: could not get resource for resource string '" + resourceString + "' in result term " + entry, e);
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
        if(root != null) {
            if(!resultTerms.containsKey(root)) {
                expects.get(root).addFailMessage("Missing project result", messagesBuilder);
            }
        }
        for(Map.Entry<ResourceKey, IStrategoTerm> entry : addedOrChangedAsts.entrySet()) {
            final ResourceKey resource = entry.getKey();
            if(!resultTerms.containsKey(resource)) {
                expects.get(resource).addFailMessage("Missing analysis result", messagesBuilder);
            }
        }

        /// 5. Build and return result object.

        final @Nullable ProjectResult projectResult;
        if(root != null) {
            projectResult = context.getProjectResult(root);
            if(projectResult == null) {
                throw new RuntimeException("BUG: context is missing project result for '" + root + "'");
            }
        } else {
            projectResult = null;
        }

        final ArrayList<Result> results = new ArrayList<>(asts.size());
        for(ResourceKey resource : addedOrChangedAsts.keySet()) {
            final @Nullable Result result = context.getResult(resource);
            if(result != null) {
                results.add(result);
            } else {
                throw new RuntimeException("BUG: context is missing analysis result for '" + resource + "'");
            }
        }

        return new MultiFileResult(projectResult, results, messagesBuilder.build());
    }


    abstract class Expect {
        final ResourceKey resource;

        Expect(ResourceKey resource) {
            this.resource = resource;
        }

        void addResultMessages(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable ResourceKey resourceOverride = multiFile ? null : resource;
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

    class ProjectUpdate extends Expect {
        ProjectUpdate(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(IStrategoTerm resultTerm, ConstraintAnalyzerContext context, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Update", 4)) != null) {
                final IStrategoTerm analysis = results.get(0);
                addResultMessages(results.get(1), results.get(2), results.get(3), messagesBuilder);
                context.updateProjectResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeProjectResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }

    class ProjectFull extends Expect {
        ProjectFull(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(IStrategoTerm resultTerm, ConstraintAnalyzerContext context, KeyedMessagesBuilder messagesBuilder) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder);
                context.updateProjectResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeProjectResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }
    }


    private IStrategoTerm mkProjectTerm(ITermFactory termFactory, ResourceKey resourceKey) {
        final String resourceStr = resourceKey.asString();
        IStrategoTerm ast = termFactory.makeTuple();
        ast = StrategoTermIndices.put(TermIndex.of(resourceStr, 0), ast, termFactory);
        TermOrigin.of(resourceStr).put(ast);
        return ast;
    }


    private static @Nullable List<IStrategoTerm> match(@Nullable IStrategoTerm term, String op, int n) {
        if(!TermUtils.isAppl(term, op, n)) {
            return null;
        }
        return Arrays.asList(term.getAllSubterms());
    }
}
