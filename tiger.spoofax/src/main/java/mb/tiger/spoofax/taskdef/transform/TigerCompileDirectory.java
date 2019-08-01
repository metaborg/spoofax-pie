package mb.tiger.spoofax.taskdef.transform;

import mb.common.util.EnumSetView;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.AllResourceMatcher;
import mb.resource.hierarchical.match.FileResourceMatcher;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.spoofax.core.language.transform.*;
import mb.tiger.spoofax.taskdef.TigerListDefNames;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class TigerCompileDirectory implements TaskDef<TransformInput, TransformOutput>, TransformDef {
    private final TigerListDefNames listDefNames;
    private final ResourceService resourceService;

    @Inject public TigerCompileDirectory(TigerListDefNames listDefNames, ResourceService resourceService) {
        this.listDefNames = listDefNames;
        this.resourceService = resourceService;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TransformOutput exec(ExecContext context, TransformInput input) throws Exception {
        final TransformContext subject = input.subject;
        final ResourcePath directoryPath = TransformSubjects.getDirectory(subject)
            .orElseThrow(() -> new RuntimeException("Cannot compile, subject '" + subject + "' is not a directory subject"));
        final ResourceMatcher matcher = new AllResourceMatcher(new FileResourceMatcher(), new PathResourceMatcher(new ExtensionsPathMatcher("tig")));
        final HierarchicalResource directory = context.require(directoryPath, ResourceStampers.modifiedDir(matcher));

        final StringBuffer sb = new StringBuffer();
        sb.append("[\n  ");
        try {
            final AtomicBoolean first = new AtomicBoolean(true);
            directory.list(matcher).forEach((f) -> {
                try {
                    if(!first.get()) {
                        sb.append(", ");
                    }
                    final @Nullable String defNames = context.require(listDefNames, f.getKey());
                    if(defNames != null) {
                        sb.append(defNames);
                    } else {
                        sb.append("[]");
                    }
                    sb.append('\n');
                    first.set(false);
                } catch(ExecException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch(RuntimeException e) {
            if(e.getCause() instanceof ExecException) {
                throw (ExecException) e.getCause();
            } else if(e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            } else {
                throw e;
            }
        }
        sb.append(']');

        final ResourcePath generatedPath = directoryPath.appendSegment("_defnames.aterm");
        final HierarchicalResource generatedResource = resourceService.getResource(generatedPath);
        generatedResource.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
        context.provide(generatedResource, ResourceStampers.hashFile());

        return new TransformOutput(ListView.of(TransformFeedbacks.openEditorForFile(generatedPath, null)));
    }

    @Override public Task<TransformOutput> createTask(TransformInput input) {
        return TaskDef.super.createTask(input);
    }


    @Override public String getDisplayName() {
        return "'Compile' directory (list definition names)";
    }

    @Override public EnumSetView<TransformExecutionType> getSupportedExecutionTypes() {
        return EnumSetView.of(TransformExecutionType.ManualOnce, TransformExecutionType.AutomaticContinuous);
    }

    @Override public EnumSetView<TransformContextType> getSupportedSubjectTypes() {
        return EnumSetView.of(TransformContextType.Directory);
    }
}