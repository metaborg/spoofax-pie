package mb.spoofax.lwb.compiler.metalang;

import mb.common.message.HasOptionalMessages;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ADT;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofx.lwb.compiler.cfg.metalang.CompileStatixInput;
import mb.statix.task.StatixCheckMulti;
import mb.statix.task.StatixCompile;
import mb.statix.util.StatixUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value;

import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

@Value.Enclosing
public class CompileStatix implements TaskDef<CompileStatixInput, Result<KeyedMessages, CompileStatix.StatixCompileException>> {
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
        final HierarchicalResource mainFile = context.require(input.statixMainFile(), ResourceStampers.<HierarchicalResource>exists());
        if(!mainFile.exists() || !mainFile.isFile()) {
            return Result.ofErr(StatixCompileException.mainFileFail(mainFile.getPath()));
        }
        final HierarchicalResource rootDirectory = context.require(input.statixRootDirectory(), ResourceStampers.<HierarchicalResource>exists());
        if(!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            return Result.ofErr(StatixCompileException.rootDirectoryFail(rootDirectory.getPath()));
        }
        for(ResourcePath includeDirectoryPath : input.statixIncludeDirs()) {
            final HierarchicalResource includeDirectory = context.require(includeDirectoryPath, ResourceStampers.<HierarchicalResource>exists());
            if(!includeDirectory.exists() || !includeDirectory.isDirectory()) {
                return Result.ofErr(StatixCompileException.includeDirectoryFail(includeDirectoryPath));
            }
        }

        // Check Statix source files.
        final ResourceWalker resourceWalker = StatixUtil.createResourceWalker();
        final ResourceMatcher resourceMatcher = new AllResourceMatcher(StatixUtil.createResourceMatcher(), new FileResourceMatcher());
        // TODO: this does not check Statix files in include directories.
        final @Nullable KeyedMessages messages = context.require(check.createTask(
            new StatixCheckMulti.Input(rootDirectory.getPath(), resourceWalker, resourceMatcher)
        ));
        if(messages.containsError()) {
            return Result.ofErr(StatixCompileException.checkFail(messages));
        }

        final HierarchicalResource outputDirectory = context.getHierarchicalResource(input.statixOutputDirectory()).ensureDirectoryExists();
        try(final Stream<? extends HierarchicalResource> stream = rootDirectory.walk(resourceWalker, resourceMatcher)) {
            for(HierarchicalResource inputFile : new StreamIterable<>(stream)) {
                final Result<StatixCompile.Output, ?> result = context.require(compile, new StatixCompile.Input(rootDirectory.getPath(), inputFile.getPath()));
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


    @ADT
    public abstract static class StatixCompileException extends Exception implements HasOptionalMessages {
        public interface Cases<R> {
            R mainFileFail(ResourceKey mainFile);

            R includeDirectoryFail(ResourcePath includeDirectory);

            R rootDirectoryFail(ResourcePath rootDirectory);

            R checkFail(KeyedMessages messages);

            R compilerFail(Exception cause);
        }

        public static StatixCompileException mainFileFail(ResourceKey mainFile) {
            return StatixCompileExceptions.mainFileFail(mainFile);
        }

        public static StatixCompileException includeDirectoryFail(ResourcePath includeDirectory) {
            return StatixCompileExceptions.includeDirectoryFail(includeDirectory);
        }

        public static StatixCompileException rootDirectoryFail(ResourcePath rootDirectory) {
            return StatixCompileExceptions.rootDirectoryFail(rootDirectory);
        }

        public static StatixCompileException checkFail(KeyedMessages messages) {
            return StatixCompileExceptions.checkFail(messages);
        }

        public static StatixCompileException compilerFail(Exception cause) {
            return withCause(StatixCompileExceptions.compilerFail(cause), cause);
        }

        private static StatixCompileException withCause(StatixCompileException e, Exception cause) {
            e.initCause(cause);
            return e;
        }


        public abstract <R> R match(Cases<R> cases);

        public StatixCompileExceptions.CasesMatchers.TotalMatcher_MainFileFail cases() {
            return StatixCompileExceptions.cases();
        }

        public StatixCompileExceptions.CaseOfMatchers.TotalMatcher_MainFileFail caseOf() {
            return StatixCompileExceptions.caseOf(this);
        }


        @Override public String getMessage() {
            return caseOf()
                .mainFileFail((mainFile) -> "Statix main file '" + mainFile + "' does not exist or is not a file")
                .includeDirectoryFail((includeDirectory) -> "Statix include directory '" + includeDirectory + "' does not exist or is not a directory")
                .rootDirectoryFail((rootDirectory) -> "Statix root directory '" + rootDirectory + "' does not exist or is not a directory")
                .checkFail((messages) -> "Parsing or checking Statix source files failed")
                .compilerFail((cause) -> "Statix compiler failed unexpectedly")
                ;
        }

        @Override public Throwable fillInStackTrace() {
            return this; // Do nothing so that no stack trace is created, saving memory and CPU time.
        }

        @Override public Optional<KeyedMessages> getOptionalMessages() {
            return StatixCompileExceptions.getMessages(this);
        }


        @Override public abstract int hashCode();

        @Override public abstract boolean equals(@Nullable Object obj);
    }
}
