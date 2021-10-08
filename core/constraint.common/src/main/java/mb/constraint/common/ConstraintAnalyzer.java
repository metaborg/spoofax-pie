package mb.constraint.common;

import mb.aterm.common.TermToString;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Messages;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.util.MapView;
import mb.jsglr.common.ResourceKeyAttachment;
import mb.jsglr.common.TermTracer;
import mb.nabl2.terms.stratego.StrategoBlob;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.TermIndex;
import mb.nabl2.terms.stratego.TermOrigin;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.common.StrategoException;
import mb.stratego.common.StrategoRuntime;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.task.ThreadCancel;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ConstraintAnalyzer {
    public static class Result implements Serializable {
        public final ResourceKey resource;
        public final IStrategoTerm parsedAst;
        public final IStrategoTerm analyzedAst;
        public final IStrategoTerm analysis;

        public Result(
            ResourceKey resource,
            IStrategoTerm parsedAst,
            IStrategoTerm analyzedAst,
            IStrategoTerm analysis
        ) {
            this.resource = resource;
            this.parsedAst = parsedAst;
            this.analyzedAst = analyzedAst;
            this.analysis = analysis;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Result result = (Result)o;
            if(!resource.equals(result.resource)) return false;
            if(!parsedAst.equals(result.parsedAst)) return false;
            if(!analyzedAst.equals(result.analyzedAst)) return false;
            return analysis.equals(result.analysis);
        }

        @Override public int hashCode() {
            int result = resource.hashCode();
            result = 31 * result + parsedAst.hashCode();
            result = 31 * result + analyzedAst.hashCode();
            result = 31 * result + analysis.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Result{" +
                "resource=" + resource +
                ", parsedAst=" + TermToString.toShortString(parsedAst) +
                ", analyzedAst=" + TermToString.toShortString(analyzedAst) +
                ", analysis=" + TermToString.toShortString(analysis) +
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

        @Override public boolean equals(@Nullable Object o) {
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
                ", analysis=" + TermToString.toShortString(analysis) +
                '}';
        }
    }


    public static class SingleFileResult implements Serializable {
        public final @Nullable ProjectResult projectResult;
        public final ResourceKey resource;
        public final IStrategoTerm parsedAst;
        public final IStrategoTerm analyzedAst;
        public final IStrategoTerm analysis;
        public final Messages messages;

        public SingleFileResult(
            @Nullable ProjectResult projectResult,
            ResourceKey resource,
            IStrategoTerm parsedAst,
            IStrategoTerm analyzedAst,
            IStrategoTerm analysis,
            Messages messages
        ) {
            this.projectResult = projectResult;
            this.resource = resource;
            this.parsedAst = parsedAst;
            this.analyzedAst = analyzedAst;
            this.analysis = analysis;
            this.messages = messages;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final SingleFileResult that = (SingleFileResult)o;
            if(projectResult != null ? !projectResult.equals(that.projectResult) : that.projectResult != null)
                return false;
            if(!resource.equals(that.resource)) return false;
            if(!parsedAst.equals(that.parsedAst)) return false;
            if(!analyzedAst.equals(that.analyzedAst)) return false;
            if(!analysis.equals(that.analysis)) return false;
            return messages.equals(that.messages);
        }

        @Override public int hashCode() {
            int result = projectResult != null ? projectResult.hashCode() : 0;
            result = 31 * result + resource.hashCode();
            result = 31 * result + parsedAst.hashCode();
            result = 31 * result + analyzedAst.hashCode();
            result = 31 * result + analysis.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "SingleFileResult{" +
                "projectResult=" + projectResult +
                ", resource=" + resource +
                ", parsedAst=" + TermToString.toShortString(parsedAst) +
                ", analyzedAst=" + TermToString.toShortString(analyzedAst) +
                ", analysis=" + TermToString.toShortString(analysis) +
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

        @Override public boolean equals(@Nullable Object o) {
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
        StrategoRuntime strategoRuntime,
        ResourceService resourceService
    ) throws ConstraintAnalyzerException {
        return analyze(null, resource, ast, context, strategoRuntime, resourceService);
    }

    public SingleFileResult analyze(
        @Nullable ResourcePath root,
        ResourceKey resource,
        IStrategoTerm ast,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime,
        ResourceService resourceService
    ) throws ConstraintAnalyzerException {
        final MapView<ResourceKey, IStrategoTerm> asts = MapView.of(resource, ast);
        final MultiFileResult multiFileResult = doAnalyze(root, asts, context, strategoRuntime, resourceService);
        final @Nullable Result result;
        try {
            result = multiFileResult.results.get(0);
        } catch(IndexOutOfBoundsException e) {
            throw new RuntimeException("BUG: no analysis result was found for resource '" + resource + "'", e);
        }
        return new SingleFileResult(multiFileResult.projectResult, result.resource, ast, result.analyzedAst, result.analysis, multiFileResult.messages.asMessages());
    }

    public MultiFileResult analyze(
        @Nullable ResourcePath root,
        MapView<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime,
        ResourceService resourceService
    ) throws ConstraintAnalyzerException {
        return doAnalyze(root, asts, context, strategoRuntime, resourceService);
    }


    private MultiFileResult doAnalyze(
        @Nullable ResourcePath root,
        MapView<ResourceKey, IStrategoTerm> asts,
        ConstraintAnalyzerContext context,
        StrategoRuntime strategoRuntime,
        ResourceService resourceService
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
            final @Nullable ProjectResult cachedResult = context.getProjectResult(root);
            if(cachedResult != null) {
                change = termFactory.makeAppl("Cached", cachedResult.analysis);
                expect = new ProjectUpdate(root);
                context.removeProjectResult(root); // TODO: is it needed to remove this?
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
                if(cachedResult.parsedAst.hashCode() != ast.hashCode() || !cachedResult.parsedAst.equals(ast) || !regionsEqual(cachedResult.parsedAst, ast)) {
                    change = termFactory.makeAppl("Changed", ast, cachedResult.analysis);
                    context.removeResult(resource); // TODO: is it needed to remove this?
                    expects.put(resource, new Full(resource, ast));
                } else {
                    change = termFactory.makeAppl("Cached", cachedResult.analysis);
                    expects.put(resource, new Update(resource));
                }
            } else {
                change = termFactory.makeAppl("Added", ast);
                expects.put(resource, new Full(resource, ast));
            }
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
        final IStrategoTerm progressTerm = termFactory.makeTuple();
        final IStrategoTerm cancelTerm = new StrategoBlob(new ThreadCancel());

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
                expects.get(resource).processResultTerm(resultTerm, context, messagesBuilder, resourceService, root);
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
        for(Entry<ResourceKey, Expect> entry : expects.entrySet()) {
            final ResourceKey resource = entry.getKey();
            final Expect expect = entry.getValue();
            if(expect.requireResult() && !resultTerms.containsKey(resource)) {
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

    private boolean regionsEqual(IStrategoTerm ast1, IStrategoTerm ast2) {
        final @Nullable Region region1 = TermTracer.getRegion(ast1);
        final @Nullable Region region2 = TermTracer.getRegion(ast2);
        if(!Objects.equals(region1, region2)) return false;
        if(ast1.getSubtermCount() != ast2.getSubtermCount()) return false;
        final Iterator<IStrategoTerm> iterator1 = ast1.iterator();
        final Iterator<IStrategoTerm> iterator2 = ast2.iterator();
        while(iterator1.hasNext() && iterator2.hasNext()) {
            if(!regionsEqual(iterator1.next(), iterator2.next())) return false;
        }
        return true;
    }

    abstract class Expect {
        final ResourceKey resource;

        Expect(ResourceKey resource) {
            this.resource = resource;
        }

        void addResultMessages(
            IStrategoTerm errors,
            IStrategoTerm warnings,
            IStrategoTerm notes,
            KeyedMessagesBuilder messagesBuilder,
            ResourceService resourceService,
            @Nullable ResourcePath rootDirectory
        ) {
            final @Nullable ResourceKey resourceOverride = multiFile ? null : resource;
            MessageUtil.addMessagesFromTerm(messagesBuilder, errors, Severity.Error, resourceOverride, resourceService, rootDirectory);
            MessageUtil.addMessagesFromTerm(messagesBuilder, warnings, Severity.Warning, resourceOverride, resourceService, rootDirectory);
            MessageUtil.addMessagesFromTerm(messagesBuilder, notes, Severity.Info, resourceOverride, resourceService, rootDirectory);
        }

        void addFailMessage(String text, KeyedMessagesBuilder messagesBuilder) {
            messagesBuilder.addMessage(text, Severity.Error, resource);
        }

        abstract void processResultTerm(
            IStrategoTerm resultTerm,
            ConstraintAnalyzerContext context,
            KeyedMessagesBuilder messagesBuilder,
            ResourceService resourceService,
            @Nullable ResourcePath rootDirectory
        );

        abstract boolean requireResult();
    }

    class Full extends Expect {
        final IStrategoTerm parsedAst;

        Full(ResourceKey resourceKey, IStrategoTerm parsedAst) {
            super(resourceKey);
            this.parsedAst = parsedAst;
        }

        @Override
        public void processResultTerm(
            IStrategoTerm resultTerm,
            ConstraintAnalyzerContext context,
            KeyedMessagesBuilder messagesBuilder,
            ResourceService resourceService,
            @Nullable ResourcePath rootDirectory
        ) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm analyzedAst = results.get(0);
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder, resourceService, rootDirectory);
                context.updateResult(resource, parsedAst, analyzedAst, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }

        @Override boolean requireResult() {
            return true;
        }
    }

    class Update extends Expect {
        Update(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(
            IStrategoTerm resultTerm,
            ConstraintAnalyzerContext context,
            KeyedMessagesBuilder messagesBuilder,
            ResourceService resourceService,
            @Nullable ResourcePath rootDirectory
        ) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Update", 4)) != null) {
                final IStrategoTerm analysis = results.get(0);
                addResultMessages(results.get(1), results.get(2), results.get(3), messagesBuilder, resourceService, rootDirectory);
                context.updateResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }

        @Override boolean requireResult() {
            return false;
        }
    }

    class ProjectFull extends Expect {
        ProjectFull(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(
            IStrategoTerm resultTerm,
            ConstraintAnalyzerContext context,
            KeyedMessagesBuilder messagesBuilder,
            ResourceService resourceService,
            @Nullable ResourcePath rootDirectory
        ) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Full", 5)) != null) {
                final IStrategoTerm analysis = results.get(1);
                addResultMessages(results.get(2), results.get(3), results.get(4), messagesBuilder, resourceService, rootDirectory);
                context.updateProjectResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeProjectResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }

        @Override boolean requireResult() {
            return true;
        }
    }

    class ProjectUpdate extends Expect {
        ProjectUpdate(ResourceKey resourceKey) {
            super(resourceKey);
        }

        @Override
        public void processResultTerm(
            IStrategoTerm resultTerm,
            ConstraintAnalyzerContext context,
            KeyedMessagesBuilder messagesBuilder,
            ResourceService resourceService,
            @Nullable ResourcePath rootDirectory
        ) {
            final @Nullable List<IStrategoTerm> results;
            if((results = match(resultTerm, "Update", 4)) != null) {
                final IStrategoTerm analysis = results.get(0);
                addResultMessages(results.get(1), results.get(2), results.get(3), messagesBuilder, resourceService, rootDirectory);
                context.updateProjectResult(resource, analysis);
            } else if(match(resultTerm, "Failed", 0) != null) {
                addFailMessage("Analysis failed", messagesBuilder);
                context.removeProjectResult(resource);
            } else {
                addFailMessage("Analysis returned incorrect result", messagesBuilder);
            }
        }

        @Override boolean requireResult() {
            return false;
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
