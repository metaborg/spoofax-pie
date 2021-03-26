package mb.str.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.str.StrategoScope;
import mb.str.config.StrategoCompileConfig;
import mb.stratego.build.strincr.StrIncr;

import javax.inject.Inject;
import java.util.ArrayList;

@StrategoScope
public class StrategoCompileToJava implements TaskDef<StrategoCompileConfig, Result<None, ?>> {
    private final StrIncr strIncr;

    @Inject public StrategoCompileToJava(StrIncr strIncr) {
        this.strIncr = strIncr;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<None, ?> exec(ExecContext context, StrategoCompileConfig config) {
        return Result.ofOkOrCatching(() -> context.require(strIncr, new StrIncr.Input(
            config.mainFile,
            config.outputJavaPackageId,
            config.includeDirs.asUnmodifiable(),
            config.builtinLibs.asUnmodifiable(),
            config.cacheDir,
            new ArrayList<>(),
            config.extraCompilerArguments,
            config.outputDir,
            config.sourceFileOrigins.asUnmodifiable(),
            config.rootDirectory,
            config.gradualTypingSetting
        )));
    }
}
