package mb.tiger.spoofax.taskdef.command;

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
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.taskdef.TigerListDefNames;
import mb.tiger.spoofax.taskdef.TigerListLiteralVals;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class TigerAltCompileFile implements TaskDef<CommandInput<TigerAltCompileFile.Args>, CommandOutput>, CommandDef<TigerAltCompileFile.Args> {
    public static class Args implements Serializable {
        final ResourcePath file;
        final boolean listDefNames;
        final boolean base64Encode;
        final String compiledFileNameSuffix;

        public Args(ResourcePath file, boolean listDefNames, boolean base64Encode, String compiledFileNameSuffix) {
            this.file = file;
            this.listDefNames = listDefNames;
            this.base64Encode = base64Encode;
            this.compiledFileNameSuffix = compiledFileNameSuffix;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args) obj;
            return listDefNames == other.listDefNames &&
                base64Encode == other.base64Encode &&
                file.equals(other.file) &&
                compiledFileNameSuffix.equals(other.compiledFileNameSuffix);
        }

        @Override public int hashCode() {
            return Objects.hash(file, listDefNames, base64Encode, compiledFileNameSuffix);
        }

        @Override public String toString() {
            return file.toString();
        }
    }


    private final TigerListDefNames listDefNames;
    private final TigerListLiteralVals listLiteralVals;
    private final ResourceService resourceService;


    @Inject
    public TigerAltCompileFile(TigerListDefNames listDefNames, TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.listDefNames = listDefNames;
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, CommandInput<Args> input) throws Exception {
        final Args args = input.args;
        final ResourcePath file = input.args.file;

        @Nullable String str;
        if(args.listDefNames) {
            str = context.require(listDefNames, file);
        } else {
            str = context.require(listLiteralVals, file);
        }

        //noinspection ConstantConditions (str can really be null)
        if(str == null) {
            return new CommandOutput(ListView.of());
        }

        if(args.base64Encode) {
            str = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
        }

        final ResourcePath generatedPath = file.replaceLeafExtension(args.compiledFileNameSuffix);
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(str.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        //noinspection ConstantConditions (region may be null)
        return new CommandOutput(ListView.of(CommandFeedbacks.showFile(generatedPath, null)));
    }

    @Override public Serializable key(CommandInput<Args> input) {
        return input.args.file; // Task is keyed by file only.
    }

    @Override public Task<CommandOutput> createTask(CommandInput<Args> input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "'Alternative compile' file";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce, CommandExecutionType.AutomaticContinuous, CommandExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<CommandContextType> getRequiredContextTypes() {
        return EnumSetView.of(CommandContextType.File);
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(
            Param.of("file", ResourcePath.class, true, ListView.of(ArgProviders.context())),
            Param.of("listDefNames", boolean.class, false, ArgProviders.value(true)),
            Param.of("base64Encode", boolean.class, false, ArgProviders.value(false)),
            Param.of("compiledFileNameSuffix", String.class, true, ArgProviders.value("defnames.aterm"))
        );
    }

    @Override public Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath file = rawArgs.getOrThrow("file");
        final boolean listDefNames = rawArgs.getOrTrue("listDefNames");
        final boolean base64Encode = rawArgs.getOrFalse("base64Encode");
        final String compiledFileName = rawArgs.getOrThrow("compiledFileNameSuffix");
        return new TigerAltCompileFile.Args(file, listDefNames, base64Encode, compiledFileName);
    }
}