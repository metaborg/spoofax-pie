package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CfgStatixConfig;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.function.Function4Throwing1;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.Export;
import mb.spoofax.core.language.ResourceExports;
import mb.spoofax.core.resource.ResourcesComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponentManagerWrapper;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerScope;
import mb.spoofax.lwb.compiler.definition.LanguageDefinitionManager;
import mb.spoofax.lwb.compiler.definition.ResolveDependencies;
import mb.spoofax.lwb.compiler.definition.UnarchiveUtil;
import mb.statix.util.StatixUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@SpoofaxLwbCompilerScope
public class SpoofaxStatixResolveDependencies extends ResolveDependencies<StatixResolvedDependency, Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException> {
    @Inject public SpoofaxStatixResolveDependencies(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        LoggerFactory loggerFactory,
        ResourceService resourceService,
        UnarchiveFromJar unarchiveFromJar,
        Provider<SpoofaxStatixConfigure> configureTaskDefProvider
    ) {
        super(
            cfgRootDirectoryToObject,
            languageDefinitionManager,
            componentManagerWrapper,
            new FromComponent(loggerFactory, resourceService, unarchiveFromJar),
            configureTaskDefProvider,
            FromConfiguredLanguageDefinition.instance,
            FromLanguageDefinition.instance,
            StatixUtil.displayName
        );
    }

    @Override public String getId() {
        return getClass().getName();
    }

    static class FromComponent implements Function4Throwing1<ResourceExports, ResourcesComponent, ExecContext, ResourcePath, ListView<StatixResolvedDependency>, IOException> {
        private final Logger logger;
        private final ResourceService resourceService;
        private final UnarchiveFromJar unarchiveFromJar;

        FromComponent(LoggerFactory loggerFactory, ResourceService resourceService, UnarchiveFromJar unarchiveFromJar) {
            this.logger = loggerFactory.create(getClass());
            this.resourceService = resourceService;
            this.unarchiveFromJar = unarchiveFromJar;
        }

        @Override
        public ListView<StatixResolvedDependency> apply(
            ResourceExports resourceExports,
            ResourcesComponent resourcesComponent,
            ExecContext context,
            ResourcePath unarchiveDirectoryBase
        ) throws IOException {
            final ArrayList<StatixResolvedDependency> resolved = new ArrayList<>();
            final LinkedHashSet<ResourcePath> unarchivedDefinitionLocations = UnarchiveUtil.unarchive(
                context,
                resourcesComponent,
                unarchiveDirectoryBase,
                unarchiveFromJar,
                StatixUtil.extensionsPathStringMatcher
            );
            for(Export export : resourceExports.getExports(CfgStatixConfig.exportsId)) {
                export.caseOf()
                    .file(relativePath -> {
                        logger.warn("Attempting to resolve dependency to Statix file export '" + relativePath + "', but individual file exports are not supported");
                        return None.instance;
                    })
                    .directory(relativePath -> {
                        UnarchiveUtil.resolveDirectory(relativePath, unarchivedDefinitionLocations, resourceService, StatixUtil.displayName, directory ->
                            resolved.add(StatixResolvedDependency.sourceDirectory(directory.getPath())));
                        return None.instance;
                    });
            }
            return ListView.of(resolved);
        }
    }

    static class FromConfiguredLanguageDefinition extends StatelessSerializableFunction<Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException>, Result<Option<ListView<StatixResolvedDependency>>, SpoofaxStatixConfigureException>> {
        public static final SpoofaxStatixResolveDependencies.FromConfiguredLanguageDefinition instance = new SpoofaxStatixResolveDependencies.FromConfiguredLanguageDefinition();

        @Override
        public Result<Option<ListView<StatixResolvedDependency>>, SpoofaxStatixConfigureException> apply(Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException> result) {
            return result.map(o -> o.map(this::resolve));
        }

        private ListView<StatixResolvedDependency> resolve(SpoofaxStatixConfig config) {
            return ListView.of();
        }

        private FromConfiguredLanguageDefinition() {}

        private Object readResolve() {return instance;}
    }

    static class FromLanguageDefinition extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<StatixResolvedDependency>>, CfgRootDirectoryToObjectException>> {
        public static final FromLanguageDefinition instance = new FromLanguageDefinition();

        @Override
        public Result<Option<ListView<StatixResolvedDependency>>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageDefinitionInput.compileMetaLanguageSourcesInput().statix()).map(c -> resolve(o.rootDirectory, c)));
        }

        private ListView<StatixResolvedDependency> resolve(ResourcePath rootDirectory, CfgStatixConfig config) {
            final ArrayList<StatixResolvedDependency> resolved = new ArrayList<>();
            config.source().getFiles().ifPresent(files -> {
                for(String relativePath : files.exportDirectories()) {
                    final ResourcePath directory = rootDirectory.appendAsRelativePath(relativePath);
                    resolved.add(StatixResolvedDependency.sourceDirectory(directory));
                }
            });
            return ListView.of(resolved);
        }

        private FromLanguageDefinition() {}

        private Object readResolve() {return instance;}
    }
}
