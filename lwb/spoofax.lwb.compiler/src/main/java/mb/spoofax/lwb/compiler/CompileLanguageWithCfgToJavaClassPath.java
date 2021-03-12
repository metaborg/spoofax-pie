package mb.spoofax.lwb.compiler;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;

import javax.inject.Inject;

/**
 * Fully compiles a Spoofax language by constructing the configuration from configuration files at given root directory,
 * and passing it along to {@link CompileLanguageToJavaClassPath}.
 */
public class CompileLanguageWithCfgToJavaClassPath implements TaskDef<ResourcePath, Result<CompileLanguageToJavaClassPath.Output, CompileLanguageWithCfgToJavaClassPathException>> {
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
    public Result<CompileLanguageToJavaClassPath.Output, CompileLanguageWithCfgToJavaClassPathException> exec(ExecContext context, ResourcePath rootDirectory) {
        return context.require(cfgRootDirectoryToObject, rootDirectory)
            .mapErr(CompileLanguageWithCfgToJavaClassPathException::createInputFail)
            .flatMap(o -> context.require(compileLanguageToJavaClassPath, o.compileLanguageToJavaClassPathInput).mapErr(CompileLanguageWithCfgToJavaClassPathException::languageCompilerFail))
            ;
    }
}
