package mb.tiger.spoofax.taskdef.transform;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.transform.*;
import mb.tiger.spoofax.taskdef.TigerListDefNames;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

public class TigerAltCompileFile implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerListDefNames listDefNames;
    private final ResourceService resourceService;

    @Inject public TigerAltCompileFile(TigerListDefNames listDefNames, ResourceService resourceService) {
        this.listDefNames = listDefNames;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        final TransformContext subject = input.subject;
        final ResourcePath file = TransformSubjects.getFile(subject)
            .orElseThrow(() -> new RuntimeException("Cannot compile, subject '" + subject + "' is not a file subject"));

        final @Nullable String defNamesStr = context.require(listDefNames, file);
        //noinspection ConstantConditions (defNamesStr can really be null)
        if(defNamesStr == null) {
            return new TransformOutput(ListView.of());
        }

        final ResourcePath generatedPath = file.replaceLeafExtension("defnames.aterm");
        final HierarchicalResource generatedResource = resourceService.getHierarchicalResource(generatedPath);
        generatedResource.writeBytes(defNamesStr.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        //noinspection ConstantConditions (region may be null)
        return new TransformOutput(ListView.of(TransformFeedbacks.openEditorForFile(generatedPath, null)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "'Alternative compile' file (list definition names)";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.ManualOnce, TransformExecutionType.AutomaticContinuous, TransformExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<TransformContextType> getSupportedContextTypes() {
        return EnumSetView.of(TransformContextType.File);
    }
}