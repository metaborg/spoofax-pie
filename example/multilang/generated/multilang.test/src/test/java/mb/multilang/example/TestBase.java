package mb.multilang.example;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.log.slf4j.SLF4JLoggerFactory;
import mb.minisdf.DaggerMiniSdfComponent;
import mb.minisdf.DaggerMiniSdfResourcesComponent;
import mb.minisdf.MiniSdfComponent;
import mb.minisdf.MiniSdfModule;
import mb.minisdf.MiniSdfResourcesComponent;
import mb.ministr.DaggerMiniStrComponent;
import mb.ministr.DaggerMiniStrResourcesComponent;
import mb.ministr.MiniStrComponent;
import mb.ministr.MiniStrModule;
import mb.ministr.MiniStrResourcesComponent;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.runtime.PieBuilderImpl;
import mb.resource.ResourceKey;
import mb.resource.fs.FSResource;
import mb.resource.text.TextResourceRegistry;
import mb.spoofax.core.platform.BaseResourceServiceComponent;
import mb.spoofax.core.platform.BaseResourceServiceModule;
import mb.spoofax.core.platform.DaggerBaseResourceServiceComponent;
import mb.spoofax.core.platform.DaggerPlatformComponent;
import mb.spoofax.core.platform.LoggerFactoryModule;
import mb.spoofax.core.platform.PlatformComponent;
import mb.spoofax.core.platform.PlatformPieModule;
import mb.statix.multilang.DaggerMultiLangComponent;
import mb.statix.multilang.MultiLangComponent;
import mb.statix.multilang.MultiLangModule;
import mb.statix.multilang.metadata.AnalysisContextService;
import mb.statix.multilang.metadata.ContextId;
import mb.statix.multilang.metadata.ImmutableAnalysisContextService;
import mb.statix.multilang.metadata.LanguageId;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.multilang.metadata.spec.SpecConfig;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.util.HashMap;

public class TestBase {

    public final FileSystem fileSystem = Jimfs.newFileSystem("multilang-test", Configuration.unix());
    public final FSResource rootDirectory = new FSResource(fileSystem.getPath("/", "virtual-project-dir"));
    public final TextResourceRegistry textResourceRegistry = new TextResourceRegistry();

    final MiniSdfResourcesComponent miniSdfResourcesComponent = DaggerMiniSdfResourcesComponent.create();
    final MiniStrResourcesComponent miniStrResourcesComponent = DaggerMiniStrResourcesComponent.create();
    final BaseResourceServiceModule resourceServiceModule = new BaseResourceServiceModule()
        .addRegistry(textResourceRegistry)
        .addRegistries(miniSdfResourcesComponent.getResourceRegistries())
        .addRegistries(miniStrResourcesComponent.getResourceRegistries());
    final BaseResourceServiceComponent resourceServiceComponent = DaggerBaseResourceServiceComponent.builder()
        .baseResourceServiceModule(resourceServiceModule)
        .build();

    public final PlatformComponent platformComponent = DaggerPlatformComponent.builder()
        .loggerFactoryModule(new LoggerFactoryModule(new SLF4JLoggerFactory()))
        .platformPieModule(new PlatformPieModule(PieBuilderImpl::new))
        .resourceServiceComponent(resourceServiceComponent)
        .build();

    public final LoggerFactory loggerFactory = platformComponent.getLoggerFactory();
    public final Logger log = loggerFactory.create(TestBase.class);

    public final MultiLangComponent multiLangComponent = DaggerMultiLangComponent.builder()
        .platformComponent(platformComponent)
        .multiLangModule(new MultiLangModule(this::getAnalysisContextService))
        .build();

    @SuppressWarnings({"deprecation", "InstantiationOfUtilityClass"})
    public final MiniSdfComponent miniSdfComponent = DaggerMiniSdfComponent.builder()
        .miniSdfResourcesComponent(miniSdfResourcesComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .platformComponent(platformComponent)
        .multiLangComponent(multiLangComponent)
        .miniSdfModule(new MiniSdfModule())
        .build();

    @SuppressWarnings({"deprecation", "InstantiationOfUtilityClass"})
    public final MiniStrComponent miniStrComponent = DaggerMiniStrComponent.builder()
        .miniStrResourcesComponent(miniStrResourcesComponent)
        .resourceServiceComponent(resourceServiceComponent)
        .platformComponent(platformComponent)
        .multiLangComponent(multiLangComponent)
        .miniStrModule(new MiniStrModule())
        .build();

    public final Pie pie = miniSdfComponent.getPie();

    public FSResource createTextFile(FSResource rootDirectory, String text, String relativePath) throws IOException {
        final FSResource resource = rootDirectory.appendRelativePath(relativePath);
        resource.createFile(true);
        resource.writeString(text, StandardCharsets.UTF_8);
        return resource;
    }

    public FSResource createTextFile(String text, String relativePath) throws IOException {
        return createTextFile(this.rootDirectory, text, relativePath);
    }

    public ResourceStringSupplier resourceStringSupplier(ResourceKey resourceKey) {
        return new ResourceStringSupplier(resourceKey);
    }

    public MixedSession newSession() {
        return pie.newSession();
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
            .platformPieBuilder(platformComponent.newPieBuilder())
            .putDefaultLanguageContexts(new LanguageId("mb.minisdf"), new ContextId("mb.multilang"))
            .putDefaultLanguageContexts(new LanguageId("mb.ministr"), new ContextId("mb.multilang"))
            .build();
    }

    @AfterEach public void cleanupFs() throws IOException {
        if(fileSystem != null && fileSystem.isOpen()) {
            fileSystem.close();
        }
    }
}
