package mb.spt.api.model;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TestCase {
    public final ResourceKey resource;
    public final ResourceKey testSuiteFile;
    public final @Nullable ResourcePath rootDirectoryHint;
    public final String description;
    public final Region descriptionRegion;
    public final Fragment fragment;
    public final ListView<TestExpectation> expectations;

    public TestCase(
        ResourceKey resource,
        ResourceKey testSuiteFile,
        @Nullable ResourcePath rootDirectoryHint,
        String description,
        Region descriptionRegion,
        Fragment fragment,
        ListView<TestExpectation> expectations
    ) {
        this.resource = resource;
        this.testSuiteFile = testSuiteFile;
        this.rootDirectoryHint = rootDirectoryHint;
        this.description = description;
        this.descriptionRegion = descriptionRegion;
        this.fragment = fragment;
        this.expectations = expectations;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TestCase testCase = (TestCase)o;
        if(!resource.equals(testCase.resource)) return false;
        if(!testSuiteFile.equals(testCase.testSuiteFile)) return false;
        if(rootDirectoryHint != null ? !rootDirectoryHint.equals(testCase.rootDirectoryHint) : testCase.rootDirectoryHint != null)
            return false;
        if(!description.equals(testCase.description)) return false;
        if(!descriptionRegion.equals(testCase.descriptionRegion)) return false;
        if(!fragment.equals(testCase.fragment)) return false;
        return expectations.equals(testCase.expectations);
    }

    @Override public int hashCode() {
        int result = resource.hashCode();
        result = 31 * result + testSuiteFile.hashCode();
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        result = 31 * result + description.hashCode();
        result = 31 * result + descriptionRegion.hashCode();
        result = 31 * result + fragment.hashCode();
        result = 31 * result + expectations.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TestCase{" +
            "resource=" + resource +
            ", testSuiteFile=" + testSuiteFile +
            ", rootDirectoryHint=" + rootDirectoryHint +
            ", description='" + description + '\'' +
            ", descriptionRegion=" + descriptionRegion +
            ", fragment=" + fragment +
            ", expectations=" + expectations +
            '}';
    }
}
