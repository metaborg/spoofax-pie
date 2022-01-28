package mb.spoofax.lwb.dynamicloading;

import mb.cfg.task.CfgRootDirectoryToObject;
import mb.cfg.task.CfgToObject;
import mb.common.result.Result;
import mb.common.util.StreamIterable;
import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.OutTransient;
import mb.pie.api.OutTransientImpl;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.lwb.compiler.CompileLanguage;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponent;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentManager;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Stream;

@DynamicLoadingScope
public class DynamicLoad implements TaskDef<CompileLanguage.Args, OutTransient<Result<DynamicComponent, ?>>> {
    private final CompileLanguage compileLanguage;
    private final CfgRootDirectoryToObject cfgRootDirectoryToObject;
    private final DynamicComponentManager dynamicComponentManager;

    @Inject public DynamicLoad(
        CompileLanguage compileLanguage,
        CfgRootDirectoryToObject cfgRootDirectoryToObject,
        DynamicComponentManager dynamicComponentManager
    ) {
        this.compileLanguage = compileLanguage;
        this.cfgRootDirectoryToObject = cfgRootDirectoryToObject;
        this.dynamicComponentManager = dynamicComponentManager;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public OutTransient<Result<DynamicComponent, ?>> exec(ExecContext context, CompileLanguage.Args args) throws Exception {
        return new OutTransientImpl<>(context.require(compileLanguage, args)
            .<Exception>mapErr(e -> e)
            .flatMapThrowing(
                compileLanguageOutput -> context.require(cfgRootDirectoryToObject, args.rootDirectory())
                    .mapThrowing(cfgOutput -> run(context, args.rootDirectory(), compileLanguageOutput, cfgOutput))
                    .mapErr(e -> e)
            ),
            true);
    }

    @Override public boolean shouldExecWhenAffected(CompileLanguage.Args input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    public DynamicComponent run(
        ExecContext context,
        ResourcePath rootDirectory,
        CompileLanguage.Output compileLanguageOutput,
        CfgToObject.Output cfgOutput
    ) throws Exception {
        for(ResourcePath path : compileLanguageOutput.javaClassPaths()) {
            // HACK: create dependency to each file separately, instead of one for the directory, to ensure this task
            //       gets re-executed in a bottom-up build when any file changes
            try(Stream<? extends ReadableResource> files = context.require(path).walk(ResourceWalker.ofTrue(), ResourceMatcher.ofFile())) {
                for(ReadableResource file : new StreamIterable<>(files)) {
                    context.require(file, ResourceStampers.modifiedFile());
                }
            }
        }
        return dynamicComponentManager.loadOrReloadFromCompiledSources(rootDirectory, compileLanguageOutput.javaClassPaths(), )
    }
}
