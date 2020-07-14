package mb.statix.multilang;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.statix.multilang.pie.SmlAnalyzeProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlBuildSpec;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.config.SmlReadConfigYaml;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@Module
public class MultiLangModule {
    private final Supplier<AnalysisContextService> analysisContextServiceSupplier;

    public MultiLangModule(Supplier<AnalysisContextService> analysisContextServiceSupplier) {
        this.analysisContextServiceSupplier = analysisContextServiceSupplier;
    }

    @Provides @MultiLangScope @MultiLang
    public AnalysisContextService provideAnalysisContextService() {
        return analysisContextServiceSupplier.get();
    }

    @Provides @MultiLangScope @MultiLang
    public SmlAnalyzeProject provideAnalyzeProject(SmlAnalyzeProject analyzeProject) {
        return analyzeProject;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlBuildContextConfiguration provideSmlBuildContextConfiguration(SmlBuildContextConfiguration buildContextConfiguration) {
        return buildContextConfiguration;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlBuildMessages provideBuildMessages(SmlBuildMessages buildMessages) {
        return buildMessages;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlBuildSpec provideBuildSpec(SmlBuildSpec buildSpec) {
        return buildSpec;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlInstantiateGlobalScope provideInstantiateGlobalScope(SmlInstantiateGlobalScope instantiateGlobalScope) {
        return instantiateGlobalScope;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlPartialSolveFile providePartialSolveFile(SmlPartialSolveFile partialSolveFile) {
        return partialSolveFile;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlPartialSolveProject providePartialSolveProject(SmlPartialSolveProject partialSolveProject) {
        return partialSolveProject;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlReadConfigYaml provideReadConfigYaml(SmlReadConfigYaml readConfigYaml) {
        return readConfigYaml;
    }

    @Provides @MultiLangScope @MultiLang @ElementsIntoSet
    public Set<TaskDef<?, ?>> provideTaskDefs(
        @MultiLang SmlAnalyzeProject analyzeProject,
        @MultiLang SmlBuildContextConfiguration buildContextConfiguration,
        @MultiLang SmlBuildMessages buildMessages,
        @MultiLang SmlBuildSpec buildSpec,
        @MultiLang SmlInstantiateGlobalScope instantiateGlobalScope,
        @MultiLang SmlPartialSolveFile partialSolveFile,
        @MultiLang SmlPartialSolveProject partialSolveProject,
        @MultiLang SmlReadConfigYaml readConfigYaml
    ) {
        Set<TaskDef<?, ?>> taskDefs = new HashSet<>();
        taskDefs.add(analyzeProject);
        taskDefs.add(buildContextConfiguration);
        taskDefs.add(buildMessages);
        taskDefs.add(buildSpec);
        taskDefs.add(instantiateGlobalScope);
        taskDefs.add(partialSolveFile);
        taskDefs.add(partialSolveProject);
        taskDefs.add(readConfigYaml);
        return taskDefs;
    }
}
