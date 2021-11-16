package mb.spt.resource;

import mb.common.util.ListView;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.ResourcePathDefaults;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SptTestCaseResourcePath extends ResourcePathDefaults<SptTestCaseResourcePath> implements ResourcePath {
    final String identifier;

    public SptTestCaseResourcePath(String identifier) {
        this.identifier = identifier;
    }

    public SptTestCaseResourcePath(String testSuite, String testCase) {
        this(testSuite + "!!" + testCase);
    }


    @Override public String getQualifier() {
        return SptTestCaseResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return identifier;
    }

    @Override public String getIdAsString() {
        return identifier;
    }


    @Override protected SptTestCaseResourcePath self() {
        return this;
    }

    @Override public SptTestCaseResourcePath appendSegments(Iterable<String> segments) {
        return this;
    }

    @Override public SptTestCaseResourcePath appendRelativePath(String relativePath) {
        return this;
    }

    @Override public SptTestCaseResourcePath appendRelativePath(ResourcePath relativePath) {
        return this;
    }

    @Override public SptTestCaseResourcePath replaceLeaf(String segment) {
        return this;
    }

    @Override public boolean isAbsolute() {
        return true;
    }

    @Override public int getSegmentCount() {
        return 0;
    }

    @Override public Iterable<String> getSegments() {
        return ListView.of(identifier);
    }

    @Override public boolean startsWith(ResourcePath prefix) {
        return false;
    }

    @Override public @Nullable ResourcePath getParent() {
        return null;
    }

    @Override public @Nullable ResourcePath getRoot() {
        return null;
    }

    @Override public @Nullable String getLeaf() {
        return null;
    }

    @Override public ResourcePath getNormalized() {
        return this;
    }

    @Override public String relativize(ResourcePath other) {
        return identifier;
    }

    @Override public ResourcePath appendSegment(String segment) {
        return this;
    }

    @Override public ResourcePath appendOrReplaceWithPath(String other) {
        return this;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SptTestCaseResourcePath that = (SptTestCaseResourcePath)o;
        return identifier.equals(that.identifier);
    }

    @Override public int hashCode() {
        return identifier.hashCode();
    }

    @Override public String toString() {
        return asString();
    }
}
