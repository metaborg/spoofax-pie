package mb.spoofax.lwb.compiler.statix;

import mb.cfg.metalang.CompileStatixInput;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.statix.task.StatixCheckMulti;
import mb.statix.task.StatixCompile;
import mb.statix.util.StatixUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.stream.Stream;

public class CompileStatix implements TaskDef<CompileStatixInput, Result<KeyedMessages, StatixCompileException>> {
    private final StatixCheckMulti check;
    private final StatixCompile compile;

    @Inject public CompileStatix(
        StatixCheckMulti check,
        StatixCompile compile
    ) {
        this.check = check;
        this.compile = compile;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<KeyedMessages, StatixCompileException> exec(ExecContext context, CompileStatixInput input) throws Exception {
        // Check main file, root directory, and include directories.
        final HierarchicalResource mainFile = context.require(input.mainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StatixCompileException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource sourceDirectory = context.require(input.sourceDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            return Result.ofErr(StatixCompileException.sourceDirectoryFail(sourceDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.includeDirectories()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(StatixCompileException.includeDirectoryFail(includeDirectoryPath));
            }
        }
        final ResourcePath rootDirectory = input.rootDirectory();

        // Check Statix source files.
        final ResourceWalker resourceWalker = StatixUtil.createResourceWalker();
        final ResourceMatcher resourceMatcher = new AllResourceMatcher(StatixUtil.createResourceMatcher(), new FileResourceMatcher());
        // TODO: this does not check Statix files in include directories.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new StatixCheckMulti.Input(rootDirectory, resourceWalker, resourceMatcher) // HACK: Run check on root directory so it can pick up the CFG file.
        ));
        if(messages.containsError()) {
            return Result.ofErr(StatixCompileException.checkFail(messages));
        }

        final HierarchicalResource outputDirectory = context.getHierarchicalResource(input.statixOutputDirectory()).ensureDirectoryExists();
        try(final Stream<? extends HierarchicalResource> stream = sourceDirectory.walk(resourceWalker, resourceMatcher)) {
            for(HierarchicalResource inputFile : new StreamIterable<>(stream)) {
                // HACK: Run compile on root directory so it can pick up the CFG file.
                final Result<StatixCompile.Output, ?> result = context.require(compile, new StatixCompile.Input(rootDirectory, inputFile.getPath()));
                if(result.isErr()) {
                    return Result.ofErr(StatixCompileException.compilerFail(result.unwrapErr()));
                }
                final StatixCompile.Output output = result.unwrapUnchecked();
                final HierarchicalResource outputFile = outputDirectory.appendAsRelativePath(output.relativeOutputPath).ensureFileExists();
                outputFile.writeString(output.spec.toString());
                context.provide(outputFile);
            }
        }

        return Result.ofOk(messages);
    }
}
