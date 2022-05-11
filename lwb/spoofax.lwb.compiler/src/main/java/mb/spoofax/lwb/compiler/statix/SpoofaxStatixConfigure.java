package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CfgStatixConfig;
import mb.cfg.metalang.CfgStatixSource;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.exec.UncheckedInterruptedException;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3_ext_statix.task.Sdf3ExtStatixGenerateStatix;
import mb.spoofax.lwb.compiler.definition.ResolveDependencies;
import mb.spoofax.lwb.compiler.definition.ResolveDependenciesException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3ConfigureException;
import mb.spoofax.lwb.compiler.sdf3.SpoofaxSdf3GenerationUtil;
import mb.statix.task.StatixConfig;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration task for Statix in the context of the Spoofax LWB compiler.
 */
public class SpoofaxStatixConfigure implements TaskDef<ResourcePath, Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final SpoofaxStatixResolveDependencies resolveDependencies;

    private final SpoofaxStatixGenerationUtil spoofaxStatixGenerationUtil;

    private final SpoofaxSdf3GenerationUtil spoofaxSdf3GenerationUtil;
    private final Sdf3ExtStatixGenerateStatix sdf3ExtStatixGenerateStatix;

    @Inject public SpoofaxStatixConfigure(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        SpoofaxStatixResolveDependencies resolveDependencies,
        SpoofaxStatixGenerationUtil spoofaxStatixGenerationUtil,
        SpoofaxSdf3GenerationUtil spoofaxSdf3GenerationUtil,
        Sdf3ExtStatixGenerateStatix sdf3ExtStatixGenerateStatix
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.resolveDependencies = resolveDependencies;
        this.spoofaxStatixGenerationUtil = spoofaxStatixGenerationUtil;
        this.spoofaxSdf3GenerationUtil = spoofaxSdf3GenerationUtil;
        this.sdf3ExtStatixGenerateStatix = sdf3ExtStatixGenerateStatix;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<SpoofaxStatixConfig>, SpoofaxStatixConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, new StatixConfigMapper())
            .mapErr(SpoofaxStatixConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<SpoofaxStatixConfig>, Exception>flatMapThrowing(o -> Result.transpose(o.mapThrowing(c -> configure(context, rootDirectory, c))));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<SpoofaxStatixConfig, SpoofaxStatixConfigureException> configure(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgStatixConfig cfgStatixConfig
    ) throws IOException, InterruptedException {
        try {
            return cfgStatixConfig.source().caseOf()
                .files((files -> configureSourceFilesCatching(context, rootDirectory, cfgStatixConfig, files)))
                .prebuilt((specAtermDirectory) -> configurePrebuilt(cfgStatixConfig, specAtermDirectory))
                ;
        } catch(UncheckedIOException e) {
            throw e.getCause();
        } // No need to unwrap UncheckedInterruptedException here, PIE handles UncheckedInterruptedException.
    }

    public Result<SpoofaxStatixConfig, SpoofaxStatixConfigureException> configureSourceFilesCatching(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgStatixConfig cfgStatixConfig,
        CfgStatixSource.Files files
    ) {
        try {
            return configureSourceFiles(context, rootDirectory, cfgStatixConfig, files);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        } catch(InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
    }

    public Result<SpoofaxStatixConfig, SpoofaxStatixConfigureException> configureSourceFiles(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgStatixConfig cfgStatixConfig,
        CfgStatixSource.Files files
    ) throws IOException, InterruptedException {
        final HierarchicalResource mainSourceDirectory = context.require(files.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(SpoofaxStatixConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(files.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(SpoofaxStatixConfigureException.mainFileFail(mainFile.getPath()));
        }
        for(ResourcePath includeDirectoryPath : files.includeDirectories()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(SpoofaxStatixConfigureException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Gather origins for provided Statix files.
        final ArrayList<STask<?>> sourceFileOrigins = new ArrayList<>();

        // Gather include directories.
        final LinkedHashSet<ResourcePath> allIncludeDirectories = new LinkedHashSet<>(); // LinkedHashSet to remove duplicates while keeping insertion order.
        allIncludeDirectories.addAll(files.includeDirectories());

        final Task<Result<ListView<StatixResolvedDependency>, ResolveDependenciesException>> resolveDependenciesTask =
            resolveDependencies.createTask(new ResolveDependencies.Input(rootDirectory, files.unarchiveDirectory()));
        sourceFileOrigins.add(resolveDependenciesTask.toSupplier());
        final Result<ListView<StatixResolvedDependency>, ResolveDependenciesException> result =
            context.require(resolveDependenciesTask);
        if(result.isErr()) {
            // noinspection ConstantConditions (err is present)
            return Result.ofErr(SpoofaxStatixConfigureException.resolveIncludeFail(result.getErr()));
        } else {
            // noinspection ConstantConditions (value is present)
            for(StatixResolvedDependency resolved : result.get()) {
                StatixResolvedDependency.cases().sourceDirectory(d -> {
                    allIncludeDirectories.add(d);
                    return None.instance;
                }).apply(resolved);
            }
        }

        // Compile each SDF3 source file (if SDF3 is enabled) to a Statix signature module (if enabled).
        final ResourcePath generatedSourcesDirectory = cfgStatixConfig.generatedSourcesDirectory();
        if(files.enableSdf3SignatureGen()) {
            try {
                spoofaxSdf3GenerationUtil.performSdf3GenerationIfEnabled(context, rootDirectory, new SpoofaxSdf3GenerationUtil.Callbacks<SpoofaxStatixConfigureException>() {
                    @Override
                    public void generateFromAst(ExecContext context, STask<Result<IStrategoTerm, ?>> astSupplier) throws SpoofaxStatixConfigureException, InterruptedException {
                        try {
                            sdf3ToStatixGenInj(context, generatedSourcesDirectory, astSupplier);
                        } catch(RuntimeException | InterruptedException e) {
                            throw e; // Do not wrap runtime and interrupted exceptions, rethrow them.
                        } catch(Exception e) {
                            throw SpoofaxStatixConfigureException.sdf3ExtStatixGenInjFail(e);
                        }
                    }

                    @Override
                    public void generateFromConfig(ExecContext context, Sdf3SpecConfig sdf3Config) {
                        // Add generated sources directory as an include Statix imports.
                        allIncludeDirectories.add(generatedSourcesDirectory);
                        // Add this as an origin, as this task provides the Statix files (in statixGenerationUtil.writePrettyPrintedFile).
                        sourceFileOrigins.add(createSupplier(rootDirectory));
                    }
                });
            } catch(SpoofaxStatixConfigureException e) {
                return Result.ofErr(e);
            } catch(SpoofaxSdf3ConfigureException e) {
                return Result.ofErr(SpoofaxStatixConfigureException.sdf3ConfigureFail(e));
            }
        }

        final StatixConfig statixConfig = new StatixConfig(
            rootDirectory,
            mainFile.getPath(),
            ListView.of(mainSourceDirectory.getPath()),
            ListView.copyOf(allIncludeDirectories),
            ListView.of(sourceFileOrigins)
        );
        return Result.ofOk(SpoofaxStatixConfig.files(statixConfig, cfgStatixConfig.outputSpecAtermDirectory()));
    }

    public Result<SpoofaxStatixConfig, SpoofaxStatixConfigureException> configurePrebuilt(
        CfgStatixConfig cfgStatixConfig,
        ResourcePath inputSpecAtermDirectory
    ) {
        return Result.ofOk(SpoofaxStatixConfig.prebuilt(inputSpecAtermDirectory, cfgStatixConfig.outputSpecAtermDirectory()));
    }

    private void sdf3ToStatixGenInj(
        ExecContext context,
        ResourcePath generatesSourcesDirectory,
        STask<Result<IStrategoTerm, ?>> astSupplier
    ) throws Exception {
        final STask<Result<IStrategoTerm, ?>> supplier = sdf3ExtStatixGenerateStatix.createSupplier(astSupplier);
        spoofaxStatixGenerationUtil.writePrettyPrintedFile(context, generatesSourcesDirectory, supplier);
    }

    private static class StatixConfigMapper extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<CfgStatixConfig>, CfgRootDirectoryToObjectException>> {
        @Override
        public Result<Option<CfgStatixConfig>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageDefinitionInput.compileMetaLanguageSourcesInput().statix()));
        }
    }
}
