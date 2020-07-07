package mb.tiger.spoofax.command;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.arg.ArgProvider;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.task.TigerCompileDirectory;

import javax.inject.Inject;

@LanguageScope
public class TigerCompileDirectoryCommand implements CommandDef<TigerCompileDirectory.Args> {
    private final TigerCompileDirectory tigerCompileDirectory;


    @Inject public TigerCompileDirectoryCommand(TigerCompileDirectory tigerCompileDirectory) {
        this.tigerCompileDirectory = tigerCompileDirectory;
    }


    @Override public String getId() {
        return tigerCompileDirectory.getId();
    }

    @Override public String getDisplayName() {
        return "Compile directory (list definition names)";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(
            CommandExecutionType.ManualOnce,
            CommandExecutionType.ManualContinuous,
            CommandExecutionType.AutomaticContinuous
        );
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("dir", ResourcePath.class, true, ListView.of(ArgProvider.context(CommandContextType.Directory)))
        );
    }

    @Override public TigerCompileDirectory.Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath dir = rawArgs.getOrThrow("dir");
        return new TigerCompileDirectory.Args(dir);
    }

    @Override public Task<CommandFeedback> createTask(TigerCompileDirectory.Args args) {
        return tigerCompileDirectory.createTask(args);
    }
}
