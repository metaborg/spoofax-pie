package mb.spoofax.lwb.dynamicloading;

import mb.cfg.CompileLanguageInput;
import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgRootDirectoryToObjectException;
import mb.cfg.task.CfgToObject;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.compiler.CompileLanguageException;

import javax.inject.Inject;
import java.util.stream.Stream;

@DynamicLoadingScope
public class DynamicLoad implements TaskDef<CompileLanguage.Args, OutTransient<DynamicLanguage>> {
    private final CompileLanguage compileLanguage;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final DynamicLanguageLoader dynamicLanguageLoader;
    private final DynamicLanguageRegistry dynamicLanguageRegistry;

    @Inject public DynamicLoad(
        CompileLanguage compileLanguage,
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        DynamicLanguageLoader dynamicLanguageLoader, DynamicLanguageRegistry dynamicLanguageRegistry
    ) {
        this.compileLanguage = compileLanguage;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.dynamicLanguageLoader = dynamicLanguageLoader;
        this.dynamicLanguageRegistry = dynamicLanguageRegistry;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public OutTransient<DynamicLanguage> exec(ExecContext context, CompileLanguage.Args args) throws Exception {
        final Result<CompileLanguage.Output, CompileLanguageException> result = context.require(compileLanguage, args);
        final CompileLanguage.Output output = result.unwrap(); // TODO: properly handle error
        for(ResourcePath path : output.classPath()) {
            // HACK: create dependency to each file separately, instead of one for the directory, to ensure this task
            //       gets re-executed in a bottom-up build when any file changes
            try(Stream<? extends ReadableResource> files = context.require(path).walk(ResourceWalker.ofTrue(), ResourceMatcher.ofFile())) {
                for(ReadableResource file : new StreamIterable<>(files)) {
                    context.require(file, ResourceStampers.modifiedFile());
                }
            }
        }
        final Result<CfgToObject.Output, CfgRootDirectoryToObjectException> cfgResult = context.require(cfgRootDirectoryToObject, args.rootDirectory());
        final CompileLanguageInput compileInput = cfgResult.unwrap().compileLanguageInput; // TODO: properly handle error.
        final DynamicLanguage dynamicLanguage = dynamicLanguageLoader.load(args.rootDirectory(), compileInput, output.classPath());
        dynamicLanguageRegistry.reload(args.rootDirectory(), dynamicLanguage);
        return new OutTransientImpl<>(dynamicLanguage, true);
    }
}
