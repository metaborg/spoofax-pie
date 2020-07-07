package mb.tiger.spoofax.command;

import mb.common.region.Region;
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
import mb.tiger.spoofax.task.TigerShowDesugaredAst;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;

@LanguageScope
public class TigerShowDesugaredAstCommand implements CommandDef<TigerShowArgs> {
    private final TigerShowDesugaredAst tigerShowDesugaredAst;


    @Inject public TigerShowDesugaredAstCommand(TigerShowDesugaredAst tigerShowDesugaredAst) {
        this.tigerShowDesugaredAst = tigerShowDesugaredAst;
    }


    @Override public String getId() {
        return tigerShowDesugaredAst.getId();
    }

    @Override public String getDisplayName() {
        return "Show desugared AST";
    }

    @Override
    public String getDescription() {
        return "Shows the desugared Abstract Syntax Tree of the program.";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(
            CommandExecutionType.ManualOnce,
            CommandExecutionType.ManualContinuous
        );
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("resource", ResourceKey.class, true, ListView.of(ArgProvider.context(CommandContextType.ResourceKey))),
            Param.of("region", Region.class, false, ListView.of(ArgProvider.context(CommandContextType.Region)))
        );
    }

    @Override public TigerShowArgs fromRawArgs(RawArgs rawArgs) {
        final ResourceKey resource = rawArgs.getOrThrow("resource");
        final @Nullable Region region = rawArgs.getOrNull("region");
        return new TigerShowArgs(resource, region);
    }

    @Override public Task<CommandFeedback> createTask(TigerShowArgs args) {
        return tigerShowDesugaredAst.createTask(args);
    }
}
