package mb.spt.task;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.testrunner.MultiTestSuiteRun;
import mb.spoofax.core.language.testrunner.TestSuiteRun;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class SptCheckForOutputAggregator implements TaskDef<SptCheckForOutputAggregator.Input, MultiTestSuiteRun> {
    private final mb.spt.SptClassLoaderResources classLoaderResources;
    private final SptCheckForOutput check;

    public static class Input implements Serializable {
        public final ResourcePath directory;
        public final ResourcePath rootDirectory;

        public Input(ResourcePath directory, ResourcePath rootDirectory) {
            this.directory = directory;
            this.rootDirectory = rootDirectory;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final SptCheckForOutputAggregator.Input input = (SptCheckForOutputAggregator.Input)o;
            if(!directory.equals(input.directory)) return false;
            return Objects.equals(rootDirectory, input.rootDirectory);
        }

        @Override public int hashCode() {
            int result = directory.hashCode();
            result = 31 * result + rootDirectory.hashCode();
            return result;
        }

        @Override public String toString() {
            return "SptCheckForOutput$Input{" +
                "directory=" + directory +
                ", rootDirectoryHint=" + rootDirectory +
                '}';
        }
    }

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

    @Override public MultiTestSuiteRun exec(ExecContext context, SptCheckForOutputAggregator.Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsLocalResource(getClass()), ResourceStampers.hashFile());
        final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input.rootDirectory);
        final HierarchicalResource selectedDirectory = context.getHierarchicalResource(input.directory);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(walker, ResourceMatcher.ofDirectory(), context::require);
        final ResourceMatcher matcher = ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtensions("spt")));
        final MultiTestSuiteRun testResults = new MultiTestSuiteRun();
        selectedDirectory.walkForEach(walker, matcher, file -> {
            final ResourceKey fileKey = file.getKey();
            final TestSuiteRun result = context.require(check, new SptCheckForOutput.Input(fileKey, input.rootDirectory));
            testResults.add(result);
        });
        return testResults;
    }
}
