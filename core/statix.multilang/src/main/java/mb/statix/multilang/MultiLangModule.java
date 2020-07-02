package mb.statix.multilang;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.statix.multilang.pie.SmlAnalyzeProject;
import mb.statix.multilang.pie.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlBuildSpec;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.SmlReadConfigYaml;

import java.util.HashSet;
import java.util.Set;

@Module
public class MultiLangModule {
    private final AnalysisContextService analysisContextService;

    public MultiLangModule(AnalysisContextService analysisContextService) {
        this.analysisContextService = analysisContextService;
    }

    @Provides @MultiLangScope public AnalysisContextService provideAnalysisContextService() {
        return analysisContextService;
    }

    @Provides @MultiLangScope @ElementsIntoSet
    public Set<TaskDef<?, ?>> provideTaskDefs(
        SmlAnalyzeProject analyzeProject,
        SmlBuildContextConfiguration buildContextConfiguration,
        SmlBuildMessages buildMessages,
        SmlBuildSpec buildSpec,
        SmlInstantiateGlobalScope instantiateGlobalScope,
        SmlPartialSolveFile partialSolveFile,
        SmlPartialSolveProject partialSolveProject,
        SmlReadConfigYaml readConfigYaml
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
