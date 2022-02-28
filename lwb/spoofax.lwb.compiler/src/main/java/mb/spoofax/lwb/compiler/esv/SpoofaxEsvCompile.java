package mb.spoofax.lwb.compiler.esv;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.esv.task.EsvCheck;
import mb.esv.task.EsvCompile;
import mb.esv.task.EsvConfig;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.WritableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.lwb.compiler.util.TaskCopyUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

/**
 * Compile task for ESV in the context of the Spoofax LWB compiler.
 */
public class SpoofaxEsvCompile implements TaskDef<ResourcePath, Result<KeyedMessages, SpoofaxEsvCompileException>> {
    private final SpoofaxEsvConfigure configure;
    private final EsvCheck check;
    private final EsvCompile compile;


    @Inject public SpoofaxEsvCompile(
        SpoofaxEsvConfigure configure,
        EsvCheck check,
        EsvCompile compile
    ) {
        this.configure = configure;
        this.check = check;
        this.compile = compile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, SpoofaxEsvCompileException> exec(ExecContext context, ResourcePath rootDirectory) {
        return context.require(configure, rootDirectory)
            .mapErr(SpoofaxEsvCompileException::configureFail)
            .flatMap(o -> o.mapOr(
                c -> compile(context, c),
                Result.ofOk(KeyedMessages.of()) // ESV is not configured, nothing to do.
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<KeyedMessages, SpoofaxEsvCompileException> compile(ExecContext context, SpoofaxEsvConfig config) {
        return config.caseOf()
            .files((esvConfig, outputFile) -> compileFromSourceFiles(context, esvConfig, outputFile))
            .prebuilt((inputFile, outputFile) -> copyPrebuilt(context, inputFile, outputFile))
            ;
    }

    public Result<KeyedMessages, SpoofaxEsvCompileException> compileFromSourceFiles(
        ExecContext context,
        EsvConfig config,
        ResourcePath atermFormatOutputFile
    ) {
        final KeyedMessages messages = context.require(check, config);
        if(messages.containsError()) {
            return Result.ofErr(SpoofaxEsvCompileException.checkFail(messages));
        }

        final Result<IStrategoTerm, ?> result = context.require(compile, config);
        if(result.isErr()) {
            return Result.ofErr(SpoofaxEsvCompileException.compileFail(result.unwrapErr()));
        }
        final IStrategoTerm atermFormat = result.get();
        final WritableResource atermFormatFile = context.getWritableResource(atermFormatOutputFile);
        try {
            atermFormatFile.writeString(atermFormat.toString());
            context.provide(atermFormatFile);
        } catch(IOException e) {
            return Result.ofErr(SpoofaxEsvCompileException.compileFail(e));
        }

        return Result.ofOk(messages);
    }

    public Result<KeyedMessages, SpoofaxEsvCompileException> copyPrebuilt(
        ExecContext context,
        ResourcePath inputFile,
        ResourcePath outputFile
    ) {
        try {
            TaskCopyUtil.copy(context, inputFile, outputFile);
        } catch(IOException e) {
            return Result.ofErr(SpoofaxEsvCompileException.compileFail(e));
        }
        return Result.ofOk(KeyedMessages.of());
    }
}
