package mb.spoofax.lwb.compiler;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

/**
 * Fully compiles a Spoofax language by constructing the configuration from configuration files at given root directory,
 * and passing it along to {@link CompileLanguageToJavaClassPath}.
 */
public class CompileLanguageWithCfgToJavaClassPath implements TaskDef<CompileLanguageWithCfgToJavaClassPath.Args, Result<CompileLanguageToJavaClassPath.Output, CompileLanguageWithCfgToJavaClassPathException>> {
    public static class Args implements Serializable {
        public final ResourcePath resourcePath;
        public final ListView<File> additionalJavaClassPath;
        public final ListView<File> additionalJavaAnnotationProcessorPath;

        public Args(ResourcePath resourcePath, ListView<File> additionalJavaClassPath, ListView<File> additionalJavaAnnotationProcessorPath) {
            this.resourcePath = resourcePath;
            this.additionalJavaClassPath = additionalJavaClassPath;
            this.additionalJavaAnnotationProcessorPath = additionalJavaAnnotationProcessorPath;
        }

        public Args(ResourcePath resourcePath, ListView<File> additionalJavaClassPath) {
            this(resourcePath, additionalJavaClassPath, ListView.of());
        }

        public Args(ResourcePath resourcePath) {
            this(resourcePath, ListView.of());
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            if(!resourcePath.equals(args.resourcePath)) return false;
            if(!additionalJavaClassPath.equals(args.additionalJavaClassPath)) return false;
            return additionalJavaAnnotationProcessorPath.equals(args.additionalJavaAnnotationProcessorPath);
        }

        @Override public int hashCode() {
            int result = resourcePath.hashCode();
            result = 31 * result + additionalJavaClassPath.hashCode();
            result = 31 * result + additionalJavaAnnotationProcessorPath.hashCode();
            return result;
        }

        @Override public String toString() {
            return "CompileLanguageWithCfgToJavaClassPath$Args{" +
                "resourcePath=" + resourcePath +
                ", additionalJavaClassPath=" + additionalJavaClassPath +
                ", additionalJavaAnnotationProcessorPath=" + additionalJavaAnnotationProcessorPath +
                '}';
        }
    }

    public Task<Result<CompileLanguageToJavaClassPath.Output, CompileLanguageWithCfgToJavaClassPathException>> createTask(ResourcePath resourcePath) {
        return new Task<>(this, new Args(resourcePath));
    }


    private final CompileLanguageToJavaClassPath compileLanguageToJavaClassPath;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;

    @Inject
    public CompileLanguageWithCfgToJavaClassPath(
        CompileLanguageToJavaClassPath compileLanguageToJavaClassPath,
        CfgRootDirectoryToObject cfgRootDirectoryToObject
    ) {
        this.compileLanguageToJavaClassPath = compileLanguageToJavaClassPath;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override
    public Result<CompileLanguageToJavaClassPath.Output, CompileLanguageWithCfgToJavaClassPathException> exec(ExecContext context, CompileLanguageWithCfgToJavaClassPath.Args args) {
        return context.require(cfgRootDirectoryToObject, args.resourcePath)
            .mapErr(CompileLanguageWithCfgToJavaClassPathException::createInputFail)
            .flatMap(o -> context.require(compileLanguageToJavaClassPath, new CompileLanguageToJavaClassPath.Args(
                o.compileLanguageToJavaClassPathInput,
                args.additionalJavaClassPath,
                args.additionalJavaAnnotationProcessorPath)
            ).mapErr(CompileLanguageWithCfgToJavaClassPathException::languageCompilerFail))
            ;
    }
}
