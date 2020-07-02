package mb.statix.multilang.pie;

import com.google.common.collect.ListMultimap;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.constraints.CConj;
import mb.statix.multilang.AnalysisContextService;
import mb.statix.multilang.AnalysisResults;
import mb.statix.multilang.ContextConfig;
import mb.statix.multilang.ContextId;
import mb.statix.multilang.FileResult;
import mb.statix.multilang.ImmutableAnalysisResults;
import mb.statix.multilang.LanguageId;
import mb.statix.multilang.MultiLangAnalysisException;
import mb.statix.multilang.MultiLangScope;
import mb.statix.solver.IConstraint;
import mb.statix.solver.IState;
import mb.statix.solver.log.IDebugContext;
import mb.statix.solver.persistent.Solver;
import mb.statix.solver.persistent.SolverResult;
import mb.statix.spec.Rule;
import mb.statix.spec.Spec;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.log.Level;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MultiLangScope
public class SmlAnalyzeProject implements TaskDef<SmlAnalyzeProject.Input, AnalysisResults> {
    public static class Input implements Serializable {
        private final ResourcePath projectPath;
        private final HashSet<LanguageId> languages;
        private final ContextId contextId;
        private final Level logLevel;

        public Input(ResourcePath projectPath, HashSet<LanguageId> languages, ContextId contextId, Level logLevel) {
            this.projectPath = projectPath;
            this.languages = languages;
            this.contextId = contextId;
            this.logLevel = logLevel;
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            Input input = (Input)o;
            return projectPath.equals(input.projectPath) &&
                languages.equals(input.languages) &&
                contextId.equals(input.contextId);
        }

        @Override public int hashCode() {
            return Objects.hash(projectPath, languages, contextId);
        }

        @Override public String toString() {
            return "Input{" +
                "projectPath=" + projectPath +
                ", languages=" + languages +
                ", contextId=" + contextId +
                '}';
        }
    }

    private final SmlInstantiateGlobalScope instantiateGlobalScope;
    private final SmlPartialSolveProject partialSolveProject;
    private final SmlPartialSolveFile partialSolveFile;
    private final SmlBuildContextConfiguration buildContextConfiguration;
    private final SmlBuildSpec buildSpec;
    private final AnalysisContextService analysisContextService;
    private final Logger logger;

    @Inject public SmlAnalyzeProject(
        SmlInstantiateGlobalScope instantiateGlobalScope,
        SmlPartialSolveProject partialSolveProject,
        SmlPartialSolveFile partialSolveFile,
        SmlBuildContextConfiguration buildContextConfiguration,
        SmlBuildSpec buildSpec,
        AnalysisContextService analysisContextService,
        LoggerFactory loggerFactory
    ) {
        this.instantiateGlobalScope = instantiateGlobalScope;
        this.partialSolveProject = partialSolveProject;
        this.partialSolveFile = partialSolveFile;
        this.buildContextConfiguration = buildContextConfiguration;
        this.buildSpec = buildSpec;
        this.analysisContextService = analysisContextService;
        logger = loggerFactory.create(SmlAnalyzeProject.class);
    }

    @Override public String getId() {
        return SmlAnalyzeProject.class.getSimpleName();
    }

    @Override public AnalysisResults exec(ExecContext context, Input input) throws Exception {
        /* final ContextConfig config = context.require(buildContextConfiguration.createTask(
            new SmlBuildContextConfiguration.Input(input.projectPath, input.contextId)
        )); */

        final Supplier<Spec> specSupplier = buildSpec.createSupplier(new SmlBuildSpec.Input(input.languages));
        final Spec combinedSpec = context.require(specSupplier);

        final ListMultimap<String, Rule> rulesWithEquivalentPatterns = combinedSpec.rules().getAllEquivalentRules();
        if(!rulesWithEquivalentPatterns.isEmpty()) {
            logger.error("+--------------------------------------+");
            logger.error("| FOUND RULES WITH EQUIVALENT PATTERNS |");
            logger.error("+--------------------------------------+");
            for(Map.Entry<String, Collection<Rule>> entry : rulesWithEquivalentPatterns.asMap().entrySet()) {
                logger.error("| Overlapping rules for: {}", entry.getKey());
                for(Rule rule : entry.getValue()) {
                    logger.error("| * {}", rule);
                }
            }
            logger.error("+--------------------------------------+");
        }

        IDebugContext debug = TaskUtils.createDebugContext(String.format("MLA [%s]", input.contextId), input.logLevel);

        Supplier<GlobalResult> globalResultSupplier = instantiateGlobalScope.createSupplier(
            new SmlInstantiateGlobalScope.Input(input.contextId.toString(), input.logLevel, specSupplier));

        Map<LanguageId, SolverResult> projectResults = input.languages.stream()
            .collect(Collectors.toMap(Function.identity(), languageId ->
                context.require(partialSolveProject.createTask(new SmlPartialSolveProject.Input(
                    globalResultSupplier,
                    specSupplier,
                    analysisContextService.getLanguageMetadata(languageId).projectConstraint(),
                    input.logLevel)))
            ));

        // Create file results (maintain resource key for error message mapping
        Map<AnalysisResults.FileKey, FileResult> fileResults = input.languages.stream()
            .map(analysisContextService::getLanguageMetadata)
            .flatMap(languageMetadata ->
                languageMetadata.resourcesSupplier().apply(context, input.projectPath).stream()
                    .map(resourceKey -> {
                        FileResult fileResult = context.require(partialSolveFile.createTask(new SmlPartialSolveFile.Input(
                            languageMetadata.astFunction(),
                            languageMetadata.postTransform(),
                            resourceKey,
                            specSupplier,
                            globalResultSupplier,
                            languageMetadata.fileConstraint(),
                            input.logLevel)));
                        return new AbstractMap.SimpleEntry<>(
                            new AnalysisResults.FileKey(languageMetadata.languageId(), resourceKey),
                            fileResult);
                    })
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        IState.Immutable combinedState = Stream.concat(
            projectResults.values().stream(),
            fileResults.values().stream().map(FileResult::getResult))
            .map(SolverResult::state)
            .reduce(IState.Immutable::add)
            .orElseThrow(() -> new MultiLangAnalysisException("Expected at least one result"));

        GlobalResult globalResult = context.require(globalResultSupplier);
        IConstraint combinedConstraint = Stream.concat(
            projectResults.values().stream(),
            fileResults.values().stream().map(FileResult::getResult))
            .map(SolverResult::delayed)
            .reduce(globalResult.getResult().delayed(), CConj::new);

        long t0 = System.currentTimeMillis();
        SolverResult finalResult = Solver.solve(combinedSpec, combinedState, combinedConstraint, (s, l, st) -> true, debug);
        long dt = System.currentTimeMillis() - t0;
        logger.info("{} analyzed in {} ms", input.contextId, dt);

        return ImmutableAnalysisResults.of(globalResult.getGlobalScope(),
            new HashMap<>(projectResults), new HashMap<>(fileResults), finalResult);
    }
}
