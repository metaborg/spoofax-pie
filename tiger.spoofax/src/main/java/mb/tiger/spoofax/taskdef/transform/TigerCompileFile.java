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
import mb.tiger.spoofax.taskdef.TigerListLiteralVals;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

public class TigerCompileFile implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerListLiteralVals listLiteralVals;
    private final ResourceService resourceService;

    @Inject public TigerCompileFile(TigerListLiteralVals listLiteralVals, ResourceService resourceService) {
        this.listLiteralVals = listLiteralVals;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        final TransformSubject subject = input.subject;
        final ResourcePath file = TransformSubjects.getFile(subject)
            .orElseThrow(() -> new RuntimeException("Cannot compile, subject '" + subject + "' is not a file subject"));

        final @Nullable String literalsStr = context.require(listLiteralVals, file);
        if(literalsStr == null) {
            return new TransformOutput(ListView.of());
        }

        final ResourcePath generatedPath = file.replaceLeafExtension("literals.aterm");
        final HierarchicalResource generatedResource = resourceService.getResource(generatedPath);
        generatedResource.writeBytes(literalsStr.getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        return new TransformOutput(ListView.of(TransformFeedbacks.openEditorForFile(generatedPath, null)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "Compile Tiger file";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.ManualOnce, TransformExecutionType.AutomaticContinuous, TransformExecutionType.ManualContinuous);
    }

    @Override public EnumSetView<TransformSubjectType> getSupportedSubjectTypes() {
        return EnumSetView.of(TransformSubjectType.File);
    }
}