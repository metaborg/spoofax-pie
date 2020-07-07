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
import mb.tiger.spoofax.task.TigerCompileFileAlt;

import javax.inject.Inject;

@LanguageScope
public class TigerCompileFileAltCommand implements CommandDef<TigerCompileFileAlt.Args> {
    private final TigerCompileFileAlt tigerCompileFileAlt;


    @Inject public TigerCompileFileAltCommand(TigerCompileFileAlt tigerCompileFileAlt) {
        this.tigerCompileFileAlt = tigerCompileFileAlt;
    }


    @Override public String getId() {
        return tigerCompileFileAlt.getId();
    }

    @Override public String getDisplayName() {
        return "Alternative compile file";
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
            Param.of("file", ResourcePath.class, true, ListView.of(ArgProvider.context(CommandContextType.File))),
            Param.of("listDefNames", boolean.class, false, ListView.of(ArgProvider.value(true))),
            Param.of("base64Encode", boolean.class, false, ListView.of(ArgProvider.value(false))),
            Param.of("compiledFileNameSuffix", String.class, true, ListView.of(ArgProvider.value("defnames.aterm")))
        );
    }

    @Override public TigerCompileFileAlt.Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath file = rawArgs.getOrThrow("file");
        final boolean listDefNames = rawArgs.getOrThrow("listDefNames");
        final boolean base64Encode = rawArgs.getOrThrow("base64Encode");
        final String compiledFileNameSuffix = rawArgs.getOrThrow("compiledFileNameSuffix");
        return new TigerCompileFileAlt.Args(file, listDefNames, base64Encode, compiledFileNameSuffix);
    }

    @Override public Task<CommandFeedback> createTask(TigerCompileFileAlt.Args args) {
        return tigerCompileFileAlt.createTask(args);
    }
}
