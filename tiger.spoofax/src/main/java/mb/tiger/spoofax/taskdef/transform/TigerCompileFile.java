package mb.tiger.spoofax.taskdef.transform;

import mb.common.region.Region;
import mb.common.util.CollectionView;
import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.*;
import mb.spoofax.core.language.transform.param.ArgProviders;
import mb.spoofax.core.language.transform.param.ParamDef;
import mb.spoofax.core.language.transform.param.Params;
import mb.spoofax.core.language.transform.param.RawArgs;
import mb.tiger.spoofax.taskdef.TigerListLiteralVals;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class TigerCompileFile implements TaskDef<TransformInput<TigerCompileFile.Args>, TransformOutput>, TransformDef<TigerCompileFile.Args> {
    public static class Args implements Serializable {
        final ResourcePath file;

        public Args(ResourcePath file) {
            this.file = file;
        }
    }

    private final TigerListLiteralVals listLiteralVals;
    private final ResourceService resourceService;


    @Inject public TigerCompileFile(TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }


    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput<Args> input) throws Exception {
        final ResourcePath file = input.arguments.file;

        final @Nullable String literalsStr = context.require(listLiteralVals, file);
        if(literalsStr == null) {
            return new TransformOutput(ListView.of());
        }

        final ResourcePath generatedPath = file.replaceLeafExtension("literals.aterm");
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(literalsStr.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        return new TransformOutput(ListView.of(TransformFeedbacks.openEditorForFile(generatedPath, null)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput<Args> input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "'Compile' file (list literals)";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.ManualOnce, TransformExecutionType.AutomaticContinuous, TransformExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<TransformContextType> getSupportedContextTypes() {
        return EnumSetView.of(TransformContextType.File);
    }

    @Override public ParamDef getParamDef() {
        return new ParamDef(CollectionView.of(Params.positional(0, ResourcePath.class, true, ListView.of(ArgProviders.context()))));
    }

    @Override public Args fromRawArgs(RawArgs rawArgs) {
        final @Nullable ResourcePath file = rawArgs.getPositional(0);
        if(file == null) {
            throw new RuntimeException("Could not create arguments from raw arguments '" + rawArgs + "', it has no positional argument at index 0");
        }
        return new Args(file);
    }
}