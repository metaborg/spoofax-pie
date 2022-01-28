package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3CheckSpec;
import mb.sdf3.task.spec.Sdf3Config;
import mb.sdf3.task.spec.Sdf3ParseTableToFile;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import mb.spoofax.lwb.compiler.util.TaskCopyUtil;
import org.metaborg.sdf2table.parsetable.ParseTable;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

/**
 * Compile task for SDF3 in the context of the Spoofax LWB compiler.
 */
public class SpoofaxSdf3Compile implements TaskDef<ResourcePath, Result<KeyedMessages, SpoofaxSdf3CompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final SpoofaxSdf3Configure configure;

    private final Sdf3CheckSpec check;
    private final Sdf3SpecToParseTable toParseTable;
    private final Sdf3ParseTableToFile parseTableToFile;

    @Inject public SpoofaxSdf3Compile(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,

        SpoofaxSdf3Configure configure,

        Sdf3CheckSpec check,
        Sdf3SpecToParseTable toParseTable,
        Sdf3ParseTableToFile parseTableToFile
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;

        this.configure = configure;

        this.check = check;
        this.toParseTable = toParseTable;
        this.parseTableToFile = parseTableToFile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, SpoofaxSdf3CompileException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(configure, rootDirectory)
            .mapErr(SpoofaxSdf3CompileException::configureFail)
            .flatMap(o -> o.mapOr(
                c -> compile(context, c),
                Result.ofOk(KeyedMessages.of()) // SDF3 is not configured, nothing to do.
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<KeyedMessages, SpoofaxSdf3CompileException> compile(ExecContext context, SpoofaxSdf3Config config) {
        return config.caseOf()
            .files((sdf3SpecConfig, sdf3Config, outputParseTableAtermFile, outputParseTablePersistedFile) -> compileFromSourceFiles(context, sdf3SpecConfig, outputParseTableAtermFile, outputParseTablePersistedFile))
            .prebuilt((inputParseTableAtermFile, inputParseTablePersistedFile, outputParseTableAtermFile, outputParseTablePersistedFile) -> copyPrebuilt(context, inputParseTableAtermFile, inputParseTablePersistedFile, outputParseTableAtermFile, outputParseTablePersistedFile))
            ;
    }

    public Result<KeyedMessages, SpoofaxSdf3CompileException> compileFromSourceFiles(
        ExecContext context,
        Sdf3SpecConfig config,
        ResourcePath outputParseTableAtermFile,
        ResourcePath outputParseTablePersistedFile
    ) {
        final KeyedMessages messages = context.require(check, config);
        if(messages.containsError()) {
            return Result.ofErr(SpoofaxSdf3CompileException.checkFail(messages));
        }

        final Supplier<Result<ParseTable, ?>> parseTableSupplier = toParseTable.createSupplier(new Sdf3SpecToParseTable.Input(config, false));
        final Result<None, ? extends Exception> compileResult = context.require(parseTableToFile, new Sdf3ParseTableToFile.Input(
            parseTableSupplier,
            outputParseTableAtermFile,
            outputParseTablePersistedFile
        ));
        if(compileResult.isErr()) {
            return Result.ofErr(SpoofaxSdf3CompileException.parseTableCompileFail(compileResult.getErr()));
        }

        return Result.ofOk(messages);
    }

    public Result<KeyedMessages, SpoofaxSdf3CompileException> copyPrebuilt(
        ExecContext context,
        ResourcePath inputParseTableAtermFilePath,
        ResourcePath inputParseTablePersistedFilePath,
        ResourcePath outputParseTableAtermFilePath,
        ResourcePath outputParseTablePersistedFilePath
    ) {
        try {
            TaskCopyUtil.copy(context, inputParseTableAtermFilePath, outputParseTableAtermFilePath);
            TaskCopyUtil.copy(context, inputParseTablePersistedFilePath, outputParseTablePersistedFilePath);
        } catch(IOException e) {
            return Result.ofErr(SpoofaxSdf3CompileException.parseTableCompileFail(e));
        }
        return Result.ofOk(KeyedMessages.of());
    }
}
