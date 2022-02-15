package mb.spoofax.lwb.compiler.dynamix;

import mb.cfg.metalang.CfgDynamixConfig;
import mb.cfg.metalang.CfgDynamixSource;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.dynamix.task.DynamixConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.STask;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.TaskDef;
import mb.pie.api.exec.UncheckedInterruptedException;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration task for Dynamix in the context of the Spoofax LWB compiler.
 */
public class SpoofaxDynamixConfigure implements TaskDef<ResourcePath, Result<Option<SpoofaxDynamixConfig>, SpoofaxDynamixConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    @Inject public SpoofaxDynamixConfigure(
        CfgRootDirectoryToObject cfgRootDirectoryToObject
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<SpoofaxDynamixConfig>, SpoofaxDynamixConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, new DynamixConfigMapper())
            .mapErr(SpoofaxDynamixConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<SpoofaxDynamixConfig>, Exception>flatMapThrowing(o -> Result.transpose(o.mapThrowing(c -> configure(context, rootDirectory, c))));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<SpoofaxDynamixConfig, SpoofaxDynamixConfigureException> configure(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgDynamixConfig cfgDynamixConfig
    ) throws IOException, InterruptedException {
        try {
            return cfgDynamixConfig.source().caseOf()
                .files((files -> configureSourceFilesCatching(context, rootDirectory, cfgDynamixConfig, files)))
                .prebuilt((specAtermDirectory) -> configurePrebuilt(cfgDynamixConfig, specAtermDirectory))
                ;
        } catch(UncheckedIOException e) {
            throw e.getCause();
        } // No need to unwrap UncheckedInterruptedException here, PIE handles UncheckedInterruptedException.
    }

    public Result<SpoofaxDynamixConfig, SpoofaxDynamixConfigureException> configureSourceFilesCatching(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgDynamixConfig cfgDynamixConfig,
        CfgDynamixSource.Files files
    ) {
        try {
            return configureSourceFiles(context, rootDirectory, cfgDynamixConfig, files);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        } catch(InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
    }

    public Result<SpoofaxDynamixConfig, SpoofaxDynamixConfigureException> configureSourceFiles(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgDynamixConfig cfgDynamixConfig,
        CfgDynamixSource.Files files
    ) throws IOException, InterruptedException {
        final HierarchicalResource mainSourceDirectory = context.require(files.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(SpoofaxDynamixConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(files.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(SpoofaxDynamixConfigureException.mainFileFail(mainFile.getPath()));
        }
        // TODO: check include directories.
        // TODO: check source directories (if added to input).

        // Gather origins for provided Dynamix files.
        final ArrayList<STask<?>> sourceFileOrigins = new ArrayList<>();
        // Gather include directories. Use LinkedHashSet to remove duplicates while keeping insertion order.
        final LinkedHashSet<ResourcePath> includeDirectories = new LinkedHashSet<>();
        includeDirectories.addAll(files.includeDirectories());

        // TODO: compilation

        final DynamixConfig dynamixConfig = new DynamixConfig(
            rootDirectory,
            mainFile.getPath(),
            ListView.of(mainSourceDirectory.getPath()),
            ListView.copyOf(includeDirectories),
            ListView.of(sourceFileOrigins)
        );
        return Result.ofOk(SpoofaxDynamixConfig.files(dynamixConfig, cfgDynamixConfig.outputSpecAtermDirectory()));
    }

    public Result<SpoofaxDynamixConfig, SpoofaxDynamixConfigureException> configurePrebuilt(
        CfgDynamixConfig cfgDynamixConfig,
        ResourcePath inputSpecAtermDirectory
    ) {
        return Result.ofOk(SpoofaxDynamixConfig.prebuilt(inputSpecAtermDirectory, cfgDynamixConfig.outputSpecAtermDirectory()));
    }

    // todo: compilation

    private static class DynamixConfigMapper extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<CfgDynamixConfig>, CfgRootDirectoryToObjectException>> {
        @Override
        public Result<Option<CfgDynamixConfig>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().dynamix()));
        }
    }
}
