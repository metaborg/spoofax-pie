package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.metalang.CfgStrategoConfig;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.function.Function4Throwing1;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.common.util.SetView;
import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResourceLocations;
import mb.resource.classloader.JarFileWithPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.FilenameExtensionUtil;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.ExtensionsPathStringMatcher;
import mb.spoofax.core.language.Export;
import mb.spoofax.core.language.ResourceExports;
import mb.spoofax.core.resource.ResourcesComponent;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerComponentManagerWrapper;
import mb.spoofax.lwb.compiler.SpoofaxLwbCompilerScope;
import mb.spoofax.lwb.compiler.definition.LanguageDefinitionManager;
import mb.spoofax.lwb.compiler.definition.ResolveDependencies;
import mb.spoofax.lwb.compiler.definition.UnarchiveUtil;
import mb.spoofax.resource.ClassLoaderResources;
import mb.str.util.StrategoUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@SpoofaxLwbCompilerScope
public class SpoofaxStrategoResolveDependencies extends ResolveDependencies<StrategoResolvedDependency> {
    @Inject public SpoofaxStrategoResolveDependencies(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        LanguageDefinitionManager languageDefinitionManager,
        SpoofaxLwbCompilerComponentManagerWrapper componentManagerWrapper,
        LoggerFactory loggerFactory,
        ResourceService resourceService,
        UnarchiveFromJar unarchiveFromJar,
        Provider<SpoofaxStrategoConfigure> configureTaskDefProvider
    ) {
        super(
            cfgRootDirectoryToObject,
            languageDefinitionManager,
            componentManagerWrapper,
            new FromComponent(loggerFactory, resourceService, unarchiveFromJar),
            configureTaskDefProvider,
            FromLanguageDefinition.instance,
            StrategoUtil.displayName
        );
    }

    @Override public String getId() {
        return getClass().getName();
    }

    static class FromComponent implements Function4Throwing1<ResourceExports, ResourcesComponent, ExecContext, ResourcePath, ListView<StrategoResolvedDependency>, IOException> {
        private final Logger logger;
        private final ResourceService resourceService;
        private final UnarchiveFromJar unarchiveFromJar;

        FromComponent(LoggerFactory loggerFactory, ResourceService resourceService, UnarchiveFromJar unarchiveFromJar) {
            this.logger = loggerFactory.create(getClass());
            this.resourceService = resourceService;
            this.unarchiveFromJar = unarchiveFromJar;
        }

        @Override
        public ListView<StrategoResolvedDependency> apply(
            ResourceExports resourceExports,
            ResourcesComponent resourcesComponent,
            ExecContext context,
            ResourcePath unarchiveDirectoryBase
        ) throws IOException {
            final ArrayList<StrategoResolvedDependency> resolved = new ArrayList<>();
            final ListView<Export> exports = resourceExports.getExports(CfgStrategoConfig.exportsId);

            boolean requireUnarchiveStr2Lib = false;
            boolean requireUnarchiveSources = false;
            for(Export export : exports) {
                requireUnarchiveStr2Lib = requireUnarchiveStr2Lib || export.caseOf()
                    .file(FromComponent::isStr2LibFile)
                    .directory_(false)
                ;
                requireUnarchiveSources = requireUnarchiveSources || export.caseOf()
                    .file_(false)
                    .directory_(true)
                ;
            }
            final LinkedHashSet<ResourcePath> unarchivedDefinitionLocations;
            if(requireUnarchiveStr2Lib || requireUnarchiveSources) {
                final LinkedHashSet<String> extensions = new LinkedHashSet<>();
                if(requireUnarchiveStr2Lib) {
                    extensions.add("str2lib");
                }
                if(requireUnarchiveSources) {
                    StrategoUtil.fileExtensions.addAllTo(extensions);
                }
                unarchivedDefinitionLocations = UnarchiveUtil.unarchive(
                    context,
                    resourcesComponent,
                    unarchiveDirectoryBase,
                    unarchiveFromJar,
                    new ExtensionsPathStringMatcher(extensions)
                );
            } else {
                unarchivedDefinitionLocations = new LinkedHashSet<>();
            }

            for(Export export : exports) {
                export.caseOf()
                    .file(relativePath -> {
                        resolveFile(relativePath, unarchivedDefinitionLocations, resourcesComponent.getClassloaderResources(), resolved);
                        return None.instance;
                    })
                    .directory(relativePath -> {
                        UnarchiveUtil.resolveDirectory(relativePath, unarchivedDefinitionLocations, resourceService, StrategoUtil.displayName, directory ->
                            resolved.add(StrategoResolvedDependency.sourceDirectory(directory.getPath())));
                        return None.instance;
                    });
            }
            return ListView.of(resolved);
        }

        private void resolveFile(
            String relativePath,
            LinkedHashSet<ResourcePath> unarchivedDefinitionLocations,
            ClassLoaderResources classLoaderResources,
            ArrayList<StrategoResolvedDependency> resolved
        ) {
            if(isStr2LibFile(relativePath)) {
                boolean found = false;
                for(ResourcePath definitionDirectory : unarchivedDefinitionLocations) {
                    final ResourcePath str2LibFilePath = definitionDirectory.appendAsRelativePath(relativePath);
                    final HierarchicalResource str2LibFile = resourceService.getHierarchicalResource(str2LibFilePath);
                    try {
                        if(str2LibFile.exists() && str2LibFile.isFile()) {
                            final LinkedHashSet<File> javaClassPaths = new LinkedHashSet<>();
                            final ClassLoaderResourceLocations<FSResource> locations = classLoaderResources.definitionDirectory.getLocations();
                            for(FSResource directory : locations.directories) {
                                javaClassPaths.add(directory.getJavaPath().toFile());
                            }
                            for(JarFileWithPath<FSResource> jarFileWithPath : locations.jarFiles) {
                                javaClassPaths.add(jarFileWithPath.file.getPath().getJavaPath().toFile());
                            }
                            resolved.add(StrategoResolvedDependency.compiledLibrary(str2LibFilePath, SetView.of(javaClassPaths)));
                            found = true;
                        }
                    } catch(IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
                if(!found) {
                    throw new UncheckedIOException(new IOException("Stratego file export '" + relativePath + "' was not found in any of its definition locations: " + unarchivedDefinitionLocations));
                }
            } else {
                logger.warn("Attempting to resolve dependency to Stratego file export '" + relativePath + "', but individual file exports are not supported (with the exception of .str2lib files)");
            }
        }

        private static boolean isStr2LibFile(String relativePath) {
            return FilenameExtensionUtil.hasExtension(relativePath, "str2lib");
        }
    }

    static class FromLanguageDefinition extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<ListView<StrategoResolvedDependency>>, CfgRootDirectoryToObjectException>> {
        public static final FromLanguageDefinition instance = new FromLanguageDefinition();

        @Override
        public Result<Option<ListView<StrategoResolvedDependency>>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().stratego()).map(c -> resolve(o.rootDirectory, c)));
        }

        private ListView<StrategoResolvedDependency> resolve(ResourcePath rootDirectory, CfgStrategoConfig config) {
            final List<String> exportDirectories = config.source().getFiles().exportDirectories();
            final ArrayList<StrategoResolvedDependency> resolved = new ArrayList<>(exportDirectories.size());
            for(String relativePath : exportDirectories) {
                final ResourcePath directory = rootDirectory.appendAsRelativePath(relativePath);
                resolved.add(StrategoResolvedDependency.sourceDirectory(directory));
            }
            return ListView.of(resolved);
        }

        private FromLanguageDefinition() {}

        private Object readResolve() {return instance;}
    }
}
