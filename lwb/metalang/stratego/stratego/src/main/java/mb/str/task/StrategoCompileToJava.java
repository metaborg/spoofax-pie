package mb.str.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.str.StrategoScope;
import mb.str.config.StrategoCompileConfig;
import mb.stratego.build.strincr.StrIncr;
import mb.stratego.build.util.StrategoGradualSetting;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.cmd.Arguments;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@StrategoScope
public class StrategoCompileToJava implements TaskDef<StrategoCompileToJava.Input, Result<None, ?>> {
    public static class Input implements Serializable {
        public final StrategoCompileConfig config;
        public final ArrayList<STask<?>> originTasks;

        public Input(StrategoCompileConfig config, ArrayList<STask<?>> originTasks) {
            this.config = config;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input)o;
            return config.equals(input.config) && originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(config, originTasks);
        }

        @Override public String toString() {
            return "Input{" +
                "config=" + config +
                ", originTasks=" + originTasks +
                '}';
        }
    }

    private final StrIncr strIncr;

    @Inject public StrategoCompileToJava(StrIncr strIncr) {
        this.strIncr = strIncr;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<None, ?> exec(ExecContext context, Input input) {
        final StrategoCompileConfig cfg = input.config;
        //noinspection ConstantConditions
        return Result.ofOkOrCatching(() -> context.require(strIncr, new StrIncr.Input(
            cfg.mainFile,
            cfg.outputJavaPackageId,
            cfg.includeDirs.asUnmodifiable(),
            cfg.builtinLibs.asUnmodifiable(),
            cfg.cacheDir,
            new ArrayList<>(),
            new Arguments(),
            cfg.outputDir,
            input.originTasks,
            cfg.projectDir,
            StrategoGradualSetting.NONE
        )));
    }
}
