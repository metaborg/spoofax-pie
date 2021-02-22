package mb.statix.multilang;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.statix.multilang.metadata.AnalysisContextService;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.SmlSolveProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.config.SmlReadConfigYaml;
import mb.statix.multilang.pie.spec.SmlBuildSpec;
import mb.statix.multilang.pie.spec.SmlLoadFragment;

@MultiLangScope
@Component(
    modules = {
        MultiLangModule.class
    },
    dependencies = {
        LoggerComponent.class
    }
)
public interface MultiLangComponent {

    @MultiLang AnalysisContextService getAnalysisContextService();

    @MultiLang SmlSolveProject getAnalyzeProject();

    @MultiLang SmlBuildContextConfiguration getBuildContextConfiguration();

    @MultiLang SmlLoadFragment getLoadFragment();

    @MultiLang SmlBuildSpec getBuildSpec();

    @MultiLang SmlInstantiateGlobalScope getInstantiateGlobalScope();

    @MultiLang SmlPartialSolveFile getPartialSolveFile();

    @MultiLang SmlPartialSolveProject getPartialSolveProject();

    @MultiLang SmlReadConfigYaml getReadConfigYaml();
}
