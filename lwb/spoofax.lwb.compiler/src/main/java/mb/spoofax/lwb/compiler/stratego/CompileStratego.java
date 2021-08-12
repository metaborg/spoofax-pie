package mb.spoofax.lwb.compiler.stratego;

import mb.cfg.metalang.CompileStrategoInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
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

@Value.Enclosing
public class CompileStratego implements TaskDef<ResourcePath, Result<CompileStratego.Output, StrategoCompileException>> {
    @Value.Immutable
    public interface Output extends Serializable {
        class Builder extends CompileStrategoData.Output.Builder {}

        static Builder builder() { return new Builder(); }

        List<ResourcePath> providedJavaFiles();

        List<File> javaClassPaths();

        @Value.Default default KeyedMessages messages() { return KeyedMessages.of(); }
    }

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
    public Result<Output, StrategoCompileException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StrategoCompileException::getLanguageCompilerConfigurationFail)
            .flatMapThrowing(o1 -> Option.ofOptional(o1.compileLanguageInput.compileLanguageSpecificationInput().stratego()).mapThrowingOr(
                i -> context.require(configure, rootDirectory)
                    .mapErr(StrategoCompileException::configureFail)
                    .flatMapThrowing(o2 -> o2.mapThrowingOr(
                        c -> checkAndCompile(context, c, i),
                        Result.ofOk(Output.builder().build())
                    )),
                Result.ofOk(Output.builder().build())
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<Output, StrategoCompileException> checkAndCompile(ExecContext context, StrategoCompileConfig config, CompileStrategoInput input) throws IOException {
        final StrategoAnalyzeConfig analyzeConfig = config.toAnalyzeConfig();
        final KeyedMessages messages = context.require(check, analyzeConfig);
        if(messages.containsError()) {
            return Result.ofErr(StrategoCompileException.checkFail(messages, analyzeConfig));
        }

        final Result<StrategoCompileToJava.Output, MessagesException> compileResult = context.require(compileToJava, config);
        if(compileResult.isErr()) {
            // noinspection ConstantConditions (error is present)
            return Result.ofErr(StrategoCompileException.compileFail(compileResult.getErr(), config));
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
