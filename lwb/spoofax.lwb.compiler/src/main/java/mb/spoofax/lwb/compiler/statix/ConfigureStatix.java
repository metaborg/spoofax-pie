package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixConfig;

import javax.inject.Inject;
import java.io.IOException;

public class ConfigureStatix implements TaskDef<ResourcePath, Result<Option<StatixConfig>, StatixConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    @Inject public ConfigureStatix(
        CfgRootDirectoryToObject cfgRootDirectoryToObject
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<StatixConfig>, StatixConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StatixConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<StatixConfig>, IOException>flatMapThrowing(cfgOutput -> Result.transpose(Option.ofOptional(cfgOutput.compileLanguageToJavaClassPathInput.compileLanguageInput().statix())
                .mapThrowing(statixInput -> toStatixConfig(context, rootDirectory, statixInput))
            ));
    }


    public Result<StatixConfig, StatixConfigureException> toStatixConfig(
        ExecContext context,
        ResourcePath rootDirectory,
        CompileStatixInput statixInput
    ) throws IOException {
        final HierarchicalResource mainSourceDirectory = context.require(statixInput.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(StatixConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(statixInput.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StatixConfigureException.mainFileFail(mainFile.getPath()));
        }
        // TODO: check include directories.
        // TODO: check source directories (if added to input).
        return Result.ofOk(new StatixConfig(rootDirectory, mainFile.getPath(), ListView.of(mainSourceDirectory.getPath()), ListView.of(statixInput.includeDirectories())));
    }
}
