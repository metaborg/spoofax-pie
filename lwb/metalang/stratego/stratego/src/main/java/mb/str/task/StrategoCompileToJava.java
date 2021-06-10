package mb.str.task;

import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.str.StrategoScope;
import mb.str.config.StrategoCompileConfig;
import mb.str.incr.MessageConverter;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;

import javax.inject.Inject;
import java.util.ArrayList;

@StrategoScope
public class StrategoCompileToJava implements TaskDef<StrategoCompileConfig, Result<CompileOutput.Success, MessagesException>> {
    private final ResourceService resourceService;
    private final Compile compile;

    @Inject public StrategoCompileToJava(ResourceService resourceService, Compile compile) {
        this.resourceService = resourceService;
        this.compile = compile;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<CompileOutput.Success, MessagesException> exec(ExecContext context, StrategoCompileConfig config) {
        final CompileOutput output = context.require(compile, new CompileInput(
            config.mainModule,
            config.rootDirectory,
            config.outputDir,
            config.outputJavaPackageId,
            config.cacheDir,
            new ArrayList<>(),
            config.includeDirs.asCopy(),
            config.builtinLibs.asCopy(),
            config.extraCompilerArguments,
            config.sourceFileOrigins.asCopy(),
            config.gradualTypingSetting,
            true,
            true
        ));
        if(output instanceof CompileOutput.Failure) {
            final CompileOutput.Failure failure = (CompileOutput.Failure)output;
            return Result.ofErr(new MessagesException(MessageConverter.convertMessages(config.rootDirectory, failure.messages, resourceService), "Stratego compilation failed"));
        }
        final CompileOutput.Success success = (CompileOutput.Success)output;
        return Result.ofOk(success);
    }
}
