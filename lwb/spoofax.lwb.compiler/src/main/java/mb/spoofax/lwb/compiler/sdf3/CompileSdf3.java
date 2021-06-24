package mb.spoofax.lwb.compiler.sdf3;

import mb.cfg.metalang.CompileSdf3Input;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.sdf3.task.spec.Sdf3CheckSpec;
import mb.sdf3.task.spec.Sdf3ParseTableToFile;
import mb.sdf3.task.spec.Sdf3SpecConfig;
import mb.sdf3.task.spec.Sdf3SpecToParseTable;
import org.metaborg.sdf2table.parsetable.ParseTable;

import javax.inject.Inject;
import java.io.IOException;

public class CompileSdf3 implements TaskDef<ResourcePath, Result<KeyedMessages, Sdf3CompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final ConfigureSdf3 configure;

    private final Sdf3CheckSpec check;
    private final Sdf3SpecToParseTable toParseTable;
    private final Sdf3ParseTableToFile parseTableToFile;

    @Inject public CompileSdf3(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,

        ConfigureSdf3 configure,

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
    public Result<KeyedMessages, Sdf3CompileException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(Sdf3CompileException::getLanguageCompilerConfigurationFail)
            .flatMapThrowing(o1 -> Option.ofOptional(o1.compileLanguageInput.compileLanguageSpecificationInput().sdf3()).mapThrowingOr(
                i -> context.require(configure, rootDirectory)
                    .mapErr(Sdf3CompileException::configureFail)
                    .flatMapThrowing(o2 -> o2.mapThrowingOr(
                        c -> checkAndCompile(context, c, i),
                        Result.ofOk(KeyedMessages.of())
                    )),
                Result.ofOk(KeyedMessages.of())
            ));
    }

    public Result<KeyedMessages, Sdf3CompileException> checkAndCompile(ExecContext context, Sdf3SpecConfig config, CompileSdf3Input input) throws IOException {
        final KeyedMessages messages = context.require(check, config);
        if(messages.containsError()) {
            return Result.ofErr(Sdf3CompileException.checkFail(messages));
        }

        final Supplier<Result<ParseTable, ?>> parseTableSupplier = toParseTable.createSupplier(new Sdf3SpecToParseTable.Input(config, false));
        final Result<None, ? extends Exception> compileResult = context.require(parseTableToFile, new Sdf3ParseTableToFile.Input(
            parseTableSupplier,
            input.parseTableAtermOutputFile(),
            input.parseTablePersistedOutputFile()
        ));
        if(compileResult.isErr()) {
            return Result.ofErr(Sdf3CompileException.parseTableCompileFail(compileResult.getErr()));
        }

        return Result.ofOk(messages);
    }
}
