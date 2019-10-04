package mb.tiger.spoofax.taskdef.command;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.spoofax.core.language.cli.CliCommand;
import mb.spoofax.core.language.cli.CliCommandItem;
import mb.spoofax.core.language.cli.CliParamDef;
import mb.spoofax.core.language.cli.CliParams;
import mb.spoofax.core.language.command.*;
import mb.spoofax.core.language.command.arg.ArgProviders;
import mb.spoofax.core.language.command.arg.Param;
import mb.spoofax.core.language.command.arg.ParamDef;
import mb.spoofax.core.language.command.arg.RawArgs;
import mb.tiger.spoofax.taskdef.TigerListDefNames;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class TigerCompileDirectory implements TaskDef<CommandInput<TigerCompileDirectory.Args>, CommandOutput>, CommandDef<TigerCompileDirectory.Args> {
    public static class Args implements Serializable {
        final ResourcePath dir;

        public Args(ResourcePath dir) {
            this.dir = dir;
        }

        @Override public boolean equals(@Nullable Object obj) {
            if(this == obj) return true;
            if(obj == null || getClass() != obj.getClass()) return false;
            final Args other = (Args) obj;
            return dir.equals(other.dir);
        }

        @Override public int hashCode() {
            return Objects.hash(dir);
        }

        @Override public String toString() {
            return dir.toString();
        }
    }


    private final TigerListDefNames listDefNames;
    private final ResourceService resourceService;


    @Inject public TigerCompileDirectory(TigerListDefNames listDefNames, ResourceService resourceService) {
        this.listDefNames = listDefNames;
        this.resourceService = resourceService;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandOutput exec(ExecContext context, CommandInput<Args> input) throws Exception {
        final ResourcePath dir = input.args.dir;

        final ResourceMatcher matcher = new AllResourceMatcher(new FileResourceMatcher(), new PathResourceMatcher(new ExtensionsPathMatcher("tig")));
        final HierarchicalResource directory = context.require(dir, ResourceStampers.modifiedDir(matcher));

        final StringBuffer sb = new StringBuffer();
        sb.append("[\n  ");
        try {
            final AtomicBoolean first = new AtomicBoolean(true);
            directory.list(matcher).forEach((f) -> {
                try {
                    if(!first.get()) {
                        sb.append(", ");
                    }
                    final @Nullable String defNames = context.require(listDefNames, f.getKey());
                    //noinspection ConstantConditions (defNames can really be null)
                    if(defNames != null) {
                        sb.append(defNames);
                    } else {
                        sb.append("[]");
                    }
                    sb.append('\n');
                    first.set(false);
                } catch(ExecException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch(RuntimeException e) {
            if(e.getCause() instanceof ExecException) {
                throw (ExecException) e.getCause();
            } else if(e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            } else {
                throw e;
            }
        }
        sb.append(']');

        final ResourcePath generatedPath = dir.appendSegment("_defnames.aterm");
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        return new CommandOutput(ListView.of(CommandFeedbacks.showFile(generatedPath, null)));
    }

    @Override public Task<CommandOutput> createTask(CommandInput<Args> input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "'Compile' directory (list definition names)";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override public EnumSetView<CommandExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(CommandExecutionType.ManualOnce, CommandExecutionType.AutomaticContinuous);
    }

    @Override public EnumSetView<CommandContextType> getRequiredContextTypes() {
        return EnumSetView.of(CommandContextType.Directory);
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(Param.of("dir", ResourcePath.class, true, ListView.of(ArgProviders.context())));
    }

    @Override public Args fromRawArgs(RawArgs rawArgs) {
        final ResourcePath dir = rawArgs.getOrThrow("dir");
        return new TigerCompileDirectory.Args(dir);
    }

    public CliCommandItem getCliCommandItem() {
        return CliCommand.of(this, "compile-dir", new CliParamDef(
                CliParams.positional("dir", 0, "DIR", "Directory to compile", null)
            ),
            "Compiles Tiger files in given directory and shows the compiled file"
        );
    }
}