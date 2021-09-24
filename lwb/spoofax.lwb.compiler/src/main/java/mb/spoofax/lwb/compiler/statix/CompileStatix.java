package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixCheckMulti;
import mb.statix.task.StatixCompileModule;
import mb.statix.task.StatixCompileProject;
import mb.statix.task.StatixConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

public class CompileStatix implements TaskDef<ResourcePath, Result<KeyedMessages, StatixCompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final ConfigureStatix configure;

    private final StatixCheckMulti check;
    private final StatixCompileProject compileProject;

    @Inject public CompileStatix(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        ConfigureStatix configure,
        StatixCheckMulti check,
        StatixCompileProject compileProject
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.configure = configure;
        this.check = check;
        this.compileProject = compileProject;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StatixCompileException> exec(ExecContext context, ResourcePath rootDirectory) throws Exception {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(StatixCompileException::getLanguageCompilerConfigurationFail)
            .flatMapThrowing(o1 -> Option.ofOptional(o1.compileLanguageInput.compileLanguageSpecificationInput().statix()).mapThrowingOr(
                i -> context.require(configure, rootDirectory)
                    .mapErr(StatixCompileException::configureFail)
                    .flatMapThrowing(o2 -> o2.mapThrowingOr(
                        c -> checkAndCompile(context, c, i),
                        Result.ofOk(KeyedMessages.of())
                    )),
                Result.ofOk(KeyedMessages.of())
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<KeyedMessages, StatixCompileException> checkAndCompile(ExecContext context, StatixConfig config, CompileStatixInput input) throws IOException {
        final KeyedMessages messages = context.require(check, input.rootDirectory());
        if(messages.containsError()) {
            return Result.ofErr(StatixCompileException.checkFail(messages));
        }

        return context.require(compileProject, new StatixCompileProject.Input(input.rootDirectory(), config.sourceFileOrigins))
            .mapThrowing(o -> {
                writeOutput(context, o, input.outputDirectory());
                return messages;
            }).mapErr(StatixCompileException::compileFail);
    }

    private void writeOutput(ExecContext context, StatixCompileProject.Output output, ResourcePath outputPath) throws IOException {
        final HierarchicalResource outputDirectory = context.getHierarchicalResource(outputPath).ensureDirectoryExists();
        for(StatixCompileModule.Output out : output.compileModuleOutputs) {
            final HierarchicalResource outputFile = outputDirectory.appendAsRelativePath(out.relativeOutputPath).ensureFileExists();
            outputFile.writeString(out.spec.toString());
            context.provide(outputFile);
        }
    }
}
