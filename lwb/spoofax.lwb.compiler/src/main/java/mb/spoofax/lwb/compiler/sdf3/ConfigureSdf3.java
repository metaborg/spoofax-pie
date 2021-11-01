package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import org.metaborg.sdf2table.parsetable.ParseTableConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

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
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, new Sdf3ConfigMapping())
            .mapErr(Sdf3ConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<Sdf3SpecConfig>, IOException>flatMapThrowing(o -> Result.transpose(o.mapThrowing(sdf3Input -> toSdf3SpecConfig(context, rootDirectory, sdf3Input))));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
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

    private static class Sdf3ConfigMapping extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<CompileSdf3Input>, CfgRootDirectoryToObjectException>> {
        @Override
        public Result<Option<CompileSdf3Input>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().sdf3()));
        }
    }
}
