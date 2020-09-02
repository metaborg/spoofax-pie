package mb.spoofax.intellij.resource;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IntellijResourceKey implements ResourceKey {
    private final String url;


    public IntellijResourceKey(String url) {
        this.url = url;
    }


    @Override public String getQualifier() {
        return IntellijResourceRegistry.qualifier;
    }

    @Override public String getId() {
        return url;
    }

    @Override public String getIdAsString() {
        return url;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final IntellijResourceKey that = (IntellijResourceKey)o;
        return url.equals(that.url);
    }

    @Override public int hashCode() {
        return url.hashCode();
    }

    @Override public String toString() {
        return asString();
    }
}
