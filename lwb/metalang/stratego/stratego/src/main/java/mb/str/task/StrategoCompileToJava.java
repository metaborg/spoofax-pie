package mb.str.task;

import mb.common.message.KeyedMessages;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoScope;
import mb.str.config.StrategoCompileConfig;
import mb.str.incr.MessageConverter;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

@StrategoScope
public class StrategoCompileToJava implements TaskDef<StrategoCompileConfig, Result<StrategoCompileToJava.Output, MessagesException>> {
    public static class Output implements Serializable {
        public final LinkedHashSet<ResourcePath> javaSourceFiles;
        public final KeyedMessages messages;

        public Output(
            LinkedHashSet<ResourcePath> javaSourceFiles,
            KeyedMessages messages
        ) {
            this.javaSourceFiles = javaSourceFiles;
            this.messages = messages;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            if(!javaSourceFiles.equals(output.javaSourceFiles)) return false;
            return messages.equals(output.messages);
        }

        @Override public int hashCode() {
            int result = javaSourceFiles.hashCode();
            result = 31 * result + messages.hashCode();
            return result;
        }

        @Override public String toString() {
            return "StrategoCompileToJava$Output{" +
                "javaSourceFiles=" + javaSourceFiles +
                ", messages=" + messages +
                '}';
        }
    }

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
    public Result<Output, MessagesException> exec(ExecContext context, StrategoCompileConfig config) {
        final CompileOutput output = context.require(compile, new CompileInput(
            config.mainModule,
            config.rootDirectory,
            config.javaSourceFileOutputDir,
            config.javaClassFileOutputDir,
            config.outputJavaPackageId,
            config.cacheDir,
            new ArrayList<>(),
            config.includeDirs.asCopy(),
            config.builtinLibs.asCopy(),
            config.extraCompilerArguments,
            config.sourceFileOrigins.asCopy(),
            true,
            true,
            config.outputLibraryName,
            config.str2libraries.asCopy()
        ));
        if(output instanceof CompileOutput.Failure) {
            final CompileOutput.Failure failure = (CompileOutput.Failure)output;
            return Result.ofErr(new MessagesException(MessageConverter.convertMessages(config.rootDirectory, failure.messages, resourceService), "Stratego compilation failed"));
        }
        final CompileOutput.Success success = (CompileOutput.Success)output;
        return Result.ofOk(new Output(success.resultFiles, MessageConverter.convertMessages(config.rootDirectory, success.messages, resourceService)));
    }

    @Override public boolean shouldExecWhenAffected(StrategoCompileConfig input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
