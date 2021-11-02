package mb.spoofax.lwb.compiler.stratego;

import mb.common.message.KeyedMessages;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.str.config.StrategoAnalyzeConfig;
import mb.str.config.StrategoCompileConfig;
import mb.str.task.StrategoCheck;
import mb.str.task.StrategoCompileToJava;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Check task for Stratego in the context of the Spoofax LWB compiler.
 */
@Value.Enclosing
public class SpoofaxStrategoCompile implements TaskDef<ResourcePath, Result<SpoofaxStrategoCompile.Output, SpoofaxStrategoCompileException>> {
    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends SpoofaxStrategoCompileData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> providedJavaFiles();

        List<File> javaClassPaths();

        @Value.Default default KeyedMessages messages() { return KeyedMessages.of(); }
    }

    private final SpoofaxStrategoConfigure configure;

    private final StrategoCheck check;
    private final StrategoCompileToJava compileToJava;


    @Inject public SpoofaxStrategoCompile(
        SpoofaxStrategoConfigure configure,

        StrategoCheck check,
        StrategoCompileToJava compileToJava
    ) {
        this.configure = configure;
        this.check = check;
        this.compileToJava = compileToJava;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<Output, SpoofaxStrategoCompileException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(configure, rootDirectory)
            .mapErr(SpoofaxStrategoCompileException::configureFail)
            .flatMapThrowing(o -> o.mapThrowingOr(
                c -> compile(context, c),
                Result.ofOk(Output.builder().build()) // Stratego is not configured, nothing to do.
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<Output, SpoofaxStrategoCompileException> compile(ExecContext context, StrategoCompileConfig config) throws IOException {
        final StrategoAnalyzeConfig analyzeConfig = config.toAnalyzeConfig();
        final KeyedMessages messages = context.require(check, analyzeConfig);
        if(messages.containsError()) {
            return Result.ofErr(SpoofaxStrategoCompileException.checkFail(messages, analyzeConfig));
        }

        final Result<StrategoCompileToJava.Output, MessagesException> compileResult = context.require(compileToJava, config);
        if(compileResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(SpoofaxStrategoCompileException.compileFail(compileResult.getErr(), config));
        } else {
            // noinspection ConstantConditions (value is present)
            final StrategoCompileToJava.Output output = compileResult.get();
            // noinspection ConstantConditions (value is really really present)
            return Result.ofOk(Output.builder()
                .providedJavaFiles(output.javaSourceFiles)
                .addAllJavaClassPaths(config.javaClassPaths)
                .messages(output.messages)
                .build()
            );
        }
    }
}
