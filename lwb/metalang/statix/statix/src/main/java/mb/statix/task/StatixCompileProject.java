package mb.statix.task;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.StatixClassLoaderResources;
import mb.statix.StatixScope;
import mb.statix.task.spoofax.StatixGetSourceFiles;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

@StatixScope
public class StatixCompileProject implements TaskDef<ResourcePath, Result<ListView<StatixCompileModule.Output>, ?>> {
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

    @Override
    public Result<ListView<StatixCompileModule.Output>, ?> exec(ExecContext context, ResourcePath input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());

        final KeyedMessages messages = context.require(check, input);
        if(messages.containsError()) {
            return Result.ofErr(new MessagesException(messages, "Cannot compile Statix files of '" + input + "' because checking produced errors"));
        }

        final ArrayList<StatixCompileModule.Output> compileModuleOutputs = new ArrayList<>();
        for(ResourcePath sourceFile : context.require(getSourceFiles, input)) {
            final Result<Option<StatixCompileModule.Output>, ?> result = context.require(compileModule, new StatixCompileModule.Input(input, sourceFile));
            if(result.isErr()) {
                return Result.ofErr(result.unwrapErr());
            }
            result.ifOk(o -> o.ifSome(compileModuleOutputs::add));
        }

        return Result.ofOk(ListView.of(compileModuleOutputs));
    }

    @Override public boolean shouldExecWhenAffected(ResourcePath input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }
}
