package mb.tiger.spoofax.command;

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
import mb.tiger.spoofax.TigerScope;
import mb.tiger.spoofax.task.TigerShowArgs;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@TigerScope
public class TigerInlineMethodCallCommand implements CommandDef<TigerShowArgs> {

    private final mb.tiger.spoofax.task.TigerInlineMethodCallCommand tigerInlineMethodCallCommand;

    @Inject public TigerInlineMethodCallCommand(
        mb.tiger.spoofax.task.TigerInlineMethodCallCommand tigerInlineMethodCallCommand
    ) {
        this.tigerInlineMethodCallCommand = tigerInlineMethodCallCommand;
    }

    @Override public String getId() {
        return tigerInlineMethodCallCommand.getId();
    }

    @Override public String getDisplayName() {
        return "Inline method call";
    }

    @Override
    public String getDescription() {
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

    @Override public TigerShowArgs fromRawArgs(RawArgs rawArgs) {
        final ResourceKey resource = rawArgs.getOrThrow("resource");
        final @Nullable Region region = rawArgs.getOrNull("region");
        return new TigerShowArgs(resource, region);
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs args) {
        return tigerInlineMethodCallCommand.createTask(args);
    }
}
