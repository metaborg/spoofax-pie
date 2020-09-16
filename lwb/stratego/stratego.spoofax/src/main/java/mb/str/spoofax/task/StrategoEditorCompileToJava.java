package mb.str.spoofax.task;

import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class StrategoEditorCompileToJava implements TaskDef<StrategoEditorCompileToJava.Args, CommandFeedback> {
    public static class Args implements Serializable {
        public final ResourcePath projectDir;
        public final ResourcePath mainFile;
        public final ArrayList<ResourcePath> includeDirs;
        public final ArrayList<String> builtinLibs;
        public final @Nullable ResourcePath cacheDir;
        public final @Nullable ResourcePath outputDir;
        public final @Nullable String outputJavaPackageId;

        public Args(
            ResourcePath projectDir,
            ResourcePath mainFile,
            ArrayList<ResourcePath> includeDirs,
            ArrayList<String> builtinLibs,
            @Nullable ResourcePath cacheDir,
            @Nullable ResourcePath outputDir,
            @Nullable String outputJavaPackageId
        ) {
            this.projectDir = projectDir;
            this.mainFile = mainFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.cacheDir = cacheDir;
            this.outputDir = outputDir;
            this.outputJavaPackageId = outputJavaPackageId;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final StrategoEditorCompileToJava.Args args = (StrategoEditorCompileToJava.Args)o;
            return projectDir.equals(args.projectDir) &&
                mainFile.equals(args.mainFile) &&
                includeDirs.equals(args.includeDirs) &&
                builtinLibs.equals(args.builtinLibs) &&
                Objects.equals(cacheDir, args.cacheDir) &&
                Objects.equals(outputDir, args.outputDir) &&
                Objects.equals(outputJavaPackageId, args.outputJavaPackageId);
        }

        @Override public int hashCode() {
            return Objects.hash(projectDir, mainFile, includeDirs, builtinLibs, cacheDir, outputDir, outputJavaPackageId);
        }
    }


    @Inject public StrategoEditorCompileToJava(StrategoCompileToJava compileToJava) {
        this.compileToJava = compileToJava;
    }

    private final StrategoCompileToJava compileToJava;


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public CommandFeedback exec(ExecContext context, StrategoEditorCompileToJava.Args input) throws Exception {
        final ArrayList<ResourcePath> includeDirs = new ArrayList<>(input.includeDirs);
        if(includeDirs.isEmpty()) {
            includeDirs.add(input.projectDir);
        }

        final ArrayList<String> builtinLibs = new ArrayList<>(input.builtinLibs);
        if(builtinLibs.isEmpty()) {
            builtinLibs.add("stratego-lib");
        }

        final ResourcePath outputDir;
        if(input.outputDir == null) {
            outputDir = input.projectDir.appendRelativePath("build/stratego/sources");
        } else {
            outputDir = input.outputDir;
        }
        context.require(outputDir, ResourceStampers.<HierarchicalResource>exists()).createDirectory(true);

        final String outputJavaPackageId;
        if(input.outputJavaPackageId == null) {
            outputJavaPackageId = "mb.test";
        } else {
            outputJavaPackageId = input.outputJavaPackageId;
        }
        
        final Result<None, ?> result = context.require(compileToJava, new StrategoCompileToJava.Args(input.projectDir, input.mainFile, includeDirs, builtinLibs, input.cacheDir, outputDir, outputJavaPackageId, new ArrayList<>()));
        return result.mapErrOrElse(e -> CommandFeedback.ofTryExtractMessagesFrom(e, input.mainFile), none -> CommandFeedback.of());
    }
}
