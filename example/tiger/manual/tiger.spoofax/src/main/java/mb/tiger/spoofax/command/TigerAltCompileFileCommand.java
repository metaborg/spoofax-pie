package mb.tiger.spoofax.command;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.Task;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.LanguageScope;
import mb.spoofax.core.language.command.CommandContextType;
import mb.spoofax.core.language.command.CommandDef;
import mb.spoofax.core.language.command.CommandExecutionType;
import mb.spoofax.core.language.command.CommandOutput;
import mb.spoofax.core.language.command.arg.ArgProvider;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.task.TigerAltCompileFile;

import javax.inject.Inject;

@LanguageScope
public class TigerAltCompileFileCommand implements CommandDef<TigerAltCompileFile.Args> {
    private final TigerAltCompileFile tigerAltCompileFile;


    @Inject public TigerAltCompileFileCommand(TigerAltCompileFile tigerAltCompileFile) {
        this.tigerAltCompileFile = tigerAltCompileFile;
    }


    @Override public String getId() {
        return tigerAltCompileFile.getId();
    }

    @Override public String getDisplayName() {
        return "'Alternative compile' file";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(
            CommandExecutionType.ManualOnce,
            CommandExecutionType.ManualContinuous,
            CommandExecutionType.AutomaticContinuous
        );
    }

    @Override public EnumSetView<CommandContextType> getRequiredContextTypes() {
        return EnumSetView.of(
            CommandContextType.File
        );
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("file", ResourcePath.class, true, ListView.of(ArgProvider.context())),
            Param.of("listDefNames", boolean.class, false, ListView.of(ArgProvider.value(true))),
            Param.of("base64Encode", boolean.class, false, ListView.of(ArgProvider.value(false))),
            Param.of("compiledFileNameSuffix", String.class, true, ListView.of(ArgProvider.value("defnames.aterm")))
        );
    }

    @Override public TigerAltCompileFile.Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath file = rawArgs.getOrThrow("file");
        final boolean listDefNames = rawArgs.getOrThrow("listDefNames");
        final boolean base64Encode = rawArgs.getOrThrow("base64Encode");
        final String compiledFileNameSuffix = rawArgs.getOrThrow("compiledFileNameSuffix");
        return new TigerAltCompileFile.Args(file, listDefNames, base64Encode, compiledFileNameSuffix);
    }

    @Override public Task<CommandOutput> createTask(TigerAltCompileFile.Args args) {
        return tigerAltCompileFile.createTask(args);
    }
}
