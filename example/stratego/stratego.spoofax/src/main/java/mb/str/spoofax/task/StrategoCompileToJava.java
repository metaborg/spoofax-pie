package mb.str.spoofax.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.StrIncr;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.metaborg.util.cmd.Arguments;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class StrategoCompileToJava implements TaskDef<StrategoCompileToJava.Args, Result<None, ?>> {
    public static class Args implements Serializable {
        public final ResourcePath projectDir;
        public final ResourcePath mainFile;
        public final ArrayList<ResourcePath> includeDirs;
        public final ArrayList<String> builtinLibs;
        public final @Nullable ResourcePath cacheDir;
        public final ResourcePath outputDir;
        public final String outputJavaPackageId;
        public final ArrayList<STask> originTasks;

        public Args(
            ResourcePath projectDir,
            ResourcePath mainFile,
            ArrayList<ResourcePath> includeDirs,
            ArrayList<String> builtinLibs,
            @Nullable ResourcePath cacheDir,
            ResourcePath outputDir,
            String outputJavaPackageId,
            ArrayList<STask> originTasks
        ) {
            this.projectDir = projectDir;
            this.mainFile = mainFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.cacheDir = cacheDir;
            this.outputDir = outputDir;
            this.outputJavaPackageId = outputJavaPackageId;
            this.originTasks = originTasks;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return projectDir.equals(args.projectDir) &&
                mainFile.equals(args.mainFile) &&
                includeDirs.equals(args.includeDirs) &&
                builtinLibs.equals(args.builtinLibs) &&
                Objects.equals(cacheDir, args.cacheDir) &&
                outputDir.equals(args.outputDir) &&
                outputJavaPackageId.equals(args.outputJavaPackageId) &&
                originTasks.equals(args.originTasks);
        }

        @Override public int hashCode() {
            return Objects.hash(projectDir, mainFile, includeDirs, builtinLibs, cacheDir, outputDir, outputJavaPackageId, originTasks);
        }
    }


    private final StrIncr strIncr;

    @Inject public StrategoCompileToJava(StrIncr strIncr) {
        this.strIncr = strIncr;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public Result<None, ?> exec(ExecContext context, Args args) {
        //noinspection ConstantConditions
        return Result.ofOkOrCatching(() -> context.require(strIncr, new StrIncr.Input(
            args.mainFile, args.outputJavaPackageId, args.includeDirs, args.builtinLibs, args.cacheDir, new ArrayList<>(), new Arguments(), args.outputDir, args.originTasks, args.projectDir
        )));
    }
}
