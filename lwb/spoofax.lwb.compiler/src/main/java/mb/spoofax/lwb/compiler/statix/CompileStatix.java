package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CompileStatixInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.task.StatixCheckMulti;
import mb.statix.task.StatixCompileModule;
import mb.statix.task.StatixCompileProject;
import mb.statix.task.StatixConfig;
import mb.spoofax.lwb.compiler.CompileLanguageSpecification;
import mb.statix.task.StatixCheckMulti;
import mb.statix.task.StatixCompileAndMergeProject;
import mb.statix.task.StatixCompileModule;
import mb.statix.task.StatixCompileProject;
import mb.statix.task.StatixConfig;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompileStatix implements TaskDef<ResourcePath, Result<KeyedMessages, StatixCompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final ConfigureStatix configure;

    private final StatixCheckMulti check;
    private final StatixCompileProject compileProject;
    private final StatixCompileAndMergeProject compileAndMergeProject;

    @Inject public CompileStatix(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        ConfigureStatix configure,
        StatixCheckMulti check,
        StatixCompileProject compileProject,
        StatixCompileAndMergeProject compileAndMergeProject
    ) {
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.configure = configure;
        this.check = check;
        this.compileProject = compileProject;
        this.compileAndMergeProject = compileAndMergeProject;
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

        final HierarchicalResource outputDirectory = context.getHierarchicalResource(input.outputDirectory()).ensureDirectoryExists();
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final Result<KeyedMessages, StatixCompileException> result = context.require(compileProject, input.rootDirectory())
            .mapThrowing(o -> {
                writeAllOutputs(context, o, outputDirectory);
                return messages;
            })
            .ifOk(messagesBuilder::addMessages)
            .mapErr(StatixCompileException::compileFail)
            .and(
                context.require(compileAndMergeProject, input.rootDirectory())
                    .mapThrowing(o -> {
                        final HierarchicalResource mergedOutputFile = outputDirectory.appendAsRelativePath("src-gen/statix/statix.merged.aterm").ensureFileExists();
                        writeOutput(context, o, mergedOutputFile);
                        return messages;
                    })
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(StatixCompileException::compileFail)
            );
        if(result.isErr()) return result.ignoreValueIfErr();
        return Result.ofOk(messagesBuilder.build());
    }

    /**
     * Writes all compiled Statix modules to files.
     *
     * @param context the execution context
     * @param compileModuleOutputs the compiled Statix modules
     * @param outputDirectory the path to write the files to
     * @throws IOException if an I/O exception occurred
     */
    private void writeAllOutputs(ExecContext context, ListView<StatixCompileModule.Output> compileModuleOutputs, HierarchicalResource outputDirectory) throws IOException {
        for(StatixCompileModule.Output output : compileModuleOutputs) {
            final HierarchicalResource outputFile = outputDirectory.appendAsRelativePath(output.relativeOutputPath).ensureFileExists();
            writeOutput(context, output.spec, outputFile);
        }
    }

    /**
     * Writes an ATerm to a file.
     *
     * @param context the execution context
     * @param ast the AST to write
     * @param outputFile the file to write to
     * @throws IOException if an I/O exception occurred
     */
    private void writeOutput(ExecContext context, IStrategoTerm ast, HierarchicalResource outputFile) throws IOException {
        outputFile.writeString(ast.toString());
        context.provide(outputFile);
    }
}
