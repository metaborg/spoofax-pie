package mb.statix.multilang;

import dagger.Module;
import dagger.Provides;
import mb.statix.multilang.metadata.AnalysisContextService;
import mb.statix.multilang.metadata.ContextDataManager;
import mb.statix.multilang.metadata.ContextPieManager;
import mb.statix.multilang.metadata.LanguageMetadataManager;
import mb.statix.multilang.metadata.SpecManager;
import mb.statix.multilang.pie.SmlBuildSpec;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.SmlSolveProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.config.SmlReadConfigYaml;

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
    public LanguageMetadataManager provideLanguageMetadataManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @MultiLangScope @MultiLang
    public ContextPieManager provideContextPieManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @MultiLangScope @MultiLang
    public ContextDataManager provideContextDataManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @MultiLangScope @MultiLang
    public SpecManager provideSpecManager(@MultiLang AnalysisContextService analysisContextService) {
        return analysisContextService;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlSolveProject provideSolveProject(SmlSolveProject solveProject) {
        return solveProject;
    }

    @Provides @MultiLangScope @MultiLang
    public SmlBuildContextConfiguration provideSmlBuildContextConfiguration(SmlBuildContextConfiguration buildContextConfiguration) {
        return buildContextConfiguration;
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
}
