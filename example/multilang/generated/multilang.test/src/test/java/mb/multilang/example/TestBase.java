package mb.multilang.example;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerModule;
import mb.minisdf.DaggerMiniSdfComponent;
import mb.minisdf.DaggerMiniSdfResourcesComponent;
import mb.minisdf.MiniSdfComponent;
import mb.minisdf.MiniSdfResourcesComponent;
import mb.ministr.DaggerMiniStrComponent;
import mb.ministr.DaggerMiniStrResourcesComponent;
import mb.ministr.MiniStrComponent;
import mb.ministr.MiniStrResourcesComponent;
import mb.resource.dagger.ResourceRegistriesProvider;
import mb.spoofax.core.language.LanguageComponent;
import mb.spoofax.test.MultiLanguageTestBase;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.MultiLangModule;
import mb.statix.multilang.metadata.AnalysisContextService;
import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.ImmutableAnalysisContextService;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.SpecConfig;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.jupiter.api.AfterEach;

import java.util.Collection;
import java.util.HashMap;

public class TestBase extends MultiLanguageTestBase {
    public final MiniSdfResourcesComponent miniSdfResourcesComponent = DaggerMiniSdfResourcesComponent.create();
    public final MiniStrResourcesComponent miniStrResourcesComponent = DaggerMiniStrResourcesComponent.create();
    @MonotonicNonNull MultiLangComponent multiLangComponent;
    @MonotonicNonNull MiniSdfComponent miniSdfComponent;
    @MonotonicNonNull MiniStrComponent miniStrComponent;

    public TestBase() {
        super(DaggerLoggerComponent.builder().loggerModule(LoggerModule.stdOutVerbose()).build());
    }

    @Override protected Collection<ResourceRegistriesProvider> createResourcesComponents() {
        return list(miniSdfResourcesComponent, miniStrResourcesComponent);
    }

    @Override protected Collection<LanguageComponent> createComponents() {
        multiLangComponent = DaggerMultiLangComponent.builder()
            .loggerComponent(loggerComponent)
            .multiLangModule(new MultiLangModule(this::getAnalysisContextService))
            .build();
        miniSdfComponent = DaggerMiniSdfComponent.builder()
            .loggerComponent(loggerComponent)
            .miniSdfResourcesComponent(miniSdfResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .multiLangComponent(multiLangComponent)
            .build();
        miniStrComponent = DaggerMiniStrComponent.builder()
            .loggerComponent(loggerComponent)
            .miniStrResourcesComponent(miniStrResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .multiLangComponent(multiLangComponent)
            .build();
        return list(miniSdfComponent, miniStrComponent);
    }

    private AnalysisContextService getAnalysisContextService() {
        HashMap<SpecFragmentId, SpecConfig> specConfigs = new HashMap<>();
        specConfigs.putAll(miniSdfComponent.getSpecConfigs());
        specConfigs.putAll(miniStrComponent.getSpecConfigs());
        return ImmutableAnalysisContextService
            .builder()
            .putAllSpecConfigs(specConfigs)
            .putLanguageMetadataSuppliers(new LanguageId("mb.minisdf"), miniSdfComponent::getLanguageMetadata)
            .putLanguageMetadataSuppliers(new LanguageId("mb.ministr"), miniStrComponent::getLanguageMetadata)
            .putDefaultLanguageContexts(new LanguageId("mb.minisdf"), new ContextId("mb.multilang"))
            .putDefaultLanguageContexts(new LanguageId("mb.ministr"), new ContextId("mb.multilang"))
            .build();
    }

    @AfterEach void closeLanguageComponents() throws Exception {
        miniStrComponent.close();
        miniSdfComponent.close();
    }
}
