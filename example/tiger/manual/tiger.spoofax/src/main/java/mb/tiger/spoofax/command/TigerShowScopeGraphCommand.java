package mb.tiger.spoofax.command;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgProvider;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.task.TigerShowArgs;
import mb.tiger.spoofax.task.TigerShowScopeGraph;

import javax.inject.Inject;

@LanguageScope
public class TigerShowScopeGraphCommand implements CommandDef<TigerShowArgs> {
    private final TigerShowScopeGraph showScopeGraph;


    @Inject public TigerShowScopeGraphCommand(TigerShowScopeGraph showScopeGraph) {
        this.showScopeGraph = showScopeGraph;
    }


    @Override public String getId() {
        return showScopeGraph.getId();
    }

    @Override public String getDisplayName() {
        return "Show scope graph";
    }

    @Override
    public String getDescription() {
        return "Shows the scope graph of the program.";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(
            CommandExecutionType.ManualOnce,
            CommandExecutionType.ManualContinuous
        );
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("resource", ResourceKey.class, true, ListView.of(ArgProvider.context(CommandContextType.ResourceKey)))
        );
    }

    @Override public TigerShowArgs fromRawArgs(RawArgs rawArgs) {
        final ResourceKey resource = rawArgs.getOrThrow("resource");
        return new TigerShowArgs(resource, null);
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs args) {
        return showScopeGraph.createTask(args);
    }
}
