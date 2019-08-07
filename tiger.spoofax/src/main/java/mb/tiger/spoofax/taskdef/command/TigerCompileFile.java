package mb.tiger.spoofax.taskdef.command;

import mb.common.util.CollectionView;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.*;
import mb.spoofax.core.language.command.arg.ArgProviders;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.Params;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.taskdef.TigerListLiteralVals;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TigerCompileFile implements TaskDef<CommandInput<TigerCompileFile.Args>, CommandOutput>, CommandDef<TigerCompileFile.Args> {
    public static class Args implements Serializable {
        final ResourcePath file;

        public Args(ResourcePath file) {
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args) obj;
            return file.equals(other.file);
        }

        @Override public int hashCode() {
            return Objects.hash(file);
        }

        @Override public String toString() {
            return file.toString();
        }
    }


    private final TigerListLiteralVals listLiteralVals;
    private final ResourceService resourceService;


    @Inject public TigerCompileFile(TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, CommandInput<Args> input) throws Exception {
        final ResourcePath file = input.args.file;

        final @Nullable String literalsStr = context.require(listLiteralVals, file);
        //noinspection ConstantConditions (literalsStr can really be null)
        if(literalsStr == null) {
            return new CommandOutput(ListView.of());
        }

        final ResourcePath generatedPath = file.replaceLeafExtension("literals.aterm");
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(literalsStr.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        return new CommandOutput(ListView.of(CommandFeedbacks.openEditorForFile(generatedPath, null)));
    }

    @Override public Task<CommandOutput> createTask(CommandInput<Args> input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "'Compile' file (list literals)";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce, CommandExecutionType.AutomaticContinuous, CommandExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<CommandContextType> getSupportedContextTypes() {
        return EnumSetView.of(CommandContextType.File);
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(CollectionView.of(Params.positional(0, ResourcePath.class, true, ListView.of(ArgProviders.context()))));
    }

    @Override public Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath file = rawArgs.getPositionalOrThrow(0);
        return new Args(file);
    }
}