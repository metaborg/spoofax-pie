package mb.statix.multilang;

import dagger.Lazy;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.pie.PieProvider;
import mb.statix.multilang.pie.config.SmlBuildContextConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@LanguageScope
public class SharedPieProvider implements PieProvider {

    private final Pie languagePie;
    private final LanguageId languageId;
    private final Lazy<AnalysisContextService> analysisContextService;
    private final SmlBuildContextConfiguration buildContextConfiguration;

    @Inject public SharedPieProvider(
        Pie languagePie,
        LanguageId languageId,
        Lazy<AnalysisContextService> analysisContextService,
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
                // Return default Pie TODO: call get once
                ContextId defaultContextId = analysisContextService.get().getDefaultContextId(languageId);
                Set<LanguageId> languageIds = analysisContextService.get().getContextLanguages(defaultContextId);
                return analysisContextService.get().buildPieForLanguages(languageIds);
            }
            List<LanguageId> languageIds = session.require(buildContextConfiguration
                .createTask(new SmlBuildContextConfiguration.Input(projectDir, languageId)))
                .unwrap()
                .getContextConfig()
                .getLanguages();
            return analysisContextService.get().buildPieForLanguages(languageIds);
        } catch(InterruptedException | ExecException | MultiLangAnalysisException e) {
            throw new RuntimeException("Cannot build pie for " + languageId + " in " + projectDir, e);
        }
    }
}
