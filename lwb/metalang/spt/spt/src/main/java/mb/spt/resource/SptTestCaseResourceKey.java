package mb.spt.resource;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SptTestCaseResourceKey implements ResourceKey {
    final String identifier;

    public SptTestCaseResourceKey(String identifier) {
        this.identifier = identifier;
    }

    public SptTestCaseResourceKey(String testSuite, String testCase) {
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


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SptTestCaseResourceKey that = (SptTestCaseResourceKey)o;
        return identifier.equals(that.identifier);
    }

    @Override public int hashCode() {
        return identifier.hashCode();
    }

    @Override public String toString() {
        return asString();
    }
}
