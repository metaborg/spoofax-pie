package mb.spt.task;

import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spt.model.MultiTestSuiteRun;
import mb.spt.model.TestSuiteRun;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;

public class SptCheckForOutputAggregator implements TaskDef<ResourcePath, MultiTestSuiteRun> {
    private final mb.spt.SptClassLoaderResources classLoaderResources;
    private final SptCheckForOutput check;

    @Inject public SptCheckForOutputAggregator(
        mb.spt.SptClassLoaderResources classLoaderResources,
        SptCheckForOutput check
    ){
        this.classLoaderResources = classLoaderResources;
        this.check = check;
    }

    @Override public String getId() {
        return "mb.spt.task.SptCheckForOutputAggregator";
    }

    @Override public @Nullable MultiTestSuiteRun exec(ExecContext context, ResourcePath input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(walker, ResourceMatcher.ofDirectory(), context::require);
        final ResourceMatcher matcher = ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtensions("spt")));
        final MultiTestSuiteRun testResults = new MultiTestSuiteRun();
        rootDirectory.walkForEach(walker, matcher, file -> {
            final ResourceKey fileKey = file.getKey();
            final TestSuiteRun result = context.require(check, new SptCheckForOutput.Input(fileKey, input));
            testResults.add(result);
        });
        return testResults;
    }
}
