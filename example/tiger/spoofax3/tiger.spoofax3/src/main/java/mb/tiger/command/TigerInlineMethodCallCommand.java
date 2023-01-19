package mb.tiger.command;

import mb.common.region.Region;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgProvider;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.statix.referenceretention.pie.InlineMethodCallTaskDef;
import mb.tiger.TigerScope;
import mb.tiger.task.TigerInlineMethodCallTaskDef;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

// TODO: This command needs to be registered somewhere
@TigerScope
public class TigerInlineMethodCallCommand implements CommandDef<InlineMethodCallTaskDef.Input> {
    private final TigerInlineMethodCallTaskDef tigerInlineMethodCallTaskDef;

    @Inject public TigerInlineMethodCallCommand(
        TigerInlineMethodCallTaskDef tigerInlineMethodCallCommand
    ) {
        this.tigerInlineMethodCallTaskDef = tigerInlineMethodCallCommand;
    }

    @Override public String getId() {
        return this.tigerInlineMethodCallTaskDef.getId();
    }

    @Override public String getDisplayName() {
        return "Inline method call";
    }

    @Override public String getDescription() {
        return "Inlines a method call.";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(
            CommandExecutionType.ManualOnce
        );
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("resource", ResourceKey.class, true, ListView.of(ArgProvider.context(CommandContextType.ReadableResource))),
            Param.of("region", Region.class, false, ListView.of(ArgProvider.context(CommandContextType.Region)))
        );
    }

    @Override public InlineMethodCallTaskDef.Input fromRawArgs(RawArgs rawArgs) {
        final ResourceKey resource = rawArgs.getOrThrow("resource");
        final @Nullable Region region = rawArgs.getOrNull("region");
        return new InlineMethodCallTaskDef.Input(resource, region);
    }

    @Override public Task<CommandFeedback> createTask(InlineMethodCallTaskDef.Input args) {
        return tigerInlineMethodCallTaskDef.createTask(args);
    }
}
