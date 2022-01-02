package mb.rv32im.task.debug;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.ResourceStringSupplier;
import mb.pie.api.StatelessSerializableFunction;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.rv32im.Rv32ImScope;
import mb.rv32im.task.ExecuteRiscV;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;

@Rv32ImScope
public class ShowExecuteRiscV implements TaskDef<ShowExecuteRiscV.Args, CommandFeedback> {
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
            return "ShowExecuteRiscV$Args{" +
                "file=" + file +
                '}';
        }
    }

    private final ExecuteRiscV executeRiscV;

    @Inject public ShowExecuteRiscV(ExecuteRiscV executeRiscV) {
        this.executeRiscV = executeRiscV;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, Args input) {
        final ResourceKey file = input.file;
        return context.require(executeRiscV, new ResourceStringSupplier(file).map(new WrapOk())).mapOrElse(
            text -> CommandFeedback.of(ShowFeedback.showText(text, "RISC-V execution output for '" + file + "'")),
            e -> CommandFeedback.ofTryExtractMessagesFrom(e, file)
        );
    }

    private static class WrapOk extends StatelessSerializableFunction<String, Result<String, ?>> {
        @Override public Result<String, ?> apply(String t) {
            return Result.ofOk(t);
        }
    }
}
