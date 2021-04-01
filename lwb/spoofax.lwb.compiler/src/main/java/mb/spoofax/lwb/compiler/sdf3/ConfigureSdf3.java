package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import java.io.IOException;

public class ConfigureSdf3 implements TaskDef<ResourcePath, Result<Option<Sdf3SpecConfig>, Sdf3ConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    @Inject public ConfigureSdf3(
        CfgRootDirectoryToObject cfgRootDirectoryToObject
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<Sdf3SpecConfig>, Sdf3ConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(Sdf3ConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<Sdf3SpecConfig>, IOException>flatMapThrowing(cfgOutput -> Result.transpose(Option.ofOptional(cfgOutput.compileLanguageInput.compileLanguageSpecificationInput().sdf3())
                .mapThrowing(sdf3Input -> toSdf3SpecConfig(context, rootDirectory, sdf3Input))
            ));
    }


    public Result<Sdf3SpecConfig, Sdf3ConfigureException> toSdf3SpecConfig(
        ExecContext context,
        ResourcePath rootDirectory,
        CompileSdf3Input sdf3Input
    ) throws IOException {
        final HierarchicalResource mainSourceDirectory = context.require(sdf3Input.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(Sdf3ConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(sdf3Input.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(Sdf3ConfigureException.mainFileFail(mainFile.getPath()));
        }
        final ParseTableConfiguration parseTableConfiguration = new ParseTableConfiguration(
            sdf3Input.createDynamicParseTable(),
            sdf3Input.createDataDependentParseTable(),
            sdf3Input.solveDeepConflictsInParseTable(),
            sdf3Input.checkOverlapInParseTable(),
            sdf3Input.checkPrioritiesInParseTable(),
            sdf3Input.createLayoutSensitiveParseTable()
        );
        return Result.ofOk(new Sdf3SpecConfig(rootDirectory, mainSourceDirectory.getPath(), mainFile.getKey(), parseTableConfiguration));
    }
}
