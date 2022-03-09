package mb.spt.model;

import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.CoordinateRequirement;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TestSuite {
    public final String name;
    public final ResourceKey file;
    public final ListView<TestCase> testCases;
    public final @Nullable ResourcePath rootDirectoryHint;
    public final @Nullable CoordinateRequirement languageCoordinateRequirementHint;

    public TestSuite(
        String name,
        ResourceKey file,
        ListView<TestCase> testCases,
        @Nullable ResourcePath rootDirectoryHint,
        @Nullable CoordinateRequirement languageCoordinateRequirementHint
    ) {
        this.name = name;
        this.file = file;
        this.testCases = testCases;
        this.rootDirectoryHint = rootDirectoryHint;
        this.languageCoordinateRequirementHint = languageCoordinateRequirementHint;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TestSuite testSuite = (TestSuite)o;
        if(!name.equals(testSuite.name)) return false;
        if(!file.equals(testSuite.file)) return false;
        if(!testCases.equals(testSuite.testCases)) return false;
        if(rootDirectoryHint != null ? !rootDirectoryHint.equals(testSuite.rootDirectoryHint) : testSuite.rootDirectoryHint != null)
            return false;
        return languageCoordinateRequirementHint != null ? languageCoordinateRequirementHint.equals(testSuite.languageCoordinateRequirementHint) : testSuite.languageCoordinateRequirementHint == null;
    }

    @Override public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + file.hashCode();
        result = 31 * result + testCases.hashCode();
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        result = 31 * result + (languageCoordinateRequirementHint != null ? languageCoordinateRequirementHint.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "TestSuite{" +
            "name='" + name + '\'' +
            ", file=" + file +
            ", testCases=" + testCases +
            ", rootDirectoryHint=" + rootDirectoryHint +
            ", languageCoordinateRequirementHint=" + languageCoordinateRequirementHint +
            '}';
    }
}
