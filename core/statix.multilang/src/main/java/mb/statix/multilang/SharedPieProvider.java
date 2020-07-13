package mb.statix.multilang;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.pie.PieProvider;
import mb.statix.multilang.pie.SmlBuildContextConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@LanguageScope
public class SharedPieProvider implements PieProvider {

    private final Pie languagePie;
    private final LanguageId languageId;
    private final AnalysisContextService analysisContextService;
    private final SmlBuildContextConfiguration buildContextConfiguration;

    @Inject public SharedPieProvider(
        Pie languagePie,
        LanguageId languageId,
        AnalysisContextService analysisContextService,
        SmlBuildContextConfiguration buildContextConfiguration
    ) {
        this.languagePie = languagePie;
        this.languageId = languageId;
        this.analysisContextService = analysisContextService;
        this.buildContextConfiguration = buildContextConfiguration;
    }

    @Override
    public Pie getPie(@Nullable ResourcePath projectDir) {
        try(MixedSession session = languagePie.newSession()) {
            if(projectDir == null) {
                // Return default Pie
                ContextId defaultContextId = analysisContextService.getDefaultContextId(languageId);
                Set<LanguageId> languageIds = analysisContextService.getContextLanguages(defaultContextId);
                return analysisContextService.buildPieForLanguages(languageIds);
            }
            List<LanguageId> languageIds = session.require(buildContextConfiguration
                .createTask(new SmlBuildContextConfiguration.Input(projectDir, languageId)))
                .unwrap()
                .getContextConfig()
                .getLanguages();
            return analysisContextService.buildPieForLanguages(languageIds);
        } catch(InterruptedException | ExecException | MultiLangAnalysisException e) {
            throw new RuntimeException("Cannot build pie for " + languageId + " in " + projectDir, e);
        }
    }
}
