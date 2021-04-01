package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.metalang.CompileStrategoInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import mb.str.task.StrategoCheck;
import mb.str.task.StrategoCompileToJava;

import javax.inject.Inject;
import java.io.IOException;

public class CompileStratego implements TaskDef<ResourcePath, Result<KeyedMessages, StrategoCompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final ConfigureStratego configure;

    private final StrategoCheck check;
    private final StrategoCompileToJava compileToJava;


    @Inject public CompileStratego(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,

        ConfigureStratego configure,

        StrategoCheck check,
        StrategoCompileToJava compileToJava
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.configure = configure;
        this.check = check;
        this.compileToJava = compileToJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StrategoCompileException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StrategoCompileException::getLanguageCompilerConfigurationFail)
            .flatMapThrowing(o1 -> Option.ofOptional(o1.compileLanguageInput.compileLanguageSpecificationInput().stratego()).mapThrowingOr(
                i -> context.require(configure, rootDirectory)
                    .mapErr(StrategoCompileException::configureFail)
                    .flatMapThrowing(o2 -> o2.mapThrowingOr(
                        c -> checkAndCompile(context, c, i),
                        Result.ofOk(KeyedMessages.of())
                    )),
                Result.ofOk(KeyedMessages.of())
            ));
    }

    public Result<KeyedMessages, StrategoCompileException> checkAndCompile(ExecContext context, StrategoCompileConfig config, CompileStrategoInput input) throws IOException {
        final StrategoAnalyzeConfig analyzeConfig = config.toAnalyzeConfig();
        final KeyedMessages messages = context.require(check, analyzeConfig);
        if(messages.containsError()) {
            return Result.ofErr(StrategoCompileException.checkFail(messages, analyzeConfig));
        }

        final Result<None, ?> compileResult = context.require(compileToJava, config);
        if(compileResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(StrategoCompileException.compileFail(compileResult.getErr(), config));
        }

        return Result.ofOk(messages);
    }
}
