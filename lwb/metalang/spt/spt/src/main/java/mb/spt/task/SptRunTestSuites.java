package mb.spt.task;

import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.PathMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import mb.spoofax.core.language.testrunner.TestResults;
import mb.spoofax.core.language.testrunner.TestSuiteResult;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SptRunTestSuites implements TaskDef<SptRunTestSuites.Input, TestResults> {
    private final mb.spt.SptClassLoaderResources classLoaderResources;
    private final SptRunTestSuite check;

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
            final SptRunTestSuites.Input input = (SptRunTestSuites.Input)o;
            if(!directory.equals(input.directory)) return false;
            return Objects.equals(rootDirectory, input.rootDirectory);
        }

        @Override public int hashCode() {
            int result = directory.hashCode();
            result = 31 * result + rootDirectory.hashCode();
            return result;
        }

        @Override public String toString() {
            return "SptRunTestSuites$Input{" +
                "directory=" + directory +
                ", rootDirectoryHint=" + rootDirectory +
                '}';
        }
    }

    @Inject public SptRunTestSuites(
        mb.spt.SptClassLoaderResources classLoaderResources,
        SptRunTestSuite check
    ){
        this.classLoaderResources = classLoaderResources;
        this.check = check;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    @Override public TestResults exec(ExecContext context, SptRunTestSuites.Input input) throws IOException {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        final ResourceWalker walker = ResourceWalker.ofPath(PathMatcher.ofNoHidden());
        final HierarchicalResource rootDirectory = context.getHierarchicalResource(input.rootDirectory);
        final HierarchicalResource selectedDirectory = context.getHierarchicalResource(input.directory);
        // Require directories recursively, so we re-execute whenever a file is added/removed from a directory.
        rootDirectory.walkForEach(walker, ResourceMatcher.ofDirectory(), context::require);
        final ResourceMatcher matcher = ResourceMatcher.ofFile().and(ResourceMatcher.ofPath(PathMatcher.ofExtensions("spt")));
        final List<TestSuiteResult> suiteResults = new ArrayList<>();
        selectedDirectory.walkForEach(walker, matcher, file -> {
            final ResourceKey fileKey = file.getKey();
            final TestSuiteResult result = context.require(check, new SptRunTestSuite.Input(fileKey, input.rootDirectory));
            suiteResults.add(result);
        });
        return new TestResults(ListView.of(suiteResults));
    }
}
