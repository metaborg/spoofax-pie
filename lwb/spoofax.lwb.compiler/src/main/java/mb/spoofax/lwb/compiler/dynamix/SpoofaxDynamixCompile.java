package mb.spoofax.lwb.compiler.dynamix;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.dynamix.task.DynamixCheckMulti;
import mb.dynamix.task.DynamixCompileAndMergeProject;
import mb.dynamix.task.DynamixCompileModule;
import mb.dynamix.task.DynamixCompileProject;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.spoofax.lwb.compiler.util.TaskCopyUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

/**
 * Compilation task for Dynamix in the context of the Spoofax LWB compiler.
 */
public class SpoofaxDynamixCompile implements TaskDef<ResourcePath, Result<KeyedMessages, SpoofaxDynamixCompileException>> {
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    private final SpoofaxDynamixConfigure configure;

    private final DynamixCheckMulti check;
    private final DynamixCompileProject compileProject;
    private final DynamixCompileAndMergeProject compileAndMergeProject;

    @Inject public SpoofaxDynamixCompile(
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        SpoofaxDynamixConfigure configure,
        DynamixCheckMulti check,
        DynamixCompileProject compileProject,
        DynamixCompileAndMergeProject compileAndMergeProject
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
    public Result<KeyedMessages, SpoofaxDynamixCompileException> exec(ExecContext context, ResourcePath rootDirectory) throws IOException {
        return context.require(configure, rootDirectory)
            .mapErr(SpoofaxDynamixCompileException::configureFail)
            .flatMapThrowing(o -> o.mapThrowingOr(
                c -> compile(context, rootDirectory, c),
                Result.ofOk(KeyedMessages.of()) // Dynamix is not configured, nothing to do.
            ));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public Result<KeyedMessages, SpoofaxDynamixCompileException> compile(
        ExecContext context,
        ResourcePath rootDirectory,
        SpoofaxDynamixConfig config
    ) throws IOException {
        try {
            return config.caseOf()
                .files((dynamixConfig, outputSpecAtermDirectory) -> compileFromSourceFilesCatching(context, rootDirectory, outputSpecAtermDirectory))
                .prebuilt((inputSpecAtermDirectory, outputSpecAtermDirectory) -> copyPrebuilt(context, inputSpecAtermDirectory, outputSpecAtermDirectory))
                ;
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    public Result<KeyedMessages, SpoofaxDynamixCompileException> compileFromSourceFilesCatching(
        ExecContext context,
        ResourcePath rootDirectory,
        ResourcePath outputSpecAtermDirectoryPath
    ) {
        try {
            return compileFromSourceFiles(context, rootDirectory, outputSpecAtermDirectoryPath);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Result<KeyedMessages, SpoofaxDynamixCompileException> compileFromSourceFiles(
        ExecContext context,
        ResourcePath rootDirectory,
        ResourcePath outputSpecAtermDirectoryPath
    ) throws IOException {
        final KeyedMessages messages = context.require(check, rootDirectory);
        if(messages.containsError()) {
            return Result.ofErr(SpoofaxDynamixCompileException.checkFail(messages));
        }

        final HierarchicalResource outputDirectory = context.getHierarchicalResource(outputSpecAtermDirectoryPath).ensureDirectoryExists();
        final KeyedMessagesBuilder messagesBuilder = new KeyedMessagesBuilder();
        final Result<KeyedMessages, SpoofaxDynamixCompileException> result = context.require(compileProject, rootDirectory)
            .mapThrowing(o -> {
                writeAllOutputs(context, o, outputDirectory);
                return messages;
            })
            .ifOk(messagesBuilder::addMessages)
            .mapErr(SpoofaxDynamixCompileException::compileFail)
            .and(
                context.require(compileAndMergeProject, rootDirectory)
                    .mapThrowing(o -> {
                        final HierarchicalResource mergedOutputFile = outputDirectory.appendAsRelativePath("src-gen/dynamix/dynamix.merged.aterm").ensureFileExists();
                        writeOutput(context, o, mergedOutputFile);
                        return messages;
                    })
                    .ifOk(messagesBuilder::addMessages)
                    .mapErr(SpoofaxDynamixCompileException::compileFail)
            );
        if(result.isErr()) return result.ignoreValueIfErr();
        return Result.ofOk(messagesBuilder.build());
    }

    public Result<KeyedMessages, SpoofaxDynamixCompileException> copyPrebuilt(
        ExecContext context,
        ResourcePath inputSpecAtermDirectoryPath,
        ResourcePath outputSpecAtermDirectoryPath
    ) {
        try {
            TaskCopyUtil.copyAll(context, inputSpecAtermDirectoryPath, outputSpecAtermDirectoryPath, ResourceMatcher.ofPath(PathMatcher.ofExtension("aterm")));
        } catch(IOException e) {
            return Result.ofErr(SpoofaxDynamixCompileException.compileFail(e));
        }
        return Result.ofOk(KeyedMessages.of());
    }

    /**
     * Writes all compiled Dynamix modules to files.
     *
     * @param context              the execution context
     * @param compileModuleOutputs the compiled Dynamix modules
     * @param outputDirectory      the path to write the files to
     * @throws IOException if an I/O exception occurred
     */
    private void writeAllOutputs(ExecContext context, ListView<DynamixCompileModule.Output> compileModuleOutputs, HierarchicalResource outputDirectory) throws IOException {
        for(DynamixCompileModule.Output output : compileModuleOutputs) {
            final HierarchicalResource outputFile = outputDirectory.appendAsRelativePath(output.relativeOutputPath).ensureFileExists();
            writeOutput(context, output.spec, outputFile);
        }
    }

    /**
     * Writes an ATerm to a file.
     *
     * @param context    the execution context
     * @param ast        the AST to write
     * @param outputFile the file to write to
     * @throws IOException if an I/O exception occurred
     */
    private void writeOutput(ExecContext context, IStrategoTerm ast, HierarchicalResource outputFile) throws IOException {
        outputFile.writeString(ast.toString());
        context.provide(outputFile);
    }
}
