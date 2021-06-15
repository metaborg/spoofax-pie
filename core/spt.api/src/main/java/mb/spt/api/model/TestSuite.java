package mb.spt.api.model;

import mb.common.util.ListView;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TestSuite {
    public final String name;
    public final ResourceKey file;
    public final ListView<TestCase> testCases;
    public final @Nullable String languageIdHint;
    public final @Nullable ResourcePath rootDirectoryHint;

    public TestSuite(
        String name,
        ResourceKey file,
        ListView<TestCase> testCases,
        @Nullable String languageIdHint,
        @Nullable ResourcePath rootDirectoryHint
    ) {
        this.name = name;
        this.testCases = testCases;
        this.languageIdHint = languageIdHint;
        this.file = file;
        this.rootDirectoryHint = rootDirectoryHint;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TestSuite testSuite = (TestSuite)o;
        if(!name.equals(testSuite.name)) return false;
        if(!file.equals(testSuite.file)) return false;
        if(!testCases.equals(testSuite.testCases)) return false;
        if(languageIdHint != null ? !languageIdHint.equals(testSuite.languageIdHint) : testSuite.languageIdHint != null)
            return false;
        return rootDirectoryHint != null ? rootDirectoryHint.equals(testSuite.rootDirectoryHint) : testSuite.rootDirectoryHint == null;
    }

    @Override public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + file.hashCode();
        result = 31 * result + testCases.hashCode();
        result = 31 * result + (languageIdHint != null ? languageIdHint.hashCode() : 0);
        result = 31 * result + (rootDirectoryHint != null ? rootDirectoryHint.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "TestSuite{" +
            "name='" + name + '\'' +
            ", file=" + file +
            ", testCases=" + testCases +
            ", languageIdHint='" + languageIdHint + '\'' +
            ", rootDirectoryHint=" + rootDirectoryHint +
            '}';
    }
}
