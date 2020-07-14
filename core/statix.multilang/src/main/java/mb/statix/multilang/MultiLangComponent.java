package mb.statix.multilang;

import dagger.Component;
import mb.spoofax.core.platform.PlatformComponent;
import mb.statix.multilang.pie.SmlAnalyzeProject;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import mb.statix.multilang.pie.SmlBuildMessages;
import mb.statix.multilang.pie.SmlBuildSpec;
import mb.statix.multilang.pie.SmlInstantiateGlobalScope;
import mb.statix.multilang.pie.SmlPartialSolveFile;
import mb.statix.multilang.pie.SmlPartialSolveProject;
import mb.statix.multilang.pie.config.SmlReadConfigYaml;

@MultiLangScope
@Component(
    modules = MultiLangModule.class,
    dependencies = PlatformComponent.class
)
public interface MultiLangComponent {
    @MultiLang AnalysisContextService getAnalysisContextService();
    @MultiLang SmlAnalyzeProject getAnalyzeProject();
    @MultiLang SmlBuildContextConfiguration getBuildContextConfiguration();
    @MultiLang SmlBuildMessages getBuildMessages();
    @MultiLang SmlBuildSpec getBuildSpec();
    @MultiLang SmlInstantiateGlobalScope getInstantiateGlobalScope();
    @MultiLang SmlPartialSolveFile getPartialSolveFile();
    @MultiLang SmlPartialSolveProject getPartialSolveProject();
    @MultiLang SmlReadConfigYaml getReadConfigYaml();
}
