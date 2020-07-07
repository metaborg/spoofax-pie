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
import mb.tiger.spoofax.task.TigerCompileFile;

import javax.inject.Inject;

@LanguageScope
public class TigerCompileFileCommand implements CommandDef<TigerCompileFile.Args> {
    private final TigerCompileFile tigerCompileFile;


    @Inject public TigerCompileFileCommand(TigerCompileFile tigerCompileFile) {
        this.tigerCompileFile = tigerCompileFile;
    }


    @Override public String getId() {
        return tigerCompileFile.getId();
    }

    @Override public String getDisplayName() {
        return "Compile file (list literals)";
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
            Param.of("file", ResourcePath.class, true, ListView.of(ArgProvider.context(CommandContextType.File)))
        );
    }

    @Override public TigerCompileFile.Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath file = rawArgs.getOrThrow("file");
        return new TigerCompileFile.Args(file);
    }

    @Override public Task<CommandFeedback> createTask(TigerCompileFile.Args args) {
        return tigerCompileFile.createTask(args);
    }
}
