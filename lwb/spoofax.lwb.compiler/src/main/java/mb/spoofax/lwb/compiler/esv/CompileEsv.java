package mb.spoofax.lwb.compiler.esv;

import mb.cfg.metalang.CompileEsvInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.esv.task.EsvCheck;
import mb.esv.task.EsvCompile;
import mb.esv.task.EsvConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.WritableResource;
import mb.resource.hierarchical.ResourcePath;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;

public class CompileEsv implements TaskDef<ResourcePath, Result<KeyedMessages, EsvCompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final ConfigureEsv configure;
    private final EsvCheck check;
    private final EsvCompile compile;


    @Inject public CompileEsv(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        ConfigureEsv configure,
        EsvCheck check,
        EsvCompile compile
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;

        this.configure = configure;
        this.check = check;
        this.compile = compile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, EsvCompileException> exec(ExecContext context, ResourcePath rootDirectory) {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(EsvCompileException::getLanguageCompilerConfigurationFail)
            .flatMap(o1 -> Option.ofOptional(o1.compileLanguageInput.compileLanguageSpecificationInput().esv()).mapOr(
                i -> context.require(configure, rootDirectory)
                    .mapErr(EsvCompileException::configureFail)
                    .flatMap(o2 -> o2.mapOr(
                        c -> checkAndCompile(context, c, i),
                        Result.ofOk(KeyedMessages.of())
                    )),
                Result.ofOk(KeyedMessages.of())
            ));
    }

    public Result<KeyedMessages, EsvCompileException> checkAndCompile(ExecContext context, EsvConfig config, CompileEsvInput input) {
        final KeyedMessages messages = context.require(check, config);
        if(messages.containsError()) {
            return Result.ofErr(EsvCompileException.checkFail(messages));
        }

        final Result<IStrategoTerm, ?> result = context.require(compile, config);
        if(result.isErr()) {
            return Result.ofErr(EsvCompileException.compileFail(result.unwrapErr()));
        }
        final IStrategoTerm atermFormat = result.get();
        final WritableResource atermFormatFile = context.getWritableResource(input.atermFormatOutputFile());
        try {
            atermFormatFile.writeString(atermFormat.toString());
            context.provide(atermFormatFile);
        } catch(IOException e) {
            return Result.ofErr(EsvCompileException.compileFail(e));
        }

        return Result.ofOk(messages);
    }
}
