package mb.dynamix.task;

import mb.common.message.KeyedMessages;
import mb.common.option.Option;
import mb.common.result.MessagesException;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.dynamix.DynamixClassLoaderResources;
import mb.dynamix.DynamixScope;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Set;

@DynamixScope
public class DynamixCompileProject implements TaskDef<ResourcePath, Result<ListView<DynamixCompileModule.Output>, ?>> {
    private final DynamixClassLoaderResources classLoaderResources;
    private final DynamixCheckMulti check;
    private final DynamixGetSourceFiles getSourceFiles;
    private final DynamixCompileModule compileModule;


    @Inject public DynamixCompileProject(
        DynamixClassLoaderResources classLoaderResources,
        DynamixCheckMulti check,
        DynamixGetSourceFiles getSourceFiles,
        DynamixCompileModule compileModule
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
    public Result<ListView<DynamixCompileModule.Output>, ?> exec(ExecContext context, ResourcePath input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());

        final KeyedMessages messages = context.require(check, input);
        if(messages.containsError()) {
            return Result.ofErr(new MessagesException(messages, "Cannot compile Dynamix files of '" + input + "' because checking produced errors"));
        }

        final ArrayList<DynamixCompileModule.Output> compileModuleOutputs = new ArrayList<>();
        for(ResourcePath sourceFile : context.require(getSourceFiles, input)) {
            final Result<Option<DynamixCompileModule.Output>, ?> result = context.require(compileModule, new DynamixCompileModule.Input(input, sourceFile));
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
