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
import javax.inject.Named;
import java.util.Set;

@LanguageScope
public class SharedPieProvider implements PieProvider {

    private final Pie languagePie;
    private final LanguageId languageId;
    private final Lazy<ContextPieManager> contextPieManager;
    private final SmlBuildContextConfiguration buildContextConfiguration;

    @Inject public SharedPieProvider(
        @Named("prototype") Pie languagePie,
        LanguageId languageId,
        Lazy<ContextPieManager> contextPieManager,
        SmlBuildContextConfiguration buildContextConfiguration
    ) {
        this.languagePie = languagePie;
        this.languageId = languageId;
        this.contextPieManager = contextPieManager;
        this.buildContextConfiguration = buildContextConfiguration;
    }

    @Override
    public Pie getPie(@Nullable ResourcePath projectDir) {
        ContextPieManager contextPieManager = this.contextPieManager.get();
        try(MixedSession session = languagePie.newSession()) {
            if(projectDir == null) {
                ContextId defaultContextId = contextPieManager.getDefaultContextId(languageId);
                Set<LanguageId> languageIds = contextPieManager.getContextLanguages(defaultContextId);
                return contextPieManager.buildPieForLanguages(languageIds);
            }
            Set<LanguageId> languageIds = session.require(buildContextConfiguration
                .createTask(new SmlBuildContextConfiguration.Input(projectDir, languageId)))
                .unwrap()
                .languages();
            return contextPieManager.buildPieForLanguages(languageIds);
        } catch(InterruptedException | ExecException | MultiLangAnalysisException e) {
            throw new RuntimeException("Cannot build pie for " + languageId + " in " + projectDir, e);
        }
    }
}
