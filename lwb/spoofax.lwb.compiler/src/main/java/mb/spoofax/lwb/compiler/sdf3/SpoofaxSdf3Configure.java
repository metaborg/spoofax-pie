package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.metalang.CfgSdf3Config;
import mb.cfg.metalang.CfgSdf3Source;
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
import java.io.UncheckedIOException;
import java.util.Set;

/**
 * Configuration task for SDF3 in the context of the Spoofax LWB compiler.
 */
public class SpoofaxSdf3Configure implements TaskDef<ResourcePath, Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    @Inject public SpoofaxSdf3Configure(
        CfgRootDirectoryToObject cfgRootDirectoryToObject
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Option<SpoofaxSdf3Config>, SpoofaxSdf3ConfigureException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.requireMapping(cfgRootDirectoryToObject, rootDirectory, new Sdf3ConfigMapping())
            .mapErr(SpoofaxSdf3ConfigureException::getLanguageCompilerConfigurationFail)
            .<Option<SpoofaxSdf3Config>, IOException>flatMapThrowing(o -> Result.transpose(o.mapThrowing(sdf3Input -> configure(context, rootDirectory, sdf3Input))));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<SpoofaxSdf3Config, SpoofaxSdf3ConfigureException> configure(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgSdf3Config cfgSdf3Config
    ) throws IOException {
        try {
            return cfgSdf3Config.source().caseOf()
                .files(files -> configureSourceFilesCatching(context, rootDirectory, cfgSdf3Config, files))
                .prebuilt((inputParseTableAtermFile, inputParseTablePersistedFile, inputCompletionParseTableAtermFile, inputCompletionParseTablePersistedFile) -> configurePrebuilt(
                    inputParseTableAtermFile, inputParseTablePersistedFile, inputCompletionParseTableAtermFile, inputCompletionParseTablePersistedFile, cfgSdf3Config));
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public Result<SpoofaxSdf3Config, SpoofaxSdf3ConfigureException> configureSourceFilesCatching(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgSdf3Config cfgSdf3Config,
        CfgSdf3Source.Files files
    ) {
        try {
            return configureSourceFiles(context, rootDirectory, cfgSdf3Config, files);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Result<SpoofaxSdf3Config, SpoofaxSdf3ConfigureException> configureSourceFiles(
        ExecContext context,
        ResourcePath rootDirectory,
        CfgSdf3Config cfgSdf3Config,
        CfgSdf3Source.Files files
    ) throws IOException {
        final HierarchicalResource mainSourceDirectory = context.require(files.mainSourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainSourceDirectory.exists() || !mainSourceDirectory.isDirectory()) {
            return Result.ofErr(SpoofaxSdf3ConfigureException.mainSourceDirectoryFail(mainSourceDirectory.getPath()));
        }
        final HierarchicalResource mainFile = context.require(files.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(SpoofaxSdf3ConfigureException.mainFileFail(mainFile.getPath()));
        }
        final ParseTableConfiguration parseTableConfiguration = new ParseTableConfiguration(
            cfgSdf3Config.createDynamicParseTable(),
            cfgSdf3Config.createDataDependentParseTable(),
            cfgSdf3Config.solveDeepConflictsInParseTable(),
            cfgSdf3Config.checkOverlapInParseTable(),
            cfgSdf3Config.checkPrioritiesInParseTable(),
            cfgSdf3Config.createLayoutSensitiveParseTable()
        );
        final Sdf3SpecConfig sdf3SpecConfig = new Sdf3SpecConfig(rootDirectory, mainSourceDirectory.getPath(), mainFile.getPath(), parseTableConfiguration);
        return Result.ofOk(SpoofaxSdf3Config.files(
            sdf3SpecConfig,
            cfgSdf3Config.parseTableAtermOutputFile(),
            cfgSdf3Config.parseTablePersistedOutputFile(),
            cfgSdf3Config.completionParseTableAtermOutputFile(),
            cfgSdf3Config.completionParseTablePersistedOutputFile()
        ));
    }

    public Result<SpoofaxSdf3Config, SpoofaxSdf3ConfigureException> configurePrebuilt(
        ResourcePath inputParseTableAtermFile,
        ResourcePath inputParseTablePersistedFile,
        ResourcePath inputCompletionParseTableAtermFile,
        ResourcePath inputCompletionParseTablePersistedFile,
        CfgSdf3Config cfgSdf3Config
    ) {
        return Result.ofOk(SpoofaxSdf3Config.prebuilt(
            inputParseTableAtermFile,
            inputParseTablePersistedFile,
            inputCompletionParseTableAtermFile,
            inputCompletionParseTablePersistedFile,
            cfgSdf3Config.parseTableAtermOutputFile(),
            cfgSdf3Config.parseTablePersistedOutputFile(),
            cfgSdf3Config.completionParseTableAtermOutputFile(),
            cfgSdf3Config.completionParseTablePersistedOutputFile()
        ));
    }

    private static class Sdf3ConfigMapping extends StatelessSerializableFunction<Result<CfgToObject.Output, CfgRootDirectoryToObjectException>, Result<Option<CfgSdf3Config>, CfgRootDirectoryToObjectException>> {
        @Override
        public Result<Option<CfgSdf3Config>, CfgRootDirectoryToObjectException> apply(Result<CfgToObject.Output, CfgRootDirectoryToObjectException> result) {
            return result.map(o -> Option.ofOptional(o.compileLanguageInput.compileLanguageSpecificationInput().sdf3()));
        }
    }
}
