package mb.spt.model;

import mb.common.region.Region;
import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class TestCase {
    public final ResourcePath resource;
    public final ResourceKey testSuiteFile;
    public final @Nullable ResourcePath rootDirectoryHint;
    public final String description;
    public final Region descriptionRegion;
    public final TestFragment testFragment;
    public final ListView<TestExpectation> expectations;

    public TestCase(
        ResourcePath resource,
        ResourceKey testSuiteFile,
        @Nullable ResourcePath rootDirectoryHint,
        String description,
        Region descriptionRegion,
        TestFragment testFragment,
        ListView<TestExpectation> expectations
    ) {
        this.resource = resource;
        this.testSuiteFile = testSuiteFile;
        this.rootDirectoryHint = rootDirectoryHint;
        this.description = description;
        this.descriptionRegion = descriptionRegion;
        this.testFragment = testFragment;
        this.expectations = expectations;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TestCase testCase = (TestCase)o;
        if(!resource.equals(testCase.resource)) return false;
        if(!testSuiteFile.equals(testCase.testSuiteFile)) return false;
        if(!Objects.equals(rootDirectoryHint, testCase.rootDirectoryHint)) return false;
        if(!description.equals(testCase.description)) return false;
        if(!descriptionRegion.equals(testCase.descriptionRegion)) return false;
        if(!testFragment.equals(testCase.testFragment)) return false;
        return expectations.equals(testCase.expectations);
    }

    @Override public int hashCode() {
        int result = resource.hashCode();
        result = 31 * result + testSuiteFile.hashCode();
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        result = 31 * result + description.hashCode();
        result = 31 * result + descriptionRegion.hashCode();
        result = 31 * result + testFragment.hashCode();
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
            ", fragment=" + testFragment +
            ", expectations=" + expectations +
            '}';
    }
}
