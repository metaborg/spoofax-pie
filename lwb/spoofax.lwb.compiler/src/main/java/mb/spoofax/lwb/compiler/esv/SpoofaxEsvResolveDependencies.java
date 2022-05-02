package mb.spoofax.lwb.compiler.esv;

import mb.cfg.metalang.CfgEsvConfig;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.function.Function4Throwing1;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.esv.util.EsvUtil;
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

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@SpoofaxLwbCompilerScope
public class SpoofaxEsvResolveDependencies extends ResolveDependencies<EsvResolvedDependency> {
    @Inject public SpoofaxEsvResolveDependencies(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        LoggerFactory loggerFactory,
        ResourceService resourceService,
        UnarchiveFromJar unarchiveFromJar,
        Provider<SpoofaxEsvConfigure> configureTaskDefProvider
    ) {
        super(
            cfgRootDirectoryToObject,
            languageDefinitionManager,
            componentManagerWrapper,
            new FromComponent(loggerFactory, resourceService, unarchiveFromJar),
            configureTaskDefProvider,
            FromLanguageDefinition.instance,
            EsvUtil.displayName
        );
    }

    @Override public String getId() {
        return getClass().getName();
    }

    static class FromComponent implements Function4Throwing1<ResourceExports, ResourcesComponent, ExecContext, ResourcePath, ListView<EsvResolvedDependency>, IOException> {
        private final Logger logger;
        private final ResourceService resourceService;
        private final UnarchiveFromJar unarchiveFromJar;

        FromComponent(LoggerFactory loggerFactory, ResourceService resourceService, UnarchiveFromJar unarchiveFromJar) {
            this.logger = loggerFactory.create(getClass());
            this.resourceService = resourceService;
            this.unarchiveFromJar = unarchiveFromJar;
        }

        @Override
        public ListView<EsvResolvedDependency> apply(
            ResourceExports resourceExports,
            ResourcesComponent resourcesComponent,
            ExecContext context,
            ResourcePath unarchiveDirectoryBase
        ) throws IOException {
            final ArrayList<EsvResolvedDependency> resolved = new ArrayList<>();
            final LinkedHashSet<ResourcePath> unarchivedDefinitionLocations = UnarchiveUtil.unarchive(
                context,
                resourcesComponent,
                unarchiveDirectoryBase,
                unarchiveFromJar,
                EsvUtil.extensionsPathStringMatcher
            );
            for(Export export : resourceExports.getExports(CfgEsvConfig.exportsId)) {
                export.caseOf()
                    .file(relativePath -> {
                        logger.warn("Attempting to resolve dependency to ESV file export '" + relativePath + "', but individual file exports are not supported");
                        return None.instance;
                    })
                    .directory(relativePath -> {
                        UnarchiveUtil.resolveDirectory(relativePath, unarchivedDefinitionLocations, resourceService, EsvUtil.displayName, directory ->
                            resolved.add(EsvResolvedDependency.sourceDirectory(directory.getPath())));
                        return None.instance;
                    });
            }
            return ListView.of(resolved);
        }
    }

    static class FromLanguageDefinition extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<EsvResolvedDependency>>, CfgRootDirectoryToObjectException>> {
        public static final FromLanguageDefinition instance = new FromLanguageDefinition();

        @Override
        public Result<Option<ListView<EsvResolvedDependency>>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().esv()).map(c -> resolve(o.rootDirectory, c)));
        }

        private ListView<EsvResolvedDependency> resolve(ResourcePath rootDirectory, CfgEsvConfig config) {
            final ArrayList<EsvResolvedDependency> resolved = new ArrayList<>();
            config.source().getFiles().ifPresent(files -> {
                for(String relativePath : files.exportDirectories()) {
                    final ResourcePath directory = rootDirectory.appendAsRelativePath(relativePath);
                    resolved.add(EsvResolvedDependency.sourceDirectory(directory));
                }
            });
            return ListView.of(resolved);
        }

        private FromLanguageDefinition() {}

        private Object readResolve() {return instance;}
    }
}
