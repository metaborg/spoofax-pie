package mb.chars;

import mb.chars.task.CharsParse;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.common.StrategoUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

public class CharsDebugRemoveA implements TaskDef<CharsDebugRemoveA.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourceKey file;

        public Args(ResourceKey file) {
            this.file = file;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return file.equals(args.file);
        }

        @Override public int hashCode() {
            return Objects.hash(file);
        }

        @Override public String toString() {
            return "Args{" +
                "file=" + file +
                '}';
        }
    }


    private final CharsClassLoaderResources classloaderResources;
    private final CharsParse parse;
    private final CharsRemoveA removeA;

    @Inject
    public CharsDebugRemoveA(
        CharsClassLoaderResources classloaderResources,
        CharsParse parse,
        CharsRemoveA removeA
    ) {
        this.classloaderResources = classloaderResources;
        this.parse = parse;
        this.removeA = removeA;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args args) throws Exception {
        context.require(classloaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final ResourceKey file = args.file;
        return context
            .require(removeA, parse.createAstSupplier(file))
            .mapOrElse(
                ast -> CommandFeedback.of(ShowFeedback.showText(StrategoUtil.toString(ast), "A characters removed from '" + file + "'")),
                e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
            );
    }
}
