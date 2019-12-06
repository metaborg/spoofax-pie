package mb.tiger.spoofax.taskdef.command;

import com.google.inject.Inject;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandInput;
import mb.spoofax.core.language.command.CommandOutput;
import mb.spoofax.core.language.command.arg.ArgProviders;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;

public class TigerCompileFileCommandDef implements CommandDef<TigerCompileFile.Args> {
    private final TigerCompileFile compileFile;


    @Inject public TigerCompileFileCommandDef(TigerCompileFile compileFile) {
        this.compileFile = compileFile;
    }


    @Override public String getId() {
        return compileFile.getId();
    }

    @Override public String getDisplayName() {
        return "'Compile' file (list literals)";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce, CommandExecutionType.AutomaticContinuous, CommandExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<CommandContextType> getRequiredContextTypes() {
        return EnumSetView.of(CommandContextType.File);
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(Param.of("file", ResourcePath.class, true, ListView.of(ArgProviders.context())));
    }

    @Override public TigerCompileFile.Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath file = rawArgs.getOrThrow("file");
        return new TigerCompileFile.Args(file);
    }

    @Override public Task<CommandOutput> createTask(CommandInput<TigerCompileFile.Args> input) {
        return compileFile.createTask(input);
    }
}
