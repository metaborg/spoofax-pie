package mb.statix.task;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.task.spoofax.StatixGetSourceFiles;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

@StatixScope
public class StatixCompileProject implements TaskDef<StatixCompileProject.Input, Result<StatixCompileProject.Output, ?>> {
    public static class Input implements Serializable {
        public final ResourcePath rootDirectory;
        public final ListView<STask<?>> sourceFileOrigins;

        public Input(ResourcePath rootDirectory, ListView<STask<?>> sourceFileOrigins) {
            this.rootDirectory = rootDirectory;
            this.sourceFileOrigins = sourceFileOrigins;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            if(!rootDirectory.equals(input.rootDirectory)) return false;
            return sourceFileOrigins.equals(input.sourceFileOrigins);
        }

        @Override public int hashCode() {
            int result = rootDirectory.hashCode();
            result = 31 * result + sourceFileOrigins.hashCode();
            return result;
        }

        @Override public String toString() {
            return "StatixCompileProject.Input{" +
                "rootDirectory=" + rootDirectory +
                ", sourceFileOrigins=" + sourceFileOrigins +
                '}';
        }
    }

    public static class Output implements Serializable {
        public final ListView<StatixCompileModule.Output> compileModuleOutputs;

        public Output(ListView<StatixCompileModule.Output> compileModuleOutputs) {
            this.compileModuleOutputs = compileModuleOutputs;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Output output = (Output)o;
            return compileModuleOutputs.equals(output.compileModuleOutputs);
        }

        @Override public int hashCode() {
            return compileModuleOutputs.hashCode();
        }

        @Override public String toString() {
            return "StatixCompileProject.Output{" +
                "compileModuleOutputs=" + compileModuleOutputs +
                '}';
        }
    }

    private final StatixClassLoaderResources classLoaderResources;
    private final StatixCheckMulti check;
    private final StatixGetSourceFiles getSourceFiles;
    private final StatixCompileModule compileModule;


    @Inject public StatixCompileProject(
        StatixClassLoaderResources classLoaderResources,
        StatixCheckMulti check,
        StatixGetSourceFiles getSourceFiles,
        StatixCompileModule compileModule
    ) {
        this.classLoaderResources = classLoaderResources;
        this.check = check;
        this.getSourceFiles = getSourceFiles;
        this.compileModule = compileModule;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<StatixCompileProject.Output, ?> exec(ExecContext context, Input input) throws Exception {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(Input.class), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsLocalResource(Output.class), ResourceStampers.hashFile());

        final KeyedMessages messages = context.require(check, input.rootDirectory);
        if(messages.containsError()) {
            return Result.ofErr(new MessagesException(messages, "Cannot compile Statix files of '" + input.rootDirectory + "' because checking produced errors"));
        }

        final ArrayList<StatixCompileModule.Output> compileModuleOutputs = new ArrayList<>();
        for(ResourcePath sourceFile : context.require(getSourceFiles, input.rootDirectory)) {
            final Result<Option<StatixCompileModule.Output>, ?> result = context.require(compileModule, new StatixCompileModule.Input(input.rootDirectory, sourceFile, input.sourceFileOrigins));
            if(result.isErr()) {
                return Result.ofErr(result.unwrapErr());
            }
            result.ifOk(o -> o.ifSome(compileModuleOutputs::add));
        }

        return Result.ofOk(new Output(ListView.of(compileModuleOutputs)));
    }

    @Override public boolean shouldExecWhenAffected(Input input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
